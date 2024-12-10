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
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.configuration.record.Locale;

import java.io.File;
import java.nio.file.Path;

import com.github.lukesky19.skymarket.configuration.record.Settings;

public class LocaleLoader {
    final SkyMarket skyMarket;
    final SettingsLoader settingsLoader;
    private Locale locale;
    private final Locale defaultLocale = new Locale(
            "1.1.0",
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
            "<red>One or more of the arguments sent is not recognized.</red>",
            "<white>The black market has been refreshed.</white>",
            "<red>Unable to open the market due to a configuration issue.</red>",
            "<white>The market will be refreshed at <yellow><time></yellow>.</white>");

    public LocaleLoader(SkyMarket skyMarket, SettingsLoader settingsLoader) {
        this.skyMarket = skyMarket;
        this.settingsLoader = settingsLoader;
    }

    public Locale getLocale() {
        if(locale == null) return defaultLocale;

        return locale;
    }

    public void reload() {
        Settings settings = settingsLoader.getSettingsConfig();
        locale = null;

        if(settings == null) return;

        copyDefaultLocales();

        String localeString = settings.locale();
        Path path = Path.of(skyMarket.getDataFolder() + File.separator + "locale" + File.separator + (localeString + ".yml"));

        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            locale = loader.load().get(Locale.class);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }

        migrateLocale();
    }

    private void saveLocale(Locale newLocale) {
        Settings settings = settingsLoader.getSettingsConfig();
        if(settings == null) return;

        String localeString = settings.locale();
        Path path = Path.of(skyMarket.getDataFolder() + File.separator + "locale" + File.separator + (localeString + ".yml"));

        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        ConfigurationNode node = loader.createNode();

        try {
            node.set(newLocale);
            loader.save(node);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    private void copyDefaultLocales() {
        Path path = Path.of(skyMarket.getDataFolder() + File.separator + "locale" + File.separator + "en_US.yml");
        if (!path.toFile().exists()) {
            skyMarket.saveResource("locale/en_US.yml", false);
        }
    }

    private void migrateLocale() {
        switch(locale.configVersion()) {
            case "1.1.0" -> {
                // Current version, do nothing.
            }

            case null -> {
                // Version 1.0.0 -> 1.1.0
                Locale newLocale = new Locale(
                        "1.1.0",
                        locale.prefix(),
                        locale.noPermission(),
                        locale.configReload(),
                        locale.notEnoughItems(),
                        locale.insufficientFunds(),
                        locale.buySuccess(),
                        locale.sellSuccess(),
                        locale.unbuyable(),
                        locale.unsellable(),
                        locale.inGameOnly(),
                        locale.unknownArgument(),
                        "<white>The market has been refreshed.</white>",
                        "<red>Unable to open the market due to a configuration issue.</red>",
                        "<white>The market will be refreshed at <yellow><time></yellow>.</white>");

                locale = newLocale;

                saveLocale(newLocale);
            }

            default -> throw new IllegalStateException("Unexpected value: " + locale.configVersion());
        }
    }
}
