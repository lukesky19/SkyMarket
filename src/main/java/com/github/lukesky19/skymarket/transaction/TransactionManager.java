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
package com.github.lukesky19.skymarket.transaction;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.format.FormatUtil;
import com.github.lukesky19.skylib.api.placeholderapi.PlaceholderAPIUtil;
import com.github.lukesky19.skylib.api.player.PlayerUtil;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.integration.HookManager;
import com.github.lukesky19.skymarket.integration.hooks.EconomyHook;
import com.github.lukesky19.skymarket.locale.LocaleManager;
import com.github.lukesky19.skymarket.locale.Locale;
import com.github.lukesky19.skymarket.data.slot.ChestMarketSlot;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This class manages the buying and selling of items or commands.
 */
public class TransactionManager {
    private final @NotNull SkyMarket skyMarket;
    private final @NotNull LocaleManager localeManager;
    private final @NonNull HookManager hookManager;

    /**
     * Default Constructor. You should use {@link TransactionManager#TransactionManager(SkyMarket, LocaleManager, HookManager)} instead.
     * @deprecated You should use {@link TransactionManager#TransactionManager(SkyMarket, LocaleManager, HookManager)} instead.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public TransactionManager() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     *
     * @param skyMarket A {@link SkyMarket} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public TransactionManager(@NotNull SkyMarket skyMarket, @NotNull LocaleManager localeManager, @NonNull HookManager hookManager) {
        this.skyMarket = skyMarket;
        this.localeManager = localeManager;
        this.hookManager = hookManager;
    }

    /**
     * Used when a button is clicked to purchase an item.
     * @param player The player purchasing the item.
     * @param playerId The player's {@link UUID}
     * @param marketSlot The {@link ChestMarketSlot}.
     */
    public void buy(
            @NotNull Player player,
            @NonNull UUID playerId,
            @NonNull ChestMarketSlot marketSlot) {
        Locale locale = localeManager.getLocale();
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);

