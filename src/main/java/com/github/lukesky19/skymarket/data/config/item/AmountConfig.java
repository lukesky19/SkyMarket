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
package com.github.lukesky19.skymarket.data.config.item;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * This record contains the configuration for configuring or calculating an {@link ItemStack}'s amount.
 * @param fixed The fixed amount of the {@link ItemStack}.
 * @param min The minimum amount. Used with the maximum amount to calculate the {@link ItemStack}'s amount.
 * @param max The maximum amount. Used with the minimum amount to calculate the {@link ItemStack}'s amount.
 */
@ConfigSerializable
public record AmountConfig(@Nullable Integer fixed, @Nullable Integer min, @Nullable Integer max) {}