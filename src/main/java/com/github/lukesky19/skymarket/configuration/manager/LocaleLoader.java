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
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.configuration.record.Locale;

import java.io.File;
import java.nio.file.Path;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class LocaleLoader {
    final SkyMarket skyMarket;
    final SettingsLoader settingsLoader;
    Locale locale;
    private final Locale defaultLocale = new Locale(
            "<gold><bold>SkyMarket</bold></gold><gray> ▪ </gray>",
            "<red>You do not have permission for this command.</red>",
            "<aqua>Configuration files have been reloaded.</aqua>",
            "<red>You do not have enough items to sell.</red>",
            "<red>Insufficient funds.</red>",
            "<white>Purchased <yellow><amount> <item></yellow> for <yellow><price></yellow>. Balance: <yellow><bal></yellow></white>",
            "<white>Sold <yellow><amount> <item></yellow> for <yellow><price></yellow>. Balance: <yellow><bal></yellow></white>",
            "<red>This item is not able to be purchased.</red>",
            "<red>This item is not able to be sold.</red>",
            "<red>This command can only be ran in-game.</red>",
            "<red>One or more of the arguments sent is not recognized.</red>");

    public LocaleLoader(SkyMarket skyMarket, SettingsLoader settingsLoader) {
        this.skyMarket = skyMarket;
        this.settingsLoader = settingsLoader;
    }

    public Locale getLocale() {
        return locale;
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public void reload() {
        locale = null;
        ComponentLogger logger = skyMarket.getComponentLogger();

        if(!skyMarket.isPluginEnabled()) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>The locale config cannot be loaded due to a previous plugin error.</red>"));
            logger.error(MiniMessage.miniMessage().deserialize("<red>Please check your server's console.</red>"));
            return;
        }

        copyDefaultLocales();

        String localeString = settingsLoader.getSettingsConfig().locale();
        Path path = Path.of(skyMarket.getDataFolder() + File.separator + "locale" + File.separator + (localeString + ".yml"));

        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            locale = loader.load().get(Locale.class);
        } catch (ConfigurateException e) {
            skyMarket.setPluginState(false);
            throw new RuntimeException(e);
        }
    }

    private void copyDefaultLocales() {
        Path path = Path.of(skyMarket.getDataFolder() + File.separator + "locale" + File.separator + "en_US.yml");
        if (!path.toFile().exists()) {
            skyMarket.saveResource("locale/en_US.yml", false);
        }
    }
}