        // Check if the item can be purchased according to the buy price or the items to trade.
        if(marketSlot.getBuyPrice() <= 0 && marketSlot.getBuyItems().isEmpty()) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.unbuyable()));
            return;
        }

        // Check limits
        if(marketSlot.getServerPurchaseLimit() > 0) {
            if(marketSlot.getServerPurchaseCount() >= marketSlot.getServerPurchaseLimit()) {
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.buyLimitReached()));
                return;
            }
        }

        if(marketSlot.getPlayerPurchaseLimit() > 0) {
            if(marketSlot.getPlayerPurchasedCount(playerId) >= marketSlot.getPlayerPurchaseLimit()) {
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.buyLimitReached()));
                return;
            }
        }

        if(!marketSlot.getBuyItems().isEmpty()) {
            boolean containsItems = true;
            Inventory inventory = player.getInventory();
            for(ItemStack item : marketSlot.getBuyItems()) {
                if(!inventory.containsAtLeast(item, item.getAmount())) {
                    containsItems = false;
                    break;
                }
            }

            if(!containsItems) {
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.insufficientItems()));
                return;
            }
        }

        // Check if the player's balance has enough money for the price
        if(marketSlot.getBuyPrice() > 0) {
            if(economyHook.getBalance(player) < marketSlot.getBuyPrice()) {
                player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.insufficientFunds()));
                return;
            }
        }

        // Remove the price from the player's balance.
        if(marketSlot.getBuyPrice() > 0) {
            economyHook.removeFromBalance(player, marketSlot.getBuyPrice());
        }

        // Remove the items from the player's inventory
        if(!marketSlot.getBuyItems().isEmpty()) {
            Inventory inventory = player.getInventory();
            for (ItemStack item : marketSlot.getBuyItems()) {
                inventory.removeItem(item);
            }
        }

        // Give the player the purchased item if applicable
        Optional<ItemStack> optionalItemStack = marketSlot.createTransactionStack();
        optionalItemStack.ifPresent(itemStack ->
                PlayerUtil.giveItem(player.getInventory(), itemStack, player.getLocation()));

        // Execute the buy commands
        ConsoleCommandSender commandSender = skyMarket.getServer().getConsoleSender();
        for(String command : marketSlot.getBuyCommands()) {
            skyMarket.getServer().dispatchCommand(commandSender,
                    PlaceholderAPIUtil.parsePlaceholders(player, command));
        }

        // Create the DecimalFormat
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        // Format the buy price
        BigDecimal bigPrice = BigDecimal.valueOf(marketSlot.getBuyPrice());
        String formattedPrice = df.format(bigPrice);

        // Format the player's balance
        BigDecimal bigBalance = BigDecimal.valueOf(economyHook.getBalance(player));
        String bal = df.format(bigBalance);

        // Create the list of placeholders
        List<TagResolver.Single> successPlaceholders = new ArrayList<>();
        // Add the placeholder for the item purchased
        successPlaceholders.add(Placeholder.parsed("transaction_name", marketSlot.getTransactionName()));
        // Add the placeholder for the item's price
        successPlaceholders.add(Placeholder.parsed("price", formattedPrice));
        // Add the placeholder for the player's balance
        successPlaceholders.add(Placeholder.parsed("bal", bal));

        // Create the placeholders for the items removed from the player's inventory
        if(!marketSlot.getBuyItems().isEmpty()) {
            for(int i = 0; i < marketSlot.getBuyItems().size() - 1; i++) {
                ItemStack buyStack = marketSlot.getBuyItems().get(i);

                String item = locale.itemFormat();
                item = item.replace("<item_name>", FormatUtil.formatMaterialName(buyStack.getType()));
                item = item.replace("<item_amount>", String.valueOf(buyStack.getAmount()));

                successPlaceholders.add(Placeholder.parsed("item" + i, item));
            }
        }

        // Send the success message
        player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.buySuccess(), successPlaceholders));

        // Increment the limits if a limit is configured
        if(marketSlot.getServerPurchaseLimit() > 0
                || marketSlot.getPlayerPurchaseLimit() > 0) {
            marketSlot.addServerPurchaseCount(1);
            marketSlot.addPlayerPurchaseCount(playerId, 1);
        }
    }

    /**
     * Used when a button is clicked to sell an item.
     * @param player The player purchasing the item.
     * @param playerId The player's {@link UUID}.
     * @param marketSlot The {@link ChestMarketSlot}.
     */
    public void sell(
            @NotNull Player player,
            @NonNull UUID playerId,
            @NonNull ChestMarketSlot marketSlot) {
        Locale locale = localeManager.getLocale();
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);

        // Check if the item can be sold according to the sell price
        if(marketSlot.getSellPrice() <= 0) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.unsellable()));
            return;
        }

        // Check limits
        if(marketSlot.getServerSellLimit() > 0) {
            if(marketSlot.getServerSoldCount() >= marketSlot.getServerSellLimit()) {
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.sellLimitReached()));
                return;
            }
        }

        if(marketSlot.getPlayerSellLimit() > 0) {
            if(marketSlot.getPlayerSoldCount(playerId) >= marketSlot.getPlayerSellLimit()) {
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.sellLimitReached()));
                return;
            }
        }

        if(marketSlot.getServerSoldCount() >= marketSlot.getServerSellLimit()
                || marketSlot.getPlayerSoldCount(playerId) >= marketSlot.getPlayerSellLimit()) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.buyLimitReached()));
            return;
        }

        Optional<ItemStack> optionalItemStack = marketSlot.createTransactionStack();
        if(optionalItemStack.isPresent()) {
            ItemStack transactionItem = optionalItemStack.get();
            // Check if the player has enough items
            if(!player.getInventory().containsAtLeast(transactionItem, transactionItem.getAmount())) {
                player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.notEnoughItems()));
                return;
            }

            // Remove the item from the player's inventory
            player.getInventory().removeItem(transactionItem);
        }

        // Give the player the price
        economyHook.addToBalance(player, marketSlot.getSellPrice());

        // Execute the sell commands
        ConsoleCommandSender commandSender = skyMarket.getServer().getConsoleSender();
        for(String command : marketSlot.getSellCommands()) {
            skyMarket.getServer().dispatchCommand(commandSender,
                    PlaceholderAPIUtil.parsePlaceholders(player, command));
        }

        // Create the DecimalFormat
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        // Format the sell price
        BigDecimal sellPrice = BigDecimal.valueOf(marketSlot.getSellPrice());
        String formattedPrice = df.format(sellPrice);

        // Format the player's balance
        BigDecimal bigBalance = BigDecimal.valueOf(economyHook.getBalance(player));
        String bal = df.format(bigBalance);

        // Create the list of placeholders
        List<TagResolver.Single> successPlaceholders = new ArrayList<>();
        // Add the placeholder for the item purchased
        successPlaceholders.add(Placeholder.parsed("transaction_name", marketSlot.getTransactionName()));
        // Add the placeholder for the item's price
        successPlaceholders.add(Placeholder.parsed("price", formattedPrice));
        // Add the placeholder for the player's balance
        successPlaceholders.add(Placeholder.parsed("bal", bal));

        // Send the success message
        player.sendMessage(AdventureUtil.deserialize(player, locale.prefix() + locale.sellSuccess(), successPlaceholders));

        // Increment the limits if a limit is configured
        if(marketSlot.getServerSellLimit() > 0
                || marketSlot.getPlayerSellLimit() > 0) {
            marketSlot.addServerSoldCount(1);
            marketSlot.addPlayerSoldCount(playerId, 1);
        }
    }
}