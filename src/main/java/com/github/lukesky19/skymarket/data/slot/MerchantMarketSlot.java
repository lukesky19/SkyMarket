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
package com.github.lukesky19.skymarket.data.slot;

import com.github.lukesky19.skymarket.interfaces.IMarketSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This market slot is used in merchant-style markets.
 */
public class MerchantMarketSlot implements IMarketSlot {
    private final int slotNum;

    // Item Config
    private final @NonNull ItemStack input1;
    private final @Nullable ItemStack input2;
    private final @NonNull ItemStack output;

    // Limits
    private final int serverLimit;
    private final int playerLimit;

    // Server Count
    private int serverCount;

    // Player Counts
    private final @NonNull Map<UUID, Integer> playerCounts = new HashMap<>();

    private long refreshTime;

    /**
     * Constructor
     * @param input1 The {@link ItemStack} for the first item taken in the trade.
     * @param input2 The {@link ItemStack} for the second item taken in the trade. May be null.
     * @param output The {@link ItemStack} for the item given in the trade.
     * @param slotNum The slot number.
     * @param serverLimit The server limit.
     * @param playerLimit The player limit.
     */
    public MerchantMarketSlot(
            @NonNull ItemStack input1,
            @Nullable ItemStack input2,
            @NonNull ItemStack output,
            int slotNum,
            int serverLimit,
            int playerLimit) {
        this.slotNum = slotNum;

        // Items
        this.input1 = input1;
        this.input2 = input2;
        this.output = output;

        // Limits
        this.serverLimit = serverLimit;
        this.playerLimit = playerLimit;
    }

    /**
     * Create the {@link MerchantRecipe}.
     * @param playerId The {@link UUID} of the player.
     * @return A {@link MerchantRecipe}.
     */
    public @NonNull MerchantRecipe createMerchantRecipe(@NonNull UUID playerId) {
        MerchantRecipe recipe = new MerchantRecipe(output, 999999999);

        // Add the first ingredient
        recipe.addIngredient(input1);
        // Add the second ingredient if present
        if(input2 != null) recipe.addIngredient(input2);

        // Set the recipe to ignore discounts and to not reward experience
        recipe.setIgnoreDiscounts(true);
        recipe.setExperienceReward(false);

        // Set the recipe's uses and max uses if a limit is configured
        if(serverLimit > 0 || playerLimit > 0) {
            int playerAmount = playerCounts.getOrDefault(playerId, 0);
            if(serverCount >= serverLimit) {
                recipe.setUses(serverCount);
                recipe.setMaxUses(serverLimit);
            } else if(playerAmount >= playerLimit) {
                recipe.setUses(playerAmount);
                recipe.setMaxUses(playerLimit);
            } else {
                recipe.setMaxUses(Math.min(playerLimit, serverLimit));
                recipe.setUses(Math.min(playerAmount, serverCount));
            }
        }

        return recipe;
    }

    /**
     * Returns an empty string.
     * @return Returns an empty string.
     */
    @Override
    public @NonNull String getTransactionName() {
        return "";
    }

    /**
     * The page number will always be 0.
     * @return Always 0.
     */
    @Override
    public int getPageNumber() {
        return 0;
    }

    @Override
    public int getSlotNumber() {
        return slotNum;
    }

    /**
     * Set the server trade count.
     * @param amount The amount.
     */
    public void setServerCount(int amount) {
        this.serverCount = Math.max(0, amount);
    }

    /**
     * Add to the server trade count.
     * @param amount The amount.
     */
    public void addServerCount(int amount) {
        this.serverCount += Math.max(0, amount);
    }

    /**
     * Set the player trade count.
     * @param playerId The {@link UUID} of the player.
     * @param amount The amount.
     */
    public void setPlayerCount(@NonNull UUID playerId, int amount) {
        playerCounts.put(playerId, Math.max(0, amount));
    }

    /**
     * Add to the player trade count.
     * @param playerId The {@link UUID} of the player.
     * @param amount The amount.
     */
    public void addPlayerCount(@NonNull UUID playerId, int amount) {
        int currentAmount = playerCounts.getOrDefault(playerId, 0);
        int updatedAmount = currentAmount + Math.max(0, amount);

        playerCounts.put(playerId, updatedAmount);
    }

    @Override
    public void setRefreshTime(long refreshTime) {
        this.refreshTime = Math.max(0, refreshTime);
    }

    @Override
    public long getRefreshTime() {
        return refreshTime;
    }
}
