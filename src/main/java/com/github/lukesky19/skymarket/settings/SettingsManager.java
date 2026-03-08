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
package com.github.lukesky19.skymarket.settings;

import com.github.lukesky19.skylib.api.common.abstracts.config.SimpleConfigManager;
import com.github.lukesky19.skymarket.SkyMarket;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

/**
 * This class manages the plugin's settings.
 */
public class SettingsManager extends SimpleConfigManager<Settings> {
    /**
     * Constructor
     * @param skyMarket A {@link SkyMarket} instance.
     */
    public SettingsManager(@NonNull SkyMarket skyMarket) {
        super(skyMarket, Path.of(skyMarket.getDataFolder() + File.separator + "settings.yml"), Settings.class);
    }

    @Override
    public void saveBundledConfig() {
        plugin.saveResource("settings.yml", false);
    }

    @Override
    public @Nullable Settings migrateConfiguration(@NonNull Settings settings) {
        if(settings.version() == 0) {
            return new Settings(3, false, settings.locale(), settings.aliases());
        }

        return settings;
    }

    @Override
    public boolean validateConfiguration(@Nullable Settings configuration) {
        return configuration != null;
    }

    /**
     * Set the first-run configuration option to false.
     */
    public void setFirstRunFalse() {
        if(configuration == null) return;

        configuration = new Settings(configuration.version(), false, configuration.locale(), configuration.aliases());

        saveConfiguration(configuration);
    }
}