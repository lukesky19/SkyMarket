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
import com.github.lukesky19.skymarket.configuration.record.Gui;

import java.io.File;
import java.nio.file.Path;

import javax.annotation.CheckForNull;

public class MarketLoader {
    private final SkyMarket skyMarket;
    private Gui configuration;

    public MarketLoader(SkyMarket skyMarket) {
        this.skyMarket = skyMarket;
    }

    @CheckForNull
    public Gui getConfiguration() {
        return configuration;
    }

    public void reload() {
        configuration = null;

        Path path = Path.of(skyMarket.getDataFolder() + File.separator + "market.yml");
        if (!path.toFile().exists()) {
            skyMarket.saveResource("market.yml", false);
        }

        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            configuration = loader.load().get(Gui.class);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }
}
