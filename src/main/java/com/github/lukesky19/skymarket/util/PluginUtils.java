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
package com.github.lukesky19.skymarket.util;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * This class contains methods to calculate a random price, and create {@link ItemStack}s.
 */
public class PluginUtils {
    /**
     * Default Constructor. All methods in this class are static.
     * @deprecated All methods in this class are static.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public PluginUtils() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Calculates a random price from a min and max.
     * @param min The min price.
     * @param max The max price.
     * @return A double representing a price.
     */
    public static double calculatePrice(double min, double max) {
        double price;
        if (max <= 0.0 && min <= 0.0) {
            price = 0.0;
        } else {
            price = BigDecimal.valueOf(Math.random() * (max - min) + min).setScale(2, RoundingMode.CEILING).doubleValue();
        }

        return price;
    }

    /**
     * Calculate the amount of items an {@link ItemStack} should have.
     * @param fixed The fixed amount.
     * @param min The minimum amount.
     * @param max The maximum amount.
     * @return An {@link Integer} representing the amount of items the {@link ItemStack} should have, or null.
     */
    public static @Nullable Integer getRandomAmount(@Nullable Integer fixed, @Nullable Integer min, @Nullable Integer max) {
        // Calculate the amount of items the stack should contain
        if(fixed != null) {
            return fixed;
        } else {
            if(max != null && min != null) {
                return (int) (Math.random() * (max - min) + min);
            }
        }

        return null;
    }

    /**
     * Generate the {@link Map} mapping {@link Enchantment}s to a level as an {@link Integer}.
     * @param itemType The {@link ItemType} to use for the dummy {@link ItemStack} to enchant.
     * @param enchantRandomly Should the item be enchanted randomly?
     * @param min The minimum exp level.
     * @param max The maximum exp level.
     * @param treasure Should treasure enchantments be included?
     * @return A {@link Map} mapping {@link Enchantment}s to a level as an {@link Integer}, or null.
     */
    public static @Nullable Map<Enchantment, Integer> getRandomEnchantments(
            @Nullable ItemType itemType,
            @Nullable Boolean enchantRandomly,
            @Nullable Integer min,
            @Nullable Integer max,
            @Nullable Boolean treasure) {
        if(itemType == null
                || (enchantRandomly == null || !enchantRandomly)
                || (min == null || min <= 0)
                || (max == null || max <= 0)
                || (treasure == null)) return null;

        // Calculate the random enchantments to add
        ItemStack dummyStack = itemType.createItemStack();

        dummyStack.enchantWithLevels(new Random().nextInt(min, max), treasure, new Random());

        return dummyStack.getEnchantments();
    }
}