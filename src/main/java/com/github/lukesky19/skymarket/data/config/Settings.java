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
package com.github.lukesky19.skymarket.data.config;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * This record contains the plugin's settings.
 * @param configVersion The version of the config.
 * @param locale The plugin's locale.
 * @param aliases The {@link List} of {@link Alias} to register commands with.
 */
@ConfigSerializable
public record Settings(@Nullable String configVersion, @Nullable  String locale, @NotNull List<Alias> aliases) {
    /**
     * This record contains the information required to register alias commands.
     * @param alias The name of the command.
     * @param marketId The market id to open for this command.
     */
    @ConfigSerializable
    public record Alias(@Nullable String alias, @Nullable String marketId) {}
}

