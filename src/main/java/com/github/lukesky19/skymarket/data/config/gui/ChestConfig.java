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

import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import com.github.lukesky19.skymarket.data.config.gui.button.ButtonConfig;
import com.github.lukesky19.skymarket.data.config.item.AmountConfig;
import com.github.lukesky19.skymarket.data.config.item.RandomEnchantConfig;
import com.github.lukesky19.skymarket.gui.ChestMarketGUI;
import com.github.lukesky19.skymarket.util.TransactionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This record contains the configuration to create a {@link ChestMarketGUI}.
 * @param configVersion The version of the config file.
 * @param refreshTime The time between refreshes.
 * @param marketName The name of the market.
 * @param guiData The {@link GuiData}.
 * @param items A {@link List} of {@link ItemConfig}.
 */
@ConfigSerializable
public record ChestConfig(
        @Nullable String configVersion,
        @Nullable String refreshTime,
        @Nullable String marketName,
        @NotNull GuiData guiData,
        @NotNull List<ItemConfig> items) {

    /**
     * This record contains the data for creating and decorating the GUI.
     * @param guiType The {@link GUIType}.
     * @param guiName The name to display inside the inventory GUI.
     * @param placeholderSlots A {@link List} of {@link Integer}s to place actual market items in.
     * @param dummyButtons A {@link List} of {@link ButtonConfig}s to display in the GUI.
     */
    @ConfigSerializable
    public record GuiData(
            @Nullable GUIType guiType,
            @Nullable String guiName,
            @NotNull ButtonConfig filler,
            @NotNull ButtonConfig exit,
            @NotNull List<Integer> placeholderSlots,
            @NotNull List<ButtonConfig> dummyButtons) {}

    /**
     * This record contains the configuration to replace placeholder slots with.
     * @param transactionType The {@link TransactionType}
     * @param displayItem The {@link ItemStackConfig} to display in the GUI.
     * @param transactionItem The {@link ItemStackConfig} that is given to the player for {@link TransactionType#ITEM}.
     * @param randomEnchants The {@link RandomEnchantConfig} to apply to the displayItem and transactionItem.
     * @param amount The {@link AmountConfig} to apply to the displayItem and transactionItem.
     * @param transactionName The transaction name to use in messages.
     * @param prices The {@link PriceConfig} for the item.
     * @param buyLimit The buy limit. Limits the amount that can be purchased for this item. Per-player.
     * @param sellLimit The sell limit. Limits the amount that can be sold for this item. Per-player.
     * @param buyCommands A {@link List} of {@link String}s to execute in console when the purchase for {@link TransactionType#COMMAND} is complete.
     * @param sellCommands A {@link List} of {@link String}s to execute in console when a sale completes for {@link TransactionType#COMMAND}
     */
    @ConfigSerializable
    public record ItemConfig(
            @Nullable TransactionType transactionType,
            @NotNull ItemStackConfig displayItem,
            @NotNull ItemStackConfig transactionItem,
            @NotNull AmountConfig amount,
            @NotNull RandomEnchantConfig randomEnchants,
            @Nullable String transactionName,
            @NotNull PriceConfig prices,
            @Nullable Integer buyLimit,
            @Nullable Integer sellLimit,
            @NotNull List<String> buyCommands,
            @NotNull List<String> sellCommands) {}

    /**
     * This record contains the configuration for the price of an item.
     * @param buyItems A {@link List} of {@link ItemStackConfig} for the items required to purchase the item. A trade basically.
     * @param buyFixed The fixed buy price.
     * @param sellFixed The fixed sell price.
     * @param buyMin The minimum buy price for calculating a random price.
     * @param buyMax The maximum buy price for calculating a random price.
     * @param sellMin The minimum sell price for calculating a random price.
     * @param sellMax The maximum sell price for calculating a random price.
     */
    @ConfigSerializable
    public record PriceConfig(
            @NotNull List<ItemStackConfig> buyItems,
            @Nullable Double buyFixed,
            @Nullable Double sellFixed,
            @Nullable Double buyMin,
            @Nullable Double buyMax,
            @Nullable Double sellMin,
            @Nullable Double sellMax) {}
}
