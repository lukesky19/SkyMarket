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
package com.github.lukesky19.skymarket.data.config.gui;

import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import com.github.lukesky19.skymarket.data.config.item.AmountConfig;
import com.github.lukesky19.skymarket.data.config.item.RandomEnchantConfig;
import com.github.lukesky19.skymarket.gui.MerchantMarketGUI;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This record contains the configuration to create a {@link MerchantMarketGUI}.
 * @param configVersion The version of the config file.
 * @param refreshTime The time between refreshes.
 * @param marketName The name of the market.
 * @param guiName The name to display inside the inventory GUI.
 * @param numOfTrades The number of trades to display inside the GUI.
 * @param trades A {@link List} of {@link Trade}.
 */
@ConfigSerializable
public record MerchantConfig(
        @Nullable String configVersion,
        @Nullable String refreshTime,
        @Nullable String marketName,
        @Nullable String guiName,
        int numOfTrades,
        @NotNull List<Trade> trades) {
    /**
     * This record contains the configuration for a {@link MerchantRecipe}.
     * @param limit The number of times this trade can be used.
     * @param input1 The {@link Item} config for the first input item.
     * @param input2 The {@link Item} config for the second input item.
     * @param output The {@link Item} config for the output item.
     */
    @ConfigSerializable
    public record Trade(
            @NotNull Integer limit,
            @NotNull Item input1,
            @NotNull Item input2,
            @NotNull Item output) {}
    /**
     * This record contains the configuration to create an {@link ItemStack}.
     * @param item The base {@link ItemStackConfig}.
     * @param amount The {@link AmountConfig} for the item.
     * @param randomEnchants The {@link RandomEnchantConfig} for the item.
     */
    @ConfigSerializable
    public record Item(
            @NotNull ItemStackConfig item,
            @NotNull AmountConfig amount,
            @NotNull RandomEnchantConfig randomEnchants) {}
}
