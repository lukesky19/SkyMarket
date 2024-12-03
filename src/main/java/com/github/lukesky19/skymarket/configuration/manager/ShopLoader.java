/*
    SkyMarket is a shop that rotates it's inventory after a set period of time.
    Copyright (C) 2024  lukeskywlker19

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
package com.github.lukesky19.skymarket.configuration.manager;

import com.github.lukesky19.skylib.config.ConfigurationUtility;
import com.github.lukesky19.skylib.format.FormatUtil;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.configuration.record.Gui;
import com.github.lukesky19.skymarket.gui.MarketGUI;

import java.io.File;
import java.nio.file.Path;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class ShopLoader {
    private final SkyMarket skyMarket;
    private final SettingsLoader settingsLoader;
    private final LocaleLoader localeLoader;
    private final ItemsLoader itemsLoader;
    MarketGUI marketGUI;
    Gui configuration;
    BukkitTask task;

    public ShopLoader(SkyMarket skyMarket, SettingsLoader settingsLoader, LocaleLoader localeLoader, ItemsLoader itemsLoader) {
        this.skyMarket = skyMarket;
        this.settingsLoader = settingsLoader;
        this.localeLoader = localeLoader;
        this.itemsLoader = itemsLoader;
    }

    public MarketGUI getMarketGUI() {
        return marketGUI;
    }

    public void reload() {
        closeShop();
        marketGUI = null;
        configuration = null;
        ComponentLogger logger = skyMarket.getComponentLogger();

        if(!skyMarket.isPluginEnabled()) {
            logger.error(FormatUtil.format("<red>The market GUI config cannot be loaded due to a previous plugin error.</red>"));
            logger.error(FormatUtil.format("<red>Please check your server's console.</red>"));
            return;
        }

        Path path = Path.of(skyMarket.getDataFolder() + File.separator + "market.yml");
        if (!path.toFile().exists()) {
            skyMarket.saveResource("market.yml", false);
        }

        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            configuration = loader.load().get(Gui.class);
        } catch (ConfigurateException e) {
            skyMarket.setPluginState(false);
            throw new RuntimeException(e);
        }

        refreshShop();
    }

    public void refreshShop() {
        closeShop();
        marketGUI = new MarketGUI(skyMarket, localeLoader, itemsLoader, configuration);
        restartRefreshTask();
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

        task = skyMarket.getServer().getScheduler().runTaskLater(skyMarket, this::refreshShop, settingsLoader.getSettingsConfig().refreshTimeSeconds() * 20L);
    }
}
