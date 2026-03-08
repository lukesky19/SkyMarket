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
package com.github.lukesky19.skymarket.market;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.time.Time;
import com.github.lukesky19.skylib.api.time.TimeUtil;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.locale.LocaleManager;
import com.github.lukesky19.skymarket.locale.Locale;
import com.github.lukesky19.skymarket.data.market.MerchantMarketData;
import com.github.lukesky19.skymarket.interfaces.IMarketData;
import com.github.lukesky19.skymarket.data.market.ChestMarketData;
import com.github.lukesky19.skymarket.gui.GUIManager;
import com.github.lukesky19.skymarket.transaction.TransactionManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;

/**
 * This class contains methods to manage markets.
 */
public class MarketManager {
    private final @NotNull SkyMarket skyMarket;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull GUIManager guiManager;
    private final @NonNull TransactionManager transactionManager;
    private final @NotNull MarketConfigManager marketConfigManager;
    private final @NotNull MarketDataManager marketDataManager;

    /**
     * Default Constructor. You should use {@link MarketManager#MarketManager(SkyMarket, LocaleManager, GUIManager, TransactionManager, MarketConfigManager, MarketDataManager)} instead.
     * @deprecated You should use {@link MarketManager#MarketManager(SkyMarket, LocaleManager, GUIManager, TransactionManager, MarketConfigManager, MarketDataManager)} instead.
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
     * @param transactionManager A {@link TransactionManager} instance.
     * @param marketConfigManager A {@link MarketConfigManager} instance.
     * @param marketDataManager A {@link MarketDataManager} instance.
     */
    public MarketManager(
            @NotNull SkyMarket skyMarket,
            @NotNull LocaleManager localeManager,
            @NotNull GUIManager guiManager,
            @NonNull TransactionManager transactionManager,
            @NotNull MarketConfigManager marketConfigManager,
            @NotNull MarketDataManager marketDataManager) {
        this.skyMarket = skyMarket;
        this.localeManager = localeManager;
        this.guiManager = guiManager;
        this.transactionManager = transactionManager;
        this.marketConfigManager = marketConfigManager;
        this.marketDataManager = marketDataManager;
    }

    /**
     * This should only be run on plugin load or reload. To refresh markets, use {@link #refreshMarkets()} or {@link #refreshMarket(String)}
     */
    public void reload() {
        marketDataManager.clearMarketData();

        marketConfigManager.getChestConfigs()
                .forEach((marketId, chestConfig) -> {
            // Config is validated on load so these will never be null.
            assert chestConfig.marketName() != null;
            assert chestConfig.guiData().guiType() != null;
            assert chestConfig.guiData().guiName() != null;

            IMarketData marketData = new ChestMarketData(
                    skyMarket,
                    localeManager,
                    guiManager,
                    transactionManager,
                    chestConfig,
                    marketId,
                    chestConfig.guiData().guiType(),
                    chestConfig.guiData().guiName());

            // Refresh the market
            marketData.refreshMarket();

            // Store the MarketData in MarketDataManager
            marketDataManager.setMarketData(marketId, marketData);
        });

        marketConfigManager.getMerchantConfigs().forEach((marketId, merchantConfig) -> {
            // Config is validated on load so this will never be null.
            assert merchantConfig.marketName() != null;
            assert merchantConfig.guiName() != null;
            IMarketData marketData = new MerchantMarketData(
                    skyMarket,
                    localeManager,
                    guiManager,
                    merchantConfig,
                    marketId,
                    merchantConfig.guiName());

            // Refresh the market
            marketData.refreshMarket();

            // Store the MarketData in MarketDataManager
            marketDataManager.setMarketData(marketId, marketData);
        });
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
     * Refreshes a specific market based on the market id.
     * @param marketId The id of the market to refresh.
     * @return true if the market refreshed successfully, false if not.
     */
    public boolean refreshMarket(@NotNull String marketId) {
        // Get the market data for the market id
        IMarketData marketData = marketDataManager.getMarketData(marketId);
        if(marketData == null) return false;

        // Close any open GUIs for the market id
        guiManager.closeGUIsByMarketId(marketId);

        // Refresh Market
        return marketData.refreshMarket();
    }

    /**
     * Opens a market based on the market id.
     * @param marketId The market id of the market to open.
     * @param player The {@link Player} who wants to view the market.
     * @return true if the market was opened, false if not.
     */
    public boolean openMarket(@NotNull String marketId, @NotNull Player player) {
        Locale locale = localeManager.getLocale();

        IMarketData marketData = marketDataManager.getMarketData(marketId);
        if(marketData == null) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.invalidMarketId()));
            return false;
        }

        return marketData.openMarket(player);
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
        @Nullable IMarketData marketData = marketDataManager.getMarketData(marketId);
        if(marketData == null) return null;

        return TimeUtil.millisToTime(marketData.getRefreshTime() - System.currentTimeMillis());
    }
}