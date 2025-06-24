/*
    SkyMarket is a shop that rotates it's inventory after a set period of time.
    Copyright (C) 2024 lukeskywlker19

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.github.lukesky19.skymarket.manager;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.time.Time;
import com.github.lukesky19.skylib.api.time.TimeUtil;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.configuration.LocaleManager;
import com.github.lukesky19.skymarket.configuration.MarketConfigManager;
import com.github.lukesky19.skymarket.data.config.Locale;
import com.github.lukesky19.skymarket.data.config.gui.ChestConfig;
import com.github.lukesky19.skymarket.data.config.gui.MerchantConfig;
import com.github.lukesky19.skymarket.data.MarketData;
import com.github.lukesky19.skymarket.data.PlayerData;
import com.github.lukesky19.skymarket.gui.ChestMarketGUI;
import com.github.lukesky19.skymarket.gui.MerchantMarketGUI;
import com.github.lukesky19.skymarket.util.MarketType;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * This class contains methods to interface with {@link MarketData} and the refreshing of markets.
 */
public class MarketManager {
    private final @NotNull SkyMarket skyMarket;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull GUIManager guiManager;
    private final @NotNull MarketConfigManager marketConfigManager;
    private final @NotNull MarketDataManager marketDataManager;
    private final @NotNull ButtonManager buttonManager;
    private final @NotNull TradeManager tradeManager;

    /**
     * Default Constructor. You should use {@link MarketManager#MarketManager(SkyMarket, LocaleManager, GUIManager, MarketConfigManager, MarketDataManager, ButtonManager, TradeManager)} instead.
     * @deprecated You should use {@link MarketManager#MarketManager(SkyMarket, LocaleManager, GUIManager, MarketConfigManager, MarketDataManager, ButtonManager, TradeManager)} instead.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public MarketManager() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param skyMarket A {@link SkyMarket} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param marketConfigManager A {@link MarketConfigManager} instance.
     * @param marketDataManager A {@link MarketDataManager} instance.
     * @param buttonManager A {@link ButtonManager} instance.
     * @param tradeManager A {@link TradeManager} instance.
     */
    public MarketManager(
            @NotNull SkyMarket skyMarket,
            @NotNull LocaleManager localeManager,
            @NotNull GUIManager guiManager,
            @NotNull MarketConfigManager marketConfigManager,
            @NotNull MarketDataManager marketDataManager,
            @NotNull ButtonManager buttonManager,
            @NotNull TradeManager tradeManager) {
        this.skyMarket = skyMarket;
        this.localeManager = localeManager;
        this.guiManager = guiManager;
        this.marketConfigManager = marketConfigManager;
        this.marketDataManager = marketDataManager;
        this.buttonManager = buttonManager;
        this.tradeManager = tradeManager;
    }

