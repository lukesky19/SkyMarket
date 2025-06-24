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
package com.github.lukesky19.skymarket.data;

import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * This class contains player data for active markets.
 */
public class PlayerData {
    private @NotNull Map<Integer, Integer> buyLimits;
    private @NotNull Map<Integer, Integer> sellLimits;
    private @NotNull List<MerchantRecipe> playerTrades;

    /**
     * Default Constructor. You should use {@link PlayerData#PlayerData(Map, Map, List)} instead.
     * @deprecated You should use {@link PlayerData#PlayerData(Map, Map, List)} instead.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public PlayerData() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param buyLimits A {@link Map} mapping a slot to their current amount purchased from that slot.
     * @param sellLimits A {@link Map} mapping a slot to their current amount sold from that slot.
     * @param playerTrades A {@link List} of {@link MerchantRecipe} for the player. Each {@link MerchantRecipe} contains the limits for the player.
     */
    public PlayerData(
            @NotNull Map<Integer, Integer> buyLimits,
            @NotNull Map<Integer, Integer> sellLimits,
            @NotNull List<MerchantRecipe> playerTrades) {
        this.buyLimits = buyLimits;
        this.sellLimits = sellLimits;
        this.playerTrades = playerTrades;
    }

    /**
     * Increment the buy limit for the provided slot.
     * @param slot The slot to increase the buy limit for.
     */
    public void incrementBuyLimit(int slot) {
        buyLimits.put(slot, buyLimits.getOrDefault(slot, 0) + 1);
    }

    /**
     * Sets the current player's buy limits to the provided mapping.
     * @param buyLimits A {@link Map} mapping slots as an {@link Integer} to the amount purchased so far as an {@link Integer}.
     */
    public void setBuyLimits(@NotNull Map<Integer, Integer> buyLimits) {
        this.buyLimits = buyLimits;
    }

    /**
     * Gets the current player's buy limits.
     * @return A {@link Map} mapping slots as an {@link Integer} to the amount purchased so far as an {@link Integer}.
     */
    public @NotNull Map<Integer, Integer> getBuyLimits() {
        return buyLimits;
    }

    /**
     * Increment the sell limit for the provided slot.
     * @param slot The slot to increase the sell limit for.
     */
    public void incrementSellLimit(int slot) {
        sellLimits.put(slot, sellLimits.getOrDefault(slot, 0) + 1);
    }

    /**
     * Sets the current player's sell limits to the provided mapping.
     * @param sellLimits A {@link Map} mapping slots as an {@link Integer} to the amount sold so far as an {@link Integer}.
     */
    public void setSellLimits(@NotNull Map<Integer, Integer> sellLimits) {
        this.sellLimits = sellLimits;
    }

    /**
     * Gets the current player's sell limits.
     * @return A {@link Map} mapping slots as an {@link Integer} to the amount sold so far as an {@link Integer}.
     */
    public @NotNull Map<Integer, Integer> getSellLimits() {
        return sellLimits;
    }

    /**
     * Sets the current player's trades.
     * @param playerTrades A {@link List} of {@link MerchantRecipe}.
     */
    public void setPlayerTrades(@NotNull List<MerchantRecipe> playerTrades) {
        this.playerTrades = playerTrades;
    }

    /**
     * Gets the current player's trades.
     * @return A {@link List} of {@link MerchantRecipe}.
     */
    public @NotNull List<MerchantRecipe> getPlayerTrades() {
        return playerTrades;
    }
}
