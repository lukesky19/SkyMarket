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
package com.github.lukesky19.skymarket.configuration;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.data.config.Locale;

import java.io.File;
import java.nio.file.Path;

import com.github.lukesky19.skymarket.data.config.Settings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class handles the management of the locale configuration.
 */
public class LocaleManager {
    private final @NotNull SkyMarket skyMarket;
    private final @NotNull SettingsManager settingsManager;
    private @Nullable Locale locale;
    private final @NotNull Locale DEFAULT_LOCALE = new Locale(
            "2.0.0.0",
            "<gold><bold>SkyMarket</bold></gold><gray> ▪ </gray>",
            "<aqua>Configuration files have been reloaded.</aqua>",
            "<red>You do not have enough items to sell.</red>",
            "<red>Insufficient funds.</red>",
            "<red>You do not have enough items to trade.</red>",
            "<white>Purchased <yellow><item></yellow> for <yellow><price></yellow>. Balance: <yellow><bal></yellow></white>",
            "<white>Sold <yellow><item></yellow> for <yellow><price></yellow>. Balance: <yellow><bal></yellow></white>",
            "<red>This item is not able to be purchased.</red>",
            "<red>This item is not able to be sold.</red>",
            "<red>You have reached the purchase limit of this item.</red>",
            "<red>You have reached the sell limit of this item.</red>",
            "<white>The <yellow><market_name></yellow> has been refreshed.</white>",
            "<white>The market will be refreshed in <yellow><time></yellow>.</white>",
            "<red>There is no market with this id.</red>",
            "<yellow><item_name> <white>x</white><item_amount></yellow>",
            "<red>Unable to open this GUI because of a configuration error.</red>");

    /**
     * Constructor
     * @param skyMarket A {@link SkyMarket} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     */
    public LocaleManager(@NotNull SkyMarket skyMarket, @NotNull SettingsManager settingsManager) {
        this.skyMarket = skyMarket;
        this.settingsManager = settingsManager;
    }

    /**
     * Gets the plugin's {@link Locale}.
     * Will return a default copy of the configuration if the user-configured one failed to load.
     * @return The plugin's {@link Locale}
     */
    public @NotNull Locale getLocale() {
        if(locale == null) return DEFAULT_LOCALE;

        return locale;
    }

    /**
     * Reloads the plugin's locale.
     */
    public void reload() {
        Settings settings = settingsManager.getSettingsConfig();
        if(settings == null) return;
        locale = null;

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

    /**
     * Saves the plugins locale to a file on the disk.
     * @param newLocale The locale to save.
     */
    private void saveLocale(Locale newLocale) {
        Settings settings = settingsManager.getSettingsConfig();
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

    /**
     * Copies the default locale files bundled with the plugin to the disk.
     */
    private void copyDefaultLocales() {
        Path path = Path.of(skyMarket.getDataFolder() + File.separator + "locale" + File.separator + "en_US.yml");
        if (!path.toFile().exists()) {
            skyMarket.saveResource("locale/en_US.yml", false);
        }
    }

    /**
     * Migrates the locale configuration.
     */
    private void migrateLocale() {
        if(locale == null) return;

        switch(locale.configVersion()) {
            case "2.0.0.0" -> {
                // Current version, do nothing.
            }

            case null, default -> {
                skyMarket.getComponentLogger().error(AdventureUtil.serialize("<red>You need to migrate your locale to the new version."));
                skyMarket.getComponentLogger().error(AdventureUtil.serialize("<red>This happens from using a locale version older than 2.0.0.0."));
                locale = null;
            }
        }
    }
}