    /**
     * This should only be run on plugin load or reload. To refresh markets, use {@link #refreshMarkets()} or {@link #refreshMarket(String)}
     */
    public void reload() {
        marketConfigManager.getChestConfigs().forEach((marketId, chestConfig) -> {
            // Config is validated on load so these will never be null.
            assert chestConfig.marketName() != null;
            assert chestConfig.guiData().guiType() != null;
            assert chestConfig.guiData().guiName() != null;
            MarketData marketData = new MarketData(chestConfig.marketName(), MarketType.CHEST, chestConfig.guiData().guiType(), chestConfig.guiData().guiName(), buttonManager.createButtons(chestConfig.guiData().guiType(), chestConfig, marketId), List.of());

            // Calculate the delay time and when the next refresh will occur.
            assert chestConfig.refreshTime() != null; // Config is validated on load.
            long delayMilliseconds = TimeUtil.stringToMillis(chestConfig.refreshTime());
            long delaySeconds = delayMilliseconds / 1000;
            long refreshTime = System.currentTimeMillis() + delayMilliseconds;

            // Restart the refresh task
            BukkitTask refreshTask = skyMarket.getServer().getScheduler().runTaskLater(skyMarket, () -> refreshMarket(marketId), delaySeconds * 20);

            // Set the refresh task in the market data
            marketData.setRefreshTask(refreshTask);

            // Set the refresh time in the market data
            marketData.setRefreshTime(refreshTime);

            // Store the MarketData in MarketDataManager
            marketDataManager.setMarketData(marketId, marketData);
        });

        marketConfigManager.getMerchantConfigs().forEach((marketId, merchantConfig) -> {
            // Config is validated on load so this will never be null.
            assert merchantConfig.marketName() != null;
            assert merchantConfig.guiName() != null;
            MarketData marketData = new MarketData(merchantConfig.marketName(), MarketType.MERCHANT, GUIType.MERCHANT, merchantConfig.guiName(), new HashMap<>(), tradeManager.createTrades(merchantConfig));

            // Calculate the delay time and when the next refresh will occur.
            assert merchantConfig.refreshTime() != null; // Config is validated on load.
            long delayMilliseconds = TimeUtil.stringToMillis(merchantConfig.refreshTime());
            long delaySeconds = delayMilliseconds / 1000;
            long refreshTime = System.currentTimeMillis() + delayMilliseconds;

            System.out.println(delayMilliseconds);

            System.out.println(delaySeconds);

            System.out.println(refreshTime);

            // Restart the refresh task
            BukkitTask refreshTask = skyMarket.getServer().getScheduler().runTaskLater(skyMarket, () -> refreshMarket(marketId), delaySeconds * 20);

            // Set the refresh task in the market data
            marketData.setRefreshTask(refreshTask);

            // Set the refresh time in the market data
            marketData.setRefreshTime(refreshTime);

            // Store the MarketData in MarketDataManager
            marketDataManager.setMarketData(marketId, marketData);
        });
    }

