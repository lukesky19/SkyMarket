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
package com.github.lukesky19.skymarket.market.config.chest;

import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import com.github.lukesky19.skymarket.market.config.button.ButtonConfig;
import com.github.lukesky19.skymarket.market.config.common.AmountConfig;
import com.github.lukesky19.skymarket.market.config.common.RandomEnchantConfig;
import com.github.lukesky19.skymarket.gui.guis.ChestMarketGUI;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * This record contains the configuration to create a {@link ChestMarketGUI}.
 * @param version The version of the config.
 * @param refreshTime The time between refreshes.
 * @param marketName The name of the market.
 * @param guiData The {@link GuiData}.
 * @param entries A {@link List} of {@link MarketEntry}.
 */
@ConfigSerializable
public record ChestConfig(
        int version,
        @Nullable String refreshTime,
        @Nullable String marketName,
        @NonNull GuiData guiData,
        @NonNull List<MarketEntry> entries) {
    /**
     * This record contains the data for creating and decorating the GUI.
     * @param guiType The {@link GUIType}.
     * @param guiName The name to display inside the inventory GUI.
     * @param filler The {@link ButtonConfig} for the filler buttons.
     * @param nextPage The {@link ButtonConfig} for the next page button.
     * @param prevPage The {@link ButtonConfig} for the previous page button.
     * @param exit The {@link ButtonConfig} for the exit button.
     * @param time The {@link ButtonConfig} for the button that displays the time until the market refreshes.
     * @param placeholderSlots A {@link List} of {@link PlaceholderConfig}s to place actual market items in.
     * @param dummyButtons A {@link List} of {@link ButtonConfig}s to display in the GUI.
     */
    @ConfigSerializable
    public record GuiData(
            @Nullable GUIType guiType,
            @Nullable String guiName,
            @NonNull ButtonConfig filler,
            @NonNull ButtonConfig nextPage,
            @NonNull ButtonConfig prevPage,
            @NonNull ButtonConfig exit,
            @NonNull ButtonConfig time,
            @NonNull List<PlaceholderConfig> placeholderSlots,
            @NonNull List<ButtonConfig> dummyButtons) {}
    /**
     * This record contains the configuration to replace placeholder slots with.
     * @param transactionName The transaction name to use in messages.
     * @param displayItem The {@link ItemStackConfig} to display in the GUI.
     * @param transactionItem The {@link ItemStackConfig} that is given to the player.
     * @param randomEnchants The {@link RandomEnchantConfig} to apply to the {@link #displayItem} and {@link #transactionItem}.
     * @param amount The {@link AmountConfig} to apply to the {@link #displayItem} and {@link #transactionItem}.
     * @param buyCommands A {@link List} of {@link String}s to execute in console when the purchase is completed.
     * @param sellCommands A {@link List} of {@link String}s to execute in console when a sale is completed.
     * @param prices The {@link PriceConfig} for the market entry.
     * @param playerBuyLimit The per-player buy limit.
     * @param playerSellLimit The per-player sell limit.
     * @param serverBuyLimit The server-wide buy limit.
     * @param serverSellLimit The server-wide sell limit.
     * @param refreshTime The time until the market entry is refreshed.
     */
    @ConfigSerializable
    public record MarketEntry(
            @Nullable String transactionName,
            @NonNull ItemStackConfig displayItem,
            @NonNull ItemStackConfig transactionItem,
            @NonNull AmountConfig amount,
            @NonNull RandomEnchantConfig randomEnchants,
            @NonNull List<String> buyCommands,
            @NonNull List<String> sellCommands,
            @NonNull PriceConfig prices,
            int playerBuyLimit,
            int playerSellLimit,
            int serverBuyLimit,
            int serverSellLimit,
            @Nullable String refreshTime) {}
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
            @NonNull List<ItemStackConfig> buyItems,
            @Nullable Double buyFixed,
            @Nullable Double sellFixed,
            @Nullable Double buyMin,
            @Nullable Double buyMax,
            @Nullable Double sellMin,
            @Nullable Double sellMax) {}
    /**
     * This record contains the configuration for the page number and slot number a market entry can be placed in.
     * @param pageNum The page number.
     * @param slotNum The slot number.
     */
    @ConfigSerializable
    public record PlaceholderConfig(
            @Nullable Integer pageNum,
            @Nullable Integer slotNum) {}
}