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
package com.github.lukesky19.skymarket.configuration.record;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public record Locale(
        String prefix,
        String noPermission,
        String configReload,
        String notEnoughItems,
        String insufficientFunds,
        String buySuccess,
        String sellSuccess,
        String unbuyable,
        String unsellable,
        String inGameOnly,
        String unknownArgument) {
}
