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
package com.github.lukesky19.skymarket.manager;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.data.config.gui.MerchantConfig;
import com.github.lukesky19.skymarket.util.PluginUtils;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class manages the creation of {@link MerchantRecipe}s for markets.
 */
public class TradeManager {
    private final @NotNull SkyMarket skyMarket;

    /**
     * Default Constructor. You should use {@link TradeManager#TradeManager(SkyMarket)} instead.
     * @deprecated You should use {@link TradeManager#TradeManager(SkyMarket)} instead.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public TradeManager() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param skyMarket A {@link SkyMarket} instance.
     */
    public TradeManager(@NotNull SkyMarket skyMarket) {
        this.skyMarket = skyMarket;
    }

    /**
     * Gets a {@link List} containing random {@link MerchantRecipe}s to populate a TradeGUI with.
     * @param tradeConfig The {@link MerchantConfig} to load data from.
     * @return A {@link List} containing a {@link MerchantRecipe}s.
     */
    public @NotNull List<MerchantRecipe> createTrades(@NotNull MerchantConfig tradeConfig) {
        ComponentLogger logger = skyMarket.getComponentLogger();

        // The final list of MerchantRecipe trades
        List<MerchantRecipe> trades = new ArrayList<>();
        // The total number of trades to create and add to the list if possible.
        int totalTrades = tradeConfig.numOfTrades();

        // THe current number of added trades
        int addedTrades = 0;
        // The list of trade configuration.
        List<MerchantConfig.Trade> tradesList = new ArrayList<>(tradeConfig.trades());
        if(tradesList.isEmpty()) {
            logger.warn(AdventureUtil.serialize("Unable to create trades for a trade gui as no trades are configured."));
            return trades;
        }

        while(addedTrades != totalTrades) {
            if(!tradesList.isEmpty()) {
                int randomIndex = new Random().nextInt(tradesList.size());
                MerchantConfig.Trade randomTrade = tradesList.get(randomIndex);

                tradesList.remove(randomIndex);

                Optional<ItemStack> optionalOutputStack = PluginUtils.createItemStack(logger, randomTrade.output().item(), randomTrade.output().amount(), randomTrade.output().randomEnchants(), List.of());
                Optional<ItemStack> optionalFirstInputStack = PluginUtils.createItemStack(logger, randomTrade.input1().item(), randomTrade.input1().amount(), randomTrade.input1().randomEnchants(), List.of());
                Optional<ItemStack> optionalSecondInputStack = PluginUtils.createItemStack(logger, randomTrade.input2().item(), randomTrade.input2().amount(), randomTrade.input2().randomEnchants(), List.of());

                // If no ItemStacks were created, let's continue on
                if(optionalOutputStack.isEmpty() && optionalFirstInputStack.isEmpty() && optionalSecondInputStack.isEmpty()) continue;
                // If there is no output stack or input stack, let's continue on. Both of these are required to create a trade
                if(optionalOutputStack.isEmpty() || optionalFirstInputStack.isEmpty()) continue;

                // Get the two required ItemStacks
                ItemStack outputStack = optionalOutputStack.get();
                ItemStack inputStack1 = optionalFirstInputStack.get();

                // Create the MerchantRecipe with the output stack
                MerchantRecipe recipe = new MerchantRecipe(outputStack, 999999999);

                // Add the first ingredient
                recipe.addIngredient(inputStack1);
                // Add the second ingredient if present
                optionalSecondInputStack.ifPresent(recipe::addIngredient);

                // Set the recipe to ignore discounts and to not reward experience
                recipe.setIgnoreDiscounts(true);
                recipe.setExperienceReward(false);

                // Add the MerchantRecipe to the list of trades
                trades.add(recipe);

                // Increment the added trades count
                addedTrades++;
            } else {
                logger.warn(AdventureUtil.serialize("Not enough trades configured to meet the number of trades to add. " + addedTrades + "/" + totalTrades));
                return trades;
            }
        }

        return trades;
    }
}
