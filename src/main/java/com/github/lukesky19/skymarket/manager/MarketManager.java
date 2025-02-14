package com.github.lukesky19.skymarket.manager;

import com.github.lukesky19.skylib.format.FormatUtil;
import com.github.lukesky19.skylib.gui.GUIType;
import com.github.lukesky19.skylib.gui.GUIButton;
import com.github.lukesky19.skylib.record.Time;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.configuration.loader.LocaleLoader;
import com.github.lukesky19.skymarket.configuration.loader.MarketLoader;
import com.github.lukesky19.skymarket.configuration.record.ActiveMerchant;
import com.github.lukesky19.skymarket.configuration.record.Locale;
import com.github.lukesky19.skymarket.configuration.record.ActiveMarket;
import com.github.lukesky19.skymarket.configuration.record.gui.Chest;
import com.github.lukesky19.skymarket.configuration.record.gui.Merchant;
import com.github.lukesky19.skymarket.gui.MarketGUI;
import com.github.lukesky19.skymarket.gui.MerchantGUI;
import com.github.lukesky19.skymarket.util.PluginUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This class manages all open markets and their data.
 */
public class MarketManager {
    private final SkyMarket skyMarket;
    private final LocaleLoader localeLoader;
    private final MarketLoader marketLoader;

    private final HashMap<UUID, MarketGUI> openMarketGUIs = new HashMap<>();
    private final HashMap<UUID, MerchantGUI> openMerchantGUIs = new HashMap<>();

    private final HashMap<String, ActiveMarket> activeMarkets = new HashMap<>();
    private final HashMap<String, ActiveMerchant> activeMerchants = new HashMap<>();

    public MarketManager(SkyMarket skyMarket, LocaleLoader localeLoader, MarketLoader marketLoader) {
        this.skyMarket = skyMarket;
        this.localeLoader = localeLoader;
        this.marketLoader = marketLoader;
    }

    /**
     * Reloads all markets.
     */
    public void reload() {
        closeMarkets(false);

        refreshMarkets();
    }

    /**
     * Gets the MarketGUI that the given UUID has open.
     * @param uuid The UUID of the player.
     * @return A MarketGUI or null if none is open.
     */
    @Nullable
    public MarketGUI getMarketGUI(UUID uuid) {
        return openMarketGUIs.get(uuid);
    }

    /**
     * Gets the MerchantGUI that the given UUID has open.
     * @param uuid The UUID of the player.
     * @return A MerchantGUI or null if none is open.
     */
    @Nullable
    public MerchantGUI getMerchantGUI(UUID uuid) {
        return openMerchantGUIs.get(uuid);
    }

