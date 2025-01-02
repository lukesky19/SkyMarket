package com.github.lukesky19.skymarket.manager;

import com.github.lukesky19.skylib.format.FormatUtil;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.configuration.manager.ItemsLoader;
import com.github.lukesky19.skymarket.configuration.manager.LocaleLoader;
import com.github.lukesky19.skymarket.configuration.manager.MarketLoader;
import com.github.lukesky19.skymarket.configuration.manager.SettingsLoader;
import com.github.lukesky19.skymarket.configuration.record.Locale;
import com.github.lukesky19.skymarket.configuration.record.Settings;
import com.github.lukesky19.skymarket.gui.MarketGUI;
import com.google.common.collect.ImmutableList;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.CheckForNull;

public class MarketManager {
    private final SkyMarket skyMarket;
    private final SettingsLoader settingsLoader;
    private final LocaleLoader localeLoader;
    private final ItemsLoader itemsLoader;
    private final MarketLoader marketLoader;
    private MarketGUI marketGUI;
    private BukkitTask task;

    private long resetTime;

    public MarketManager(SkyMarket skyMarket, SettingsLoader settingsLoader, LocaleLoader localeLoader, ItemsLoader itemsLoader, MarketLoader marketLoader) {
        this.skyMarket = skyMarket;
        this.settingsLoader = settingsLoader;
        this.localeLoader = localeLoader;
        this.itemsLoader = itemsLoader;
        this.marketLoader = marketLoader;
    }

    @CheckForNull
    public MarketGUI getMarketGUI() {
        return marketGUI;
    }

    public long getResetTime() {
        return resetTime;
    }

    public void refreshMarket() {
        final Locale locale = localeLoader.getLocale();

        closeShop();
        marketGUI = new MarketGUI(skyMarket, localeLoader, itemsLoader, marketLoader.getConfiguration());

        for(Player player : ImmutableList.copyOf(skyMarket.getServer().getOnlinePlayers())) {
            if(player.isOnline() && player.isConnected()) {
                player.sendMessage(FormatUtil.format(locale.prefix() + locale.marketRefreshed()));
            }
        }

        restartRefreshTask();
        resetTime = System.currentTimeMillis() + FormatUtil.stringToMillis("6h");
    }

    private void closeShop() {
        for(Player player : skyMarket.getServer().getOnlinePlayers()) {
            if(player.getOpenInventory() instanceof MarketGUI gui) {
                gui.closeInventory(skyMarket, player);
            }
        }
    }

    private void restartRefreshTask() {
        if(task != null && !task.isCancelled()) {
            task.cancel();
        }

        Settings settings = settingsLoader.getSettingsConfig();
        if(settings == null) return;

        task = skyMarket.getServer().getScheduler().runTaskLater(skyMarket, this::refreshMarket, settings.refreshTimeSeconds() * 20L);
    }
}
