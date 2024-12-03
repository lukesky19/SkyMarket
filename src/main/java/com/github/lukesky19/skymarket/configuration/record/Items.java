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

import java.util.LinkedHashMap;
import java.util.List;

@ConfigSerializable
public record Items(LinkedHashMap<Integer, Entry> items) {
    @ConfigSerializable
    public record Entry(
            String type,
            Item item,
            Prices prices,
            Commands commands) {}

    @ConfigSerializable
    public record Item(
            String material,
            String name,
            List<String> lore) {}

    @ConfigSerializable
    public record Prices(
            Double buyPriceMin,
            Double buyPriceMax,
            Double sellPriceMin,
            Double sellPriceMax) { }

    @ConfigSerializable
    public record Commands(
            List<String> buyCommands,
            List<String> sellCommands) {}
}
