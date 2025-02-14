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
package com.github.lukesky19.skymarket.configuration.loader;

import com.github.lukesky19.skylib.config.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.configuration.record.Settings;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;

/**
 * This class manages the loading of the plugin's settings.
 */
public class SettingsLoader {
    private final SkyMarket skyMarket;
    private Settings settingsConfig;

    /**
     * Constructor
     * @param skyMarket A SkyMarket plugin instance.
     */
    public SettingsLoader(SkyMarket skyMarket) {
        this.skyMarket = skyMarket;
    }

    /**
     * Gets the plugin's configuration.
     * @return A Settings object representing the plugin settings, or null if it failed to load.
     */
    @Nullable
    public Settings getSettingsConfig() {
        return settingsConfig;
    }

    /**
     * Reloads the plugin's settings.
     */
    public void reload() {
        settingsConfig = null;
        Path path = Path.of(skyMarket.getDataFolder() + File.separator + "settings.yml");
        if(!path.toFile().exists()) {
            skyMarket.saveResource("settings.yml", false);
        }

        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            settingsConfig = loader.load().get(Settings.class);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }
}
