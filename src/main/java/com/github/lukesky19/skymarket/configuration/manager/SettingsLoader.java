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

import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.configuration.record.Settings;
import com.github.lukesky19.skymarket.util.ConfigurationUtility;

import java.io.File;
import java.nio.file.Path;

import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

public class SettingsLoader {
    final SkyMarket skyMarket;
    final ConfigurationUtility configurationUtility;
    Settings settingsConfig;

    public SettingsLoader(SkyMarket skyMarket, ConfigurationUtility configurationUtility) {
        this.skyMarket = skyMarket;
        this.configurationUtility = configurationUtility;
    }

    public Settings getSettingsConfig() {
        return settingsConfig;
    }

    public void reload() {
        settingsConfig = null;
        Path path = Path.of(skyMarket.getDataFolder() + File.separator + "settings.yml");
        if(!path.toFile().exists()) {
            skyMarket.saveResource("settings.yml", false);
        }

        YamlConfigurationLoader loader = configurationUtility.getYamlConfigurationLoader(path);
        try {
            settingsConfig = loader.load().get(Settings.class);
        } catch (ConfigurateException e) {
            skyMarket.setPluginState(false);
            throw new RuntimeException(e);
        }
    }
}