    /**
     * Refreshes a specific market based on the market id.
     * @param marketId The id of the market to refresh.
     * @return true if the market refreshed successfully, false if not.
     */
    public boolean refreshMarket(String marketId) {
        Locale locale = localeLoader.getLocale();

        if(activeMarkets.containsKey(marketId)) {
            ActiveMarket market = activeMarkets.get(marketId);
            BukkitTask task = market.refreshTask();

            if(!task.isCancelled()) {
                task.cancel();
            }

            Chest chestConfig = marketLoader.getChestConfig(marketId);
            if(chestConfig == null) return false;

            if(chestConfig.refreshTime() != null) {
                GUIType type = GUIType.getType(chestConfig.guiData().guiType());
                if(type == null) return false;

                String marketName = chestConfig.marketName();
                if(marketName == null) return false;

                long resetTime = System.currentTimeMillis() + FormatUtil.stringToMillis(chestConfig.refreshTime());
                long seconds = FormatUtil.stringToMillis(chestConfig.refreshTime()) / 1000;

                BukkitTask refreshTask = skyMarket.getServer().getScheduler().runTaskLater(skyMarket, () -> refreshMarket(marketId), seconds * 20L);

                HashMap<Integer, GUIButton> buttonMap = PluginUtils.getButtons(skyMarket, this, locale, type, chestConfig, marketId);

                List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("market_name", marketName));

                skyMarket.getServer().getOnlinePlayers().forEach(player -> {
                    if (player.isOnline() && player.isConnected()) {
                        player.sendMessage(FormatUtil.format(locale.prefix() + locale.marketRefreshed(), placeholders));
                    }
                });

                activeMarkets.put(marketId, new ActiveMarket(marketName, buttonMap, new HashMap<>(), new HashMap<>(), refreshTask, resetTime));

                return true;
            }
        } else if(activeMerchants.containsKey(marketId)) {
            ActiveMerchant merchant = activeMerchants.get(marketId);
            BukkitTask task = merchant.refreshTask();

            if(!task.isCancelled()) {
                task.cancel();
            }

            Merchant merchantConfig = marketLoader.getMerchantConfig(marketId);
            if(merchantConfig == null) return false;

            if(merchantConfig.refreshTime() != null) {
                String marketName = merchantConfig.marketName();
                if(marketName == null) return false;

                long resetTime = System.currentTimeMillis() + FormatUtil.stringToMillis(merchantConfig.refreshTime());
                long seconds = FormatUtil.stringToMillis(merchantConfig.refreshTime()) / 1000;

                BukkitTask refreshTask = skyMarket.getServer().getScheduler().runTaskLater(skyMarket, () -> refreshMarket(marketId), seconds * 20L);

                List<MerchantRecipe> tradeList = PluginUtils.getTrades(skyMarket, merchantConfig);

                List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("market_name", marketName));

                skyMarket.getServer().getOnlinePlayers().forEach(player -> {
                    if (player.isOnline() && player.isConnected()) {
                        player.sendMessage(FormatUtil.format(locale.prefix() + locale.marketRefreshed(), placeholders));
                    }
                });

                activeMerchants.put(marketId, new ActiveMerchant(marketName, tradeList, new HashMap<>(), refreshTask, resetTime));

                return true;
            }
        }

        return false;
    }

    /**
     * Refreshes all markets.
     */
    public void refreshMarkets() {
        Locale locale = localeLoader.getLocale();

        for(ActiveMarket activeMarket : activeMarkets.values()) {
            BukkitTask task = activeMarket.refreshTask();
            if(task.isCancelled()) {
                task.cancel();
            }
        }

        for(ActiveMerchant activeMerchant : activeMerchants.values()) {
            BukkitTask task = activeMerchant.refreshTask();
            if(task.isCancelled()) {
                task.cancel();
            }
        }

        activeMarkets.clear();
        activeMerchants.clear();

        marketLoader.getChestGuiConfigurations().forEach((marketId, chestConfig) -> {
            if(chestConfig.refreshTime() != null) {
                GUIType type = GUIType.getType(chestConfig.guiData().guiType());
                if(type == null) return;

                String marketName = chestConfig.marketName();
                if(marketName == null) return;

                long resetTime = System.currentTimeMillis() + FormatUtil.stringToMillis(chestConfig.refreshTime());
                long seconds = FormatUtil.stringToMillis(chestConfig.refreshTime()) / 1000;

                BukkitTask refreshTask = skyMarket.getServer().getScheduler().runTaskLater(skyMarket, () -> refreshMarket(marketId), seconds * 20L);

                HashMap<Integer, GUIButton> buttonMap = PluginUtils.getButtons(skyMarket, this, locale, type, chestConfig, marketId);

                List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("market_name", marketName));

                skyMarket.getServer().getOnlinePlayers().forEach(player -> {
                    if (player.isOnline() && player.isConnected()) {
                        player.sendMessage(FormatUtil.format(locale.prefix() + locale.marketRefreshed(), placeholders));
                    }
                });

                activeMarkets.put(marketId, new ActiveMarket(marketName, buttonMap, new HashMap<>(), new HashMap<>(), refreshTask, resetTime));
            }
        });

        marketLoader.getMerchantGuiConfigurations().forEach((marketId, merchantConfig) -> {
            if(merchantConfig.refreshTime() != null) {
                String marketName = merchantConfig.marketName();
                if(marketName == null) return;

                long resetTime = System.currentTimeMillis() + FormatUtil.stringToMillis(merchantConfig.refreshTime());
                long seconds = FormatUtil.stringToMillis(merchantConfig.refreshTime()) / 1000;

                BukkitTask refreshTask = skyMarket.getServer().getScheduler().runTaskLater(skyMarket, () -> refreshMarket(marketId), seconds * 20L);

                List<MerchantRecipe> tradeList = PluginUtils.getTrades(skyMarket, merchantConfig);

                List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("market_name", marketName));

                skyMarket.getServer().getOnlinePlayers().forEach(player -> {
                    if (player.isOnline() && player.isConnected()) {
                        player.sendMessage(FormatUtil.format(locale.prefix() + locale.marketRefreshed(), placeholders));
                    }
                });

                activeMerchants.put(marketId, new ActiveMerchant(marketName, tradeList, new HashMap<>(), refreshTask, resetTime));
            }
        });
    }

    /**
     * Closes all open markets.
     * @param onDisable If the closure is occuring on plugin disable or not.
     */
    public void closeMarkets(boolean onDisable) {
        if(!onDisable) {
            openMarketGUIs.forEach((uuid, marketGUI) -> {
                Player player = skyMarket.getServer().getPlayer(uuid);
                if(player != null && player.isOnline() && player.isConnected()) {
                    marketGUI.closeInventory(skyMarket, player);
                }
            });

            openMerchantGUIs.forEach((uuid, merchantGUI) -> {
                Player player = skyMarket.getServer().getPlayer(uuid);
                if(player != null && player.isOnline() && player.isConnected()) {
                    merchantGUI.closeInventory(skyMarket, player);
                }
            });
        } else {
            openMarketGUIs.forEach((uuid, marketGUI) -> {
                Player player = skyMarket.getServer().getPlayer(uuid);
                if(player != null && player.isOnline() && player.isConnected()) {
                    player.closeInventory();
                }
            });

            openMerchantGUIs.forEach((uuid, merchantGUI) -> {
                Player player = skyMarket.getServer().getPlayer(uuid);
                if(player != null && player.isOnline() && player.isConnected()) {
                    player.closeInventory();
                }
            });
        }

        openMarketGUIs.clear();
        openMerchantGUIs.clear();
    }

    /**
     * Opens a market based on the market id.
     * @param marketId The market id of the gui to open.
     * @param sender The CommandSender/Player who wants to open the GUI.
     * @return true if the market was opened, false if not.
     */
    public boolean openMarket(String marketId, CommandSender sender) {
        Locale locale = localeLoader.getLocale();
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        if(activeMarkets.containsKey(marketId)) {
            ActiveMarket activeMarket = activeMarkets.get(marketId);
            Chest chestConfig = marketLoader.getChestConfig(marketId);

            if(chestConfig != null) {
                MarketGUI marketGUI = new MarketGUI(chestConfig, activeMarket.buttons(), player, this);

                openMarketGUIs.put(player.getUniqueId(), marketGUI);

                marketGUI.openInventory(skyMarket, player);

                return true;
            } else {
                sender.sendMessage(FormatUtil.format(locale.prefix() + locale.invalidMarketId()));
                return false;
            }
        } else if(activeMerchants.containsKey(marketId)) {
            ActiveMerchant activeMerchant = activeMerchants.get(marketId);
            Merchant merchantConfig = marketLoader.getMerchantConfig(marketId);

            if (merchantConfig != null && merchantConfig.guiName() != null) {
                List<MerchantRecipe> trades = activeMerchant.playerTrades().getOrDefault(uuid, activeMerchant.trades());
                MerchantGUI merchantGUI = new MerchantGUI(marketId, trades, merchantConfig.guiName(), player, this);

                openMerchantGUIs.put(player.getUniqueId(), merchantGUI);

                merchantGUI.openInventory(skyMarket, player);

                return true;
            } else {
                sender.sendMessage(FormatUtil.format(locale.prefix() + locale.invalidMarketId()));
                return false;
            }
        } else {
            sender.sendMessage(FormatUtil.format(locale.prefix() + locale.invalidMarketId()));
            return false;
        }
    }

    /**
     * Gets the time when a market will next refresh.
     * @param marketId The market id to get the refresh time for.
     * @return A {@link Time} object or null if the market id is not known to the plugin.
     */
    @Nullable
    public Time getRefreshTime(String marketId) {
        if(activeMarkets.containsKey(marketId)) {
            long time = activeMarkets.get(marketId).resetTime();

            return FormatUtil.millisToTime(time - System.currentTimeMillis());
        } else if(activeMerchants.containsKey(marketId)) {
            long time = activeMerchants.get(marketId).resetTime();

            return FormatUtil.millisToTime(time - System.currentTimeMillis());
        } else {
            return null;
        }
    }

    /**
     * Gets a list of all known market ids.
     * @return A List of Strings for all known market ids
     */
    public List<String> getMarketIds() {
        List<String> list = new ArrayList<>();

        list.addAll(activeMarkets.keySet());
        list.addAll(activeMerchants.keySet());

        return list;
    }

    /**
     * Gets the market name for a specific market id.
     * @param marketId The market id to get the market name for.
     * @return A String representing the market name or null if the market id is not known to the plugin.
     */
    @Nullable
    public String getMarketName(String marketId) {
        if(activeMarkets.containsKey(marketId)) {
            return activeMarkets.get(marketId).marketName();
        } else if(activeMerchants.containsKey(marketId)) {
            return activeMerchants.get(marketId).marketName();
        } else {
            return null;
        }
    }

    /**
     * Adds 1 to the per-player button buy limit for the given market id, uuid, and slot.
     * @param marketId The market id the button was clicked
     * @param uuid The UUID of the player who clicked the button.
     * @param slot The slot where the button was clicked.
     */
    public void addToBuyButtonLimit(String marketId, UUID uuid, int slot) {
        if(activeMarkets.containsKey(marketId)) {
            ActiveMarket currentActiveMarket = activeMarkets.get(marketId);
            HashMap<UUID, HashMap<Integer, Integer>> uuidMap = currentActiveMarket.buySlotLimits();
            HashMap<Integer, Integer> slotMap = uuidMap.getOrDefault(uuid, new HashMap<>());

            slotMap.put(slot, slotMap.getOrDefault(slot, 0) + 1);

            uuidMap.put(uuid, slotMap);

            ActiveMarket updateActiveMarket = new ActiveMarket(currentActiveMarket.marketName(), currentActiveMarket.buttons(), uuidMap, currentActiveMarket.sellSlotLimits(), currentActiveMarket.refreshTask(), currentActiveMarket.resetTime());

            activeMarkets.put(marketId, updateActiveMarket);
        } else {
            throw new RuntimeException("Unable to add purchase to button limit because there is no market with market id " + marketId);
        }
    }

    /**
     * Adds 1 to the per-player button sell limit for the given market id, uuid, and slot.
     * @param marketId The market id the button was clicked
     * @param uuid The UUID of the player who clicked the button.
     * @param slot The slot where the button was clicked.
     */
    public void addToSellButtonLimit(String marketId, UUID uuid, int slot) {
        if(activeMarkets.containsKey(marketId)) {
            ActiveMarket currentActiveMarket = activeMarkets.get(marketId);
            HashMap<UUID, HashMap<Integer, Integer>> uuidMap = currentActiveMarket.sellSlotLimits();
            HashMap<Integer, Integer> slotMap = uuidMap.getOrDefault(uuid, new HashMap<>());

            slotMap.put(slot, slotMap.getOrDefault(slot, 0) + 1);

            uuidMap.put(uuid, slotMap);

            ActiveMarket updateActiveMarket = new ActiveMarket(currentActiveMarket.marketName(), currentActiveMarket.buttons(), currentActiveMarket.buySlotLimits(), uuidMap, currentActiveMarket.refreshTask(), currentActiveMarket.resetTime());

            activeMarkets.put(marketId, updateActiveMarket);
        } else {
            throw new RuntimeException("Unable to add purchase to button limit because there is no market with market id " + marketId);
        }
    }

    /**
     * Gets the per-player limits associated with a slot inside the Inventory for buying.
     * @param marketId The market id to get the button buy limits for.
     * @param uuid The UUID of the player to get the button buy limits for.
     * @return A HashMap representing a slot and the buy limit for that player.
     */
    @Nullable
    public HashMap<Integer, Integer> getBuyButtonLimits(String marketId, UUID uuid) {
        return activeMarkets.get(marketId).buySlotLimits().getOrDefault(uuid, new HashMap<>());
    }

    /**
     * Gets the per-player limits associated with a slot inside the Inventory for selling.
     * @param marketId The market id to get the button sell limits for.
     * @param uuid The UUID of the player to get the button sell limits for.
     * @return A HashMap representing a slot and the sell limit for that player.
     */
    @Nullable
    public HashMap<Integer, Integer> getSellButtonLimits(String marketId, UUID uuid) {
        return activeMarkets.get(marketId).sellSlotLimits().get(uuid);
    }

    /**
     * Stores the current trades for the player so they are re-populate once they reopen the Merchant market.
     * @param marketId The market id the player's trades belong to.
     * @param uuid The UUID of the player.
     * @param trades A List of MerchantRecipe that represents the current trades.
     */
    public void updatePlayerTrades(String marketId, UUID uuid, List<MerchantRecipe> trades) {
        if(activeMerchants.containsKey(marketId)) {
            ActiveMerchant currentActiveMerchant = activeMerchants.get(marketId);

            currentActiveMerchant.playerTrades().put(uuid, trades);
        }
    }

    /**
     * Removes the player's active GUI from being tracked. Used when the GUI is closed by the player or server.
     * @param uuid The UUID of the player whose GUI was closed.
     */
    public void removeActiveGui(UUID uuid) {
        openMarketGUIs.remove(uuid);
        openMerchantGUIs.remove(uuid);
    }
}
