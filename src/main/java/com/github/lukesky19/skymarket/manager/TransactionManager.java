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
import com.github.lukesky19.skylib.api.format.FormatUtil;
import com.github.lukesky19.skylib.api.placeholderapi.PlaceholderAPIUtil;
import com.github.lukesky19.skylib.api.player.PlayerUtil;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.configuration.LocaleManager;
import com.github.lukesky19.skymarket.data.config.Locale;
import com.github.lukesky19.skymarket.data.PlayerData;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This class manages the buying and selling of items or commands.
 */
public class TransactionManager {
    private final @NotNull SkyMarket skyMarket;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull GUIManager guiManager;

    /**
     * Default Constructor. You should use {@link TransactionManager#TransactionManager(SkyMarket, LocaleManager, GUIManager)} instead.
     * @deprecated You should use {@link TransactionManager#TransactionManager(SkyMarket, LocaleManager, GUIManager)} instead.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public TransactionManager() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param skyMarket A {@link SkyMarket} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     */
    public TransactionManager(@NotNull SkyMarket skyMarket, @NotNull LocaleManager localeManager, @NotNull GUIManager guiManager) {
        this.skyMarket = skyMarket;
        this.localeManager = localeManager;
        this.guiManager = guiManager;
    }

    /**
     * Used when a button is clicked to purchase an item.
     * @param player The player purchasing the item.
     * @param playerData The player's {@link PlayerData}.
     * @param itemStack The item to purchase.
     * @param price The buy price of the item.
     * @param buyItems The items to take in exchange for the item.
     * @param slot The slot of the button clicked.
     * @param limit The limit of how many times this item can be purchased.
     */
    public void buyItem(
            @NotNull Player player,
            @NotNull PlayerData playerData,
            @NotNull ItemStack itemStack,
            double price,
            @NotNull List<ItemStack> buyItems,
            int slot,
            @Nullable Integer limit) {
        Locale locale = localeManager.getLocale();

        // Check if the item can be purchased according to the buy price or the items to trade.
        if(price <= 0 && buyItems.isEmpty()) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.unbuyable()));
            return;
        }

        // If a limit is configured, compare the player's current buy count to the limit.
        if(limit != null && limit > 0) {
            @Nullable Integer playerLimit = playerData.getBuyLimits().get(slot);
            if(playerLimit != null && playerLimit >= limit) {
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.buyLimitReached()));
                return;
            }
        }

        if(price > 0) {
            if(!buyItems.isEmpty()) {
                // Check if the player's balance has enough money for the price
                if(skyMarket.getEconomy().getBalance(player) < price) {
                    player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.insufficientFunds()));

                    skyMarket.getServer().getScheduler().runTaskLater(skyMarket, () -> {
                        player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

                        guiManager.removeOpenGUI(player.getUniqueId());
                    }, 1L);

                    return;
                }

                boolean containsItems = true;
                for(ItemStack item : buyItems) {
                    if(!player.getInventory().containsAtLeast(item, item.getAmount())) {
                        containsItems = false;
                        break;
                    }
                }

                if(!containsItems) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.insufficientItems()));

                    skyMarket.getServer().getScheduler().runTaskLater(skyMarket, () -> {
                        player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

                        guiManager.removeOpenGUI(player.getUniqueId());
                    }, 1L);

                    return;
                }

                // Remove the price from the player's balance.
                skyMarket.getEconomy().withdrawPlayer(player, price);

                // Remove the items from the player's inventory
                for(ItemStack item : buyItems) {
                    player.getInventory().removeItem(item);
                }

                // Give the player the purchased item
                PlayerUtil.giveItem(player.getInventory(), itemStack, itemStack.getAmount(), player.getLocation());

                // Format the purchased item's name
                String playerItem = locale.itemFormat();
                playerItem = playerItem.replace("<item_name>", FormatUtil.formatMaterialName(itemStack.getType()));
                playerItem = playerItem.replace("<item_amount>", String.valueOf(itemStack.getAmount()));

                // Create the DecimalFormat
                DecimalFormat df = new DecimalFormat("#.##");
                df.setRoundingMode(RoundingMode.CEILING);

                // Format the buy price
                BigDecimal bigPrice = BigDecimal.valueOf(price);
                String formattedPrice = df.format(bigPrice);

                // Format the player's balance
                BigDecimal bigBalance = BigDecimal.valueOf(skyMarket.getEconomy().getBalance(player));
                String bal = df.format(bigBalance);

                // Create the list of placeholders
                List<TagResolver.Single> successPlaceholders = new ArrayList<>();
                // Add the placeholder for the item purchased
                successPlaceholders.add(Placeholder.parsed("item", playerItem));
                // Add the placeholder for the item's price
                successPlaceholders.add(Placeholder.parsed("price", formattedPrice));
                // Add the placeholder for the player's balance
                successPlaceholders.add(Placeholder.parsed("bal", bal));

                // Create the placeholders for the items removed from the player's inventory
                for(int i = 0; i < buyItems.size() - 1; i++) {
                    ItemStack buyStack = buyItems.get(i);

                    String item = locale.itemFormat();
                    item = item.replace("<item_name>", FormatUtil.formatMaterialName(buyStack.getType()));
                    item = item.replace("<item_amount>", String.valueOf(buyStack.getAmount()));

                    successPlaceholders.add(Placeholder.parsed("item" + i, item));
                }

                // Send the success message
                player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.buySuccess(), successPlaceholders));

                // Increment the player's buy limit if a limit is configured
                if(limit != null && limit > 0) {
                    playerData.incrementBuyLimit(slot);
                }
            } else {
                // Check if the player's balance has enough money for the price
                if(skyMarket.getEconomy().getBalance(player) < price) {
                    player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.insufficientFunds()));

                    skyMarket.getServer().getScheduler().runTaskLater(skyMarket, () -> {
                        player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

                        guiManager.removeOpenGUI(player.getUniqueId());
                    }, 1L);

                    return;
                }

                // Remove the price from the player's balance.
                skyMarket.getEconomy().withdrawPlayer(player, price);

                // Give the player the purchased item
                PlayerUtil.giveItem(player.getInventory(), itemStack, itemStack.getAmount(), player.getLocation());

                // Format the purchased item's name
                String playerItem = locale.itemFormat();
                playerItem = playerItem.replace("<item_name>", FormatUtil.formatMaterialName(itemStack.getType()));
                playerItem = playerItem.replace("<item_amount>", String.valueOf(itemStack.getAmount()));

                // Create the DecimalFormat
                DecimalFormat df = new DecimalFormat("#.##");
                df.setRoundingMode(RoundingMode.CEILING);

                // Format the buy price
                BigDecimal bigPrice = BigDecimal.valueOf(price);
                String formattedPrice = df.format(bigPrice);

                // Format the player's balance
                BigDecimal bigBalance = BigDecimal.valueOf(skyMarket.getEconomy().getBalance(player));
                String bal = df.format(bigBalance);

                // Create the list of placeholders
                List<TagResolver.Single> successPlaceholders = new ArrayList<>();
                // Add the placeholder for the item purchased
                successPlaceholders.add(Placeholder.parsed("item", playerItem));
                // Add the placeholder for the item's price
                successPlaceholders.add(Placeholder.parsed("price", formattedPrice));
                // Add the placeholder for the player's balance
                successPlaceholders.add(Placeholder.parsed("bal", bal));

                // Send the success message
                player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.buySuccess(), successPlaceholders));

                // Increment the player's buy limit if a limit is configured
                if(limit != null && limit > 0) {
                    playerData.incrementBuyLimit(slot);
                }
            }
        } else {
            // Check if the player contains the required items
            boolean containsItems = true;
            for(ItemStack item : buyItems) {
                if(!player.getInventory().containsAtLeast(item, item.getAmount())) {
                    containsItems = false;
                    break;
                }
            }

            if(!containsItems) {
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.insufficientItems()));

                skyMarket.getServer().getScheduler().runTaskLater(skyMarket, () -> {
                    player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

                    guiManager.removeOpenGUI(player.getUniqueId());
                }, 1L);

                return;
            }

            // Remove the items from the player's inventory
            for(ItemStack item : buyItems) {
                player.getInventory().removeItem(item);
            }

            // Give the player the purchased item
            PlayerUtil.giveItem(player.getInventory(), itemStack, itemStack.getAmount(), player.getLocation());

            // Format the purchased item's name
            String playerItem = locale.itemFormat();
            playerItem = playerItem.replace("<item_name>", FormatUtil.formatMaterialName(itemStack.getType()));
            playerItem = playerItem.replace("<item_amount>", String.valueOf(itemStack.getAmount()));

            // Create the DecimalFormat
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);

            // Format the player's balance
            BigDecimal bigBalance = BigDecimal.valueOf(skyMarket.getEconomy().getBalance(player));
            String bal = df.format(bigBalance);

            // Create the list of placeholders
            List<TagResolver.Single> successPlaceholders = new ArrayList<>();
            // Add the placeholder for the item purchased
            successPlaceholders.add(Placeholder.parsed("item", playerItem));
            successPlaceholders.add(Placeholder.parsed("price", ""));
            // Add the placeholder for the player's balance
            successPlaceholders.add(Placeholder.parsed("bal", bal));

            // Create the placeholders for the items removed from the player's inventory
            for(int i = 0; i < buyItems.size() - 1; i++) {
                ItemStack buyStack = buyItems.get(i);

                String item = locale.itemFormat();
                item = item.replace("<item_name>", FormatUtil.formatMaterialName(buyStack.getType()));
                item = item.replace("<item_amount>", String.valueOf(buyStack.getAmount()));

                successPlaceholders.add(Placeholder.parsed("item" + i, item));
            }

            // Send the success message
            player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.buySuccess(), successPlaceholders));

            // Increment the player's buy limit if a limit is configured
            if(limit != null && limit > 0) {
                playerData.incrementBuyLimit(slot);
            }
        }
    }

    /**
     * Used when a button is clicked to sell an item.
     * @param player The player selling the item.
     * @param playerData The player's {@link PlayerData}.
     * @param itemStack The item to sell.
     * @param price The sell price of the item.
     * @param slot The slot of the button clicked.
     * @param limit The limit of how many times this item can be sold.
     */
    public void sellItem(
            @NotNull Player player,
            @NotNull PlayerData playerData,
            @NotNull ItemStack itemStack,
            double price,
            int slot,
            @Nullable Integer limit) {
        Locale locale = localeManager.getLocale();

        // Check if the item can be sold according to the sell price
        if(price <= 0) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.unsellable()));
            return;
        }

        // If a limit is configured, compare the player's current buy count to the limit.
        if(limit != null && limit > 0) {
            @Nullable Integer playerLimit = playerData.getSellLimits().get(slot);
            if(playerLimit != null && playerLimit >= limit) {
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.sellLimitReached()));
                return;
            }
        }

        if(!player.getInventory().containsAtLeast(itemStack, itemStack.getAmount())) {
            player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.notEnoughItems()));

            skyMarket.getServer().getScheduler().runTaskLater(skyMarket, () -> {
                player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

                guiManager.removeOpenGUI(player.getUniqueId());
            }, 1L);

            return;
        }

        // Remove the item from the player's inventory
        player.getInventory().removeItem(itemStack);

        // Give the player the price
        skyMarket.getEconomy().depositPlayer(player, price);

        // Format the sold item's name
        String soldItem = locale.itemFormat();
        soldItem = soldItem.replace("<item_name>", FormatUtil.formatMaterialName(itemStack.getType()));
        soldItem = soldItem.replace("<item_amount>", String.valueOf(itemStack.getAmount()));

        // Create the DecimalFormat
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        // Format the sell price
        BigDecimal sellPrice = BigDecimal.valueOf(price);
        String formattedPrice = df.format(sellPrice);

        // Format the player's balance
        BigDecimal bigBalance = BigDecimal.valueOf(skyMarket.getEconomy().getBalance(player));
        String bal = df.format(bigBalance);

        // Create the list of placeholders
        List<TagResolver.Single> successPlaceholders = new ArrayList<>();
        // Add the placeholder for the item purchased
        successPlaceholders.add(Placeholder.parsed("item", soldItem));
        // Add the placeholder for the item's price
        successPlaceholders.add(Placeholder.parsed("price", formattedPrice));
        // Add the placeholder for the player's balance
        successPlaceholders.add(Placeholder.parsed("bal", bal));

        // Send the success message
        player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.sellSuccess(), successPlaceholders));

        // Increment the player's sell limit if a limit is configured
        if(limit != null && limit > 0) {
            playerData.incrementSellLimit(slot);
        }
    }

    /**
     * Used when a button is clicked to buy a command. (Runs a command through console, doesn't give the player access to the command.)
     * @param player The player buying the command.
     * @param playerData The player's {@link PlayerData}.
     * @param name The name of the command being purchased. Taken from the GUI configuration.
     * @param price The price of the command.
     * @param buyItems The items to take in exchange for the command.
     * @param buyCommands The commands to run once the transaction takes place.
     * @param slot The slot of the button clicked.
     * @param limit The limit of how many times this item can be purchased.
     */
    public void buyCommand(
            @NotNull Player player,
            @NotNull PlayerData playerData,
            @NotNull String name,
            double price,
            @NotNull List<ItemStack> buyItems,
            @NotNull List<String> buyCommands,
            int slot,
            @Nullable Integer limit) {
        Locale locale = localeManager.getLocale();

        // Check if the command can be purchased according to the buy price or the items to trade.
        if(price <= 0 && buyItems.isEmpty()) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.unbuyable()));
            return;
        }

        // If a limit is configured, compare the player's current buy count to the limit.
        if(limit != null && limit > 0) {
            @Nullable Integer playerLimit = playerData.getBuyLimits().get(slot);
            if(playerLimit != null && playerLimit >= limit) {
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.buyLimitReached()));
                return;
            }
        }

        if(price > 0) {
            if(!buyItems.isEmpty()) {
                // Check if the player's balance has enough money for the price
                if(skyMarket.getEconomy().getBalance(player) < price) {
                    player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.insufficientFunds()));

                    skyMarket.getServer().getScheduler().runTaskLater(skyMarket, () -> {
                        player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

                        guiManager.removeOpenGUI(player.getUniqueId());
                    }, 1L);

                    return;
                }

                boolean containsItems = true;
                for(ItemStack item : buyItems) {
                    if (!player.getInventory().containsAtLeast(item, item.getAmount())) {
                        containsItems = false;
                        break;
                    }
                }

                if(!containsItems) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.insufficientItems()));

                    skyMarket.getServer().getScheduler().runTaskLater(skyMarket, () -> {
                        player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

                        guiManager.removeOpenGUI(player.getUniqueId());
                    }, 1L);

                    return;
                }

                // Remove the price from the player's balance.
                skyMarket.getEconomy().withdrawPlayer(player, price);

                // Remove the items from the player's inventory
                for(ItemStack item : buyItems) {
                    player.getInventory().removeItem(item);
                }

                // Run the buy commands through console
                for (String command : buyCommands) {
                    skyMarket.getServer().dispatchCommand(skyMarket.getServer().getConsoleSender(), PlaceholderAPIUtil.parsePlaceholders(player, command));
                }

                // Create the DecimalFormat
                DecimalFormat df = new DecimalFormat("#.##");
                df.setRoundingMode(RoundingMode.CEILING);

                // Format the buy price
                BigDecimal bigPrice = BigDecimal.valueOf(price);
                String formattedPrice = df.format(bigPrice);

                // Format the player's balance
                BigDecimal bigBalance = BigDecimal.valueOf(skyMarket.getEconomy().getBalance(player));
                String bal = df.format(bigBalance);

                // Create the list of placeholders
                List<TagResolver.Single> successPlaceholders = new ArrayList<>();
                // Add the placeholder for the item purchased
                successPlaceholders.add(Placeholder.parsed("item", name));
                // Add the placeholder for the item's price
                successPlaceholders.add(Placeholder.parsed("price", formattedPrice));
                // Add the placeholder for the player's balance
                successPlaceholders.add(Placeholder.parsed("bal", bal));

                // Create the placeholders for the items removed from the player's inventory
                for(int i = 0; i < buyItems.size() - 1; i++) {
                    ItemStack buyStack = buyItems.get(i);

                    String item = locale.itemFormat();
                    item = item.replace("<item_name>", FormatUtil.formatMaterialName(buyStack.getType()));
                    item = item.replace("<item_amount>", String.valueOf(buyStack.getAmount()));

                    successPlaceholders.add(Placeholder.parsed("item" + i, item));
                }

                // Send the success message
                player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.buySuccess(), successPlaceholders));

                // Increment the player's buy limit if a limit is configured
                if(limit != null && limit > 0) {
                    playerData.incrementBuyLimit(slot);
                }
            } else {
                // Check if the player's balance has enough money for the price
                if(skyMarket.getEconomy().getBalance(player) < price) {
                    player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.insufficientFunds()));

                    skyMarket.getServer().getScheduler().runTaskLater(skyMarket, () -> {
                        player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

                        guiManager.removeOpenGUI(player.getUniqueId());
                    }, 1L);

                    return;
                }

                // Run the buy commands through console
                for (String command : buyCommands) {
                    skyMarket.getServer().dispatchCommand(skyMarket.getServer().getConsoleSender(), PlaceholderAPIUtil.parsePlaceholders(player, command));
                }

                // Create the DecimalFormat
                DecimalFormat df = new DecimalFormat("#.##");
                df.setRoundingMode(RoundingMode.CEILING);

                // Format the buy price
                BigDecimal bigPrice = BigDecimal.valueOf(price);
                String formattedPrice = df.format(bigPrice);

                // Format the player's balance
                BigDecimal bigBalance = BigDecimal.valueOf(skyMarket.getEconomy().getBalance(player));
                String bal = df.format(bigBalance);

                // Create the list of placeholders
                List<TagResolver.Single> successPlaceholders = new ArrayList<>();
                // Add the placeholder for the item purchased
                successPlaceholders.add(Placeholder.parsed("item", name));
                // Add the placeholder for the item's price
                successPlaceholders.add(Placeholder.parsed("price", formattedPrice));
                // Add the placeholder for the player's balance
                successPlaceholders.add(Placeholder.parsed("bal", bal));

                // Send the success message
                player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.buySuccess(), successPlaceholders));

                // Increment the player's buy limit if a limit is configured
                if(limit != null && limit > 0) {
                    playerData.incrementBuyLimit(slot);
                }
            }
        } else {
            boolean containsItems = true;
            for(ItemStack item : buyItems) {
                if (!player.getInventory().containsAtLeast(item, item.getAmount())) {
                    containsItems = false;
                    break;
                }
            }

            if(!containsItems) {
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.insufficientItems()));

                skyMarket.getServer().getScheduler().runTaskLater(skyMarket, () -> {
                    player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

                    guiManager.removeOpenGUI(player.getUniqueId());
                }, 1L);

                return;
            }

            // Remove the items from the player's inventory
            for(ItemStack item : buyItems) {
                player.getInventory().removeItem(item);
            }

            // Run the buy commands through console
            for (String command : buyCommands) {
                skyMarket.getServer().dispatchCommand(skyMarket.getServer().getConsoleSender(), PlaceholderAPIUtil.parsePlaceholders(player, command));
            }

            // Create the DecimalFormat
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);

            // Format the player's balance
            BigDecimal bigBalance = BigDecimal.valueOf(skyMarket.getEconomy().getBalance(player));
            String bal = df.format(bigBalance);

            // Create the list of placeholders
            List<TagResolver.Single> successPlaceholders = new ArrayList<>();
            // Add the placeholder for the item purchased
            successPlaceholders.add(Placeholder.parsed("item", name));
            // Add the placeholder for the item's price
            successPlaceholders.add(Placeholder.parsed("price", ""));
            // Add the placeholder for the player's balance
            successPlaceholders.add(Placeholder.parsed("bal", bal));

            // Create the placeholders for the items removed from the player's inventory
            for(int i = 0; i < buyItems.size() - 1; i++) {
                ItemStack buyStack = buyItems.get(i);

                String item = locale.itemFormat();
                item = item.replace("<item_name>", FormatUtil.formatMaterialName(buyStack.getType()));
                item = item.replace("<item_amount>", String.valueOf(buyStack.getAmount()));

                successPlaceholders.add(Placeholder.parsed("item" + i, item));
            }

            // Send the success message
            player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.buySuccess(), successPlaceholders));

            // Increment the player's buy limit if a limit is configured
            if(limit != null && limit > 0) {
                playerData.incrementBuyLimit(slot);
            }
        }
    }

    /**
     * Used when a button is clicked to sell a command. (Runs a command through console, doesn't take away the player access to the command.)
     * @param player The player selling the command.
     * @param playerData The player's {@link PlayerData}.
     * @param name The name of the command being sold. Taken from the GUI configuration.
     * @param price The price of the command.
     * @param sellCommands The commands to run once the transaction takes place.
     * @param slot The slot of the button clicked.
     * @param limit The limit of how many times this item can be sold.
     */
    public void sellCommand(
            @NotNull Player player,
            @NotNull PlayerData playerData,
            @NotNull String name,
            double price,
            @NotNull List<String> sellCommands,
            int slot,
            @Nullable Integer limit) {
        Locale locale = localeManager.getLocale();

        // Check if the item can be sold according to the sell price
        if(price <= 0) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.unsellable()));
            return;
        }

        // If a limit is configured, compare the player's current buy count to the limit.
        if(limit != null && limit > 0) {
            @Nullable Integer playerLimit = playerData.getSellLimits().get(slot);
            if(playerLimit != null && playerLimit >= limit) {
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.sellLimitReached()));
                return;
            }
        }

        // Give the player the price
        skyMarket.getEconomy().depositPlayer(player, price);

        // Run the sell commands through console
        for(String command : sellCommands) {
            skyMarket.getServer().dispatchCommand(skyMarket.getServer().getConsoleSender(), PlaceholderAPIUtil.parsePlaceholders(player, command));
        }

        // Create the DecimalFormat
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        // Format the buy price
        BigDecimal bigPrice = BigDecimal.valueOf(price);
        String formattedPrice = df.format(bigPrice);

        // Format the player's balance
        BigDecimal bigBalance = BigDecimal.valueOf(skyMarket.getEconomy().getBalance(player));
        String bal = df.format(bigBalance);

        // Create the list of placeholders
        List<TagResolver.Single> successPlaceholders = new ArrayList<>();
        // Add the placeholder for the item purchased
        successPlaceholders.add(Placeholder.parsed("item", name));
        // Add the placeholder for the item's price
        successPlaceholders.add(Placeholder.parsed("price", formattedPrice));
        // Add the placeholder for the player's balance
        successPlaceholders.add(Placeholder.parsed("bal", bal));

        player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.sellSuccess(), successPlaceholders));

        // Increment the player's buy limit if a limit is configured
        if(limit != null && limit > 0) {
            playerData.incrementSellLimit(slot);
        }
    }
}