    /**
     * Refreshes a specific market based on the market id.
     * @param marketId The id of the market to refresh.
     * @return true if the market refreshed successfully, false if not.
     */
    public boolean refreshMarket(@NotNull String marketId) {
        Locale locale = localeManager.getLocale();

        MarketData marketData = marketDataManager.getMarketData(marketId);
        if(marketData == null) return false;

        @NotNull MarketType marketType = marketData.getMarketType();
        @Nullable BukkitTask refreshTask = marketData.getRefreshTask();

        // Cancel the refresh task and set it to null.
        if(refreshTask != null && !refreshTask.isCancelled()) {
            refreshTask.cancel();
            marketData.setRefreshTask(null);
        }

        if(marketType.equals(MarketType.CHEST)) {
            // Get the configuration for the market
            @Nullable ChestConfig marketConfig = marketConfigManager.getChestConfig(marketId);
            if(marketConfig == null) return false;

            // Generate a new Map of buttons and update the map inside the market
            assert marketConfig.guiData().guiType() != null; // Config is validated on load.
            Map<Integer, GUIButton> buttonMap = buttonManager.createButtons(marketConfig.guiData().guiType(), marketConfig, marketId);
            marketData.setButtons(buttonMap);

            // Create the placeholders list
            List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("market_name", marketData.getMarketName()));
            // Tell all online players that the market was refreshed.
            skyMarket.getServer().getOnlinePlayers().forEach(player -> {
                if(player.isOnline() && player.isConnected()) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.marketRefreshed(), placeholders));
                }
            });

            // Calculate the delay time and when the next refresh will occur.
            assert marketConfig.refreshTime() != null; // Config is validated on load.
            long delayMilliseconds = TimeUtil.stringToMillis(marketConfig.refreshTime());
            long delaySeconds = delayMilliseconds / 1000;
            long refreshTime = System.currentTimeMillis() + delayMilliseconds;

            System.out.println(delayMilliseconds);

            System.out.println(delaySeconds);

            System.out.println(refreshTime);

            // Restart the refresh task
            refreshTask = skyMarket.getServer().getScheduler().runTaskLater(skyMarket, () -> refreshMarket(marketId), delaySeconds * 20);

            // Set the refresh task in the market data
            marketData.setRefreshTask(refreshTask);

            // Set the refresh time in the market data
            marketData.setRefreshTime(refreshTime);
        } else {
            // Get the configuration for the market
            @Nullable MerchantConfig tradeConfig = marketConfigManager.getMerchantConfig(marketId);
            if(tradeConfig == null) return false;

            // Get the market's name. This is separate from its id.
            @Nullable String marketName = marketData.getMarketName();

            // Generate a new list of trades and update the list in the market.
            List<MerchantRecipe> tradeList = tradeManager.createTrades(tradeConfig);
            marketData.setTrades(tradeList);

            // Create the placeholders list
            List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("market_name", marketName));
            // Tell all online players that the market was refreshed.
            skyMarket.getServer().getOnlinePlayers().forEach(player -> {
                if(player.isOnline() && player.isConnected()) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.marketRefreshed(), placeholders));
                }
            });

            // Calculate the delay time and when the next refresh will occur.
            assert tradeConfig.refreshTime() != null; // Config is validated on load.
            long delayMilliseconds = TimeUtil.stringToMillis(tradeConfig.refreshTime());
            long delaySeconds = delayMilliseconds / 1000;
            long refreshTime = System.currentTimeMillis() + delayMilliseconds;

            System.out.println(delayMilliseconds);

            System.out.println(delaySeconds);

            System.out.println(refreshTime);

            // Restart the refresh task
            refreshTask = skyMarket.getServer().getScheduler().runTaskLater(skyMarket, () -> refreshMarket(marketId), delaySeconds * 20);

            // Set the refresh task in the market data
            marketData.setRefreshTask(refreshTask);

            // Set the refresh time in the market data
            marketData.setRefreshTime(refreshTime);
        }

        return true;
    }

    /**
     * Refreshes all markets.
     */
    public void refreshMarkets() {
        marketConfigManager.getChestConfigs().forEach((marketId, chestConfig) -> {
            if(chestConfig.refreshTime() != null) {
                refreshMarket(marketId);
            }
        });

        marketConfigManager.getMerchantConfigs().forEach((marketId, merchantConfig) -> {
            if(merchantConfig.refreshTime() != null) {
                refreshMarket(marketId);
            }
        });
    }

    /**
     * Opens a market based on the market id.
     * @param marketId The market id of the market to open.
     * @param player The {@link Player} who wants to view the market.
     * @return true if the market was opened, false if not.
     */
    public boolean openMarket(@NotNull String marketId, @NotNull Player player) {
        Locale locale = localeManager.getLocale();
        ComponentLogger logger = skyMarket.getComponentLogger();
        UUID uuid = player.getUniqueId();

        MarketData marketData = marketDataManager.getMarketData(marketId);
        if(marketData == null) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.invalidMarketId()));
            return false;
        }

        if(marketData.getMarketType().equals(MarketType.CHEST)) {
            GUIType guiType = marketData.getGuiType();
            String guiName = marketData.getGuiName();

            ChestMarketGUI marketGUI = new ChestMarketGUI(skyMarket, guiManager, player, guiType, guiName, marketData.getButtons());

            boolean creationResult = marketGUI.create();
            if(!creationResult) {
                logger.error(AdventureUtil.serialize("Unable to create the InventoryView for a market GUI of id " + marketId + " for player " + player.getName() + " due to a configuration error."));
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                return false;
            }

            // This method is completed sync, the api returns a CompletableFuture for supporting plugins with async requirements.
            @NotNull CompletableFuture<Boolean> updateFuture = marketGUI.update();
            try {
                boolean updateResult = updateFuture.get();

                if(!updateResult) {
                    logger.error(AdventureUtil.serialize("Unable to decorate a market GUI of id " + marketId + " for player " + player.getName() + " due to a configuration error."));
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return false;
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.error(AdventureUtil.serialize("Unable to decorate a market GUI of id " + marketId + " for player " + player.getName() + " due to a configuration error. " + e.getMessage()));
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                return false;
            }

            boolean openResult = marketGUI.open();
            if(!openResult) {
                logger.error(AdventureUtil.serialize("Unable to open a market GUI of id " + marketId + " for player " + player.getName() + " due to a configuration error."));
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                return false;
            }
        } else {
            List<MerchantRecipe> trades = marketData.getTrades();
            PlayerData playerData = marketData.getPlayerData(uuid);
            if(!playerData.getPlayerTrades().isEmpty()) trades = playerData.getPlayerTrades();

            MerchantMarketGUI tradeGUI = new MerchantMarketGUI(skyMarket, guiManager, player, marketId, marketData.getGuiName(), trades, this);

            boolean creationResult = tradeGUI.create();
            if(!creationResult) {
                logger.error(AdventureUtil.serialize("Unable to create the InventoryView for a trade GUI of id " + marketId + " for player " + player.getName() + " due to a configuration error."));
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                return false;
            }

            // This method is completed sync, the api returns a CompletableFuture for supporting plugins with async requirements.
            @NotNull CompletableFuture<Boolean> updateFuture = tradeGUI.update();
            try {
                boolean updateResult = updateFuture.get();

                if(!updateResult) {
                    logger.error(AdventureUtil.serialize("Unable to decorate a trade GUI of id " + marketId + " for player " + player.getName() + " due to a configuration error."));
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                    return false;
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.error(AdventureUtil.serialize("Unable to decorate a trade GUI of id " + marketId + " for player " + player.getName() + " due to a configuration error. " + e.getMessage()));
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                return false;
            }

            boolean openResult = tradeGUI.open();
            if(!openResult) {
                logger.error(AdventureUtil.serialize("Unable to open a trade GUI of id " + marketId + " for player " + player.getName() + " due to a configuration error."));
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
                return false;
            }
        }

        return true;
    }

    /**
     * Takes a {@link List} of {@link MerchantRecipe}s from a {@link MerchantMarketGUI} and stores it for later use.
     * @param marketId The market id.
     * @param uuid The {@link UUID} of the player.
     * @param trades A {@link List} of {@link MerchantRecipe}s.
     */
    public void updatePlayerTrades(@NotNull String marketId, @NotNull UUID uuid, @NotNull List<MerchantRecipe> trades) {
        @Nullable MarketData marketData = marketDataManager.getMarketData(marketId);
        if(marketData == null) return;
        PlayerData playerData = marketData.getPlayerData(uuid);

        playerData.setPlayerTrades(trades);
    }

    /**
     * Gets a {@link List} of {@link String}s for all known market ids.
     * @return A {@link List} of {@link String}s for all known market ids
     */
    public @NotNull List<String> getMarketIds() {
        List<String> marketIds = new ArrayList<>();

        marketIds.addAll(marketConfigManager.getChestConfigs().keySet());
        marketIds.addAll(marketConfigManager.getMerchantConfigs().keySet());

        return marketIds;
    }

    /**
     * Gets the time when a market will next refresh.
     * @param marketId The market id to get the refresh time for.
     * @return A {@link Time} object or null if the market id is not known to the plugin.
     */
    public @Nullable Time getRefreshTime(@NotNull String marketId) {
        @Nullable MarketData marketData = marketDataManager.getMarketData(marketId);
        if(marketData == null) return null;

        return TimeUtil.millisToTime(marketData.getRefreshTime() - System.currentTimeMillis());
    }

    /**
     * Get the market name for the provided market id.
     * @param marketId The market's id.
     * @return The name of the market.
     */
    public @Nullable String getMarketName(@NotNull String marketId) {
        @Nullable MarketData marketData = marketDataManager.getMarketData(marketId);
        if(marketData == null) return null;

        return marketData.getMarketName();
    }
}
