/*
    SkyMarket is a shop that rotates it's inventory after a set period of time.
    Copyright (C) 2024  lukeskywlker19

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
package com.github.lukesky19.skymarket.gui;

import com.github.lukesky19.skylib.format.FormatUtil;
import com.github.lukesky19.skylib.format.PlaceholderAPIUtil;
import com.github.lukesky19.skylib.gui.GUIButton;
import com.github.lukesky19.skylib.gui.InventoryGUI;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.configuration.manager.ItemsLoader;
import com.github.lukesky19.skymarket.configuration.manager.LocaleLoader;
import com.github.lukesky19.skymarket.configuration.record.Gui;
import com.github.lukesky19.skymarket.configuration.record.Items;
import com.github.lukesky19.skymarket.configuration.record.Locale;
import com.github.lukesky19.skymarket.enums.ActionType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class MarketGUI extends InventoryGUI {
    private final SkyMarket skyMarket;
    private final LocaleLoader localeLoader;
    private final ItemsLoader itemsLoader;
    private final Gui guiConfig;

    public MarketGUI(
            SkyMarket skyMarket,
            LocaleLoader localeLoader,
            ItemsLoader itemsLoader,
            Gui guiConfig) {
        this.skyMarket = skyMarket;
        this.localeLoader = localeLoader;
        this.itemsLoader = itemsLoader;
        this.guiConfig = guiConfig;

        createInventory();
        decorate();
    }

    public void createInventory() {
        int shopSize = guiConfig.gui().size();
        Component shopName = FormatUtil.format(guiConfig.gui().name());
        setInventory(skyMarket.getServer().createInventory(this, shopSize, shopName));
    }

    public void decorate() {
        clearButtons();

        if(itemsLoader.getItems() == null) return;

        LinkedHashMap<Integer, Items.Entry> items = itemsLoader.getItems().items();
        List<Integer> keysList = new ArrayList<>(items.keySet());

        for(Map.Entry<Integer, Gui.Entry> guiEntry : guiConfig.gui().entries().entrySet()) {
            Gui.Entry entry = guiEntry.getValue();
            ActionType type = ActionType.valueOf(entry.type());

            switch (type) {
                case FILLER -> {
                    Gui.Item item = entry.item();
                    Material material = Material.getMaterial(item.material());

                    if (material != null) {
                        List<Component> lore = item.lore().stream().map(FormatUtil::format).toList();

                        GUIButton.Builder builder = new GUIButton.Builder();

                        builder.setMaterial(material);

                        if(item.name() != null) {
                            builder.setItemName(FormatUtil.format(item.name()));
                        }

                        builder.setLore(lore);

                        GUIButton button = builder.build();

                        for (int i = 0; i <= guiConfig.gui().size() - 1; i++) {
                            setButton(i, button);
                        }
                    }
                }

                case RETURN -> {
                    Gui.Item item = entry.item();
                    Material material = Material.getMaterial(item.material());

                    if(material != null) {
                        List<Component> lore = item.lore().stream().map(FormatUtil::format).toList();

                        GUIButton.Builder builder = new GUIButton.Builder();

                        builder.setMaterial(material);

                        if(item.name() != null) {
                            builder.setItemName(FormatUtil.format(item.name()));
                        }

                        builder.setLore(lore);

                        builder.setAction(event -> closeInventory(skyMarket, (Player) event.getWhoClicked()));

                        setButton(entry.slot(), builder.build());
                    }
                }

                case PLACEHOLDER -> {
                    Integer randomKey = keysList.get(new Random().nextInt(keysList.size()));
                    Items.Entry randomEntry = items.get(randomKey);
                    keysList.remove(randomKey);
                    items.remove(randomKey);

                    Items.Item item = randomEntry.item();
                    Items.Prices prices = randomEntry.prices();

                    Material material = Material.getMaterial(item.material());

                    if(material != null) {
                        double buyPrice;
                        double sellPrice;
                        if (prices.buyPriceMax() <= 0.0 && prices.buyPriceMin() <= 0.0) {
                            buyPrice = 0.0;
                        } else {
                            buyPrice = BigDecimal.valueOf(Math.random() * (prices.buyPriceMax() - prices.buyPriceMin()) + prices.buyPriceMin()).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        }

                        if (prices.sellPriceMax() <= 0.0 && prices.sellPriceMin() <= 0.0) {
                            sellPrice = 0.0;
                        } else {
                            sellPrice = BigDecimal.valueOf(Math.random() * (prices.sellPriceMax() - prices.sellPriceMin()) + prices.sellPriceMin()).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        }

                        List<TagResolver.Single> placeholders = List.of(
                                Placeholder.parsed("buy_price", String.valueOf(buyPrice)),
                                Placeholder.parsed("sell_price", String.valueOf(sellPrice)));

                        List<Component> lore = item.lore().stream().map(line -> FormatUtil.format(line, placeholders)).toList();


                        GUIButton.Builder builder = new GUIButton.Builder();

                        builder.setMaterial(material);

                        if(item.name() != null) {
                            builder.setItemName(FormatUtil.format(item.name()));
                        }

                        builder.setLore(lore);

                        builder.setAction(event -> {
                            Locale locale = localeLoader.getLocale();
                            Player player = (Player) event.getWhoClicked();

                            switch(event.getClick()) {
                                case LEFT, SHIFT_LEFT -> {
                                    if (buyPrice <= 0.0) {
                                        event.getWhoClicked().sendMessage(FormatUtil.format(player, locale.prefix() + locale.unbuyable()));
                                        return;
                                    }

                                    ActionType randomType = ActionType.valueOf(randomEntry.type());
                                    switch(randomType) {
                                        case ITEM -> buyItem(player, material, item.name(), buyPrice);

                                        case COMMAND -> buyCommand(player, item.name(), buyPrice, randomEntry.commands().buyCommands());
                                    }
                                }

                                case RIGHT, SHIFT_RIGHT -> {
                                    if (sellPrice <= 0.0) {
                                        event.getWhoClicked().sendMessage(FormatUtil.format(player, locale.prefix() + locale.unsellable()));
                                        return;
                                    }

                                    ActionType randomType = ActionType.valueOf(randomEntry.type());
                                    switch(randomType) {
                                        case ITEM -> sellItem(player,material, item.name(), sellPrice);

                                        case COMMAND -> sellCommand(player, item.name(), sellPrice, randomEntry.commands().sellCommands());
                                    }
                                }
                            }
                        });

                        setButton(entry.slot(), builder.build());
                    }
                }
            }
        }

        super.decorate();
    }

    /**
     * This method contains the logic to purchase an item.
     * @param player The player buying.
     * @param material The material being purchased.
     * @param itemName The name of the item being purchased.
     * @param price The price of the item being purchased.
     */
    private void buyItem(Player player, Material material, String itemName, double price) {
        Locale locale = localeLoader.getLocale();

        ItemStack buyItem = new ItemStack(material);

        if (skyMarket.getEconomy().getBalance(player) >= price) {
            skyMarket.getEconomy().withdrawPlayer(player, price);

            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(buyItem);
            for (Map.Entry<Integer, ItemStack> leftoverEntry : leftover.entrySet()) {
                ItemStack itemStack = leftoverEntry.getValue();
                player.getWorld().dropItem(player.getLocation(), itemStack);
            }

            List<TagResolver.Single> successPlaceholders = new ArrayList<>();
            successPlaceholders.add(Placeholder.parsed("amount", String.valueOf(buyItem.getAmount())));
            successPlaceholders.add(Placeholder.parsed("item", itemName));
            successPlaceholders.add(Placeholder.parsed("price", String.valueOf(price)));
            successPlaceholders.add(Placeholder.parsed("bal", String.valueOf(skyMarket.getEconomy().getBalance(player))));

            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.buySuccess(), successPlaceholders));
        } else {
            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.insufficientFunds()));
            Bukkit.getScheduler().runTaskLater(skyMarket, () -> player.closeInventory(), 1L);
        }
    }

    /**
     * This method contains the logic to sell an item.
     * @param player The player selling.
     * @param material The material being sold
     * @param itemName The name of the item being sold.
     * @param price The price of the item being sold.
     */
    private void sellItem(Player player, Material material, String itemName, double price) {
        Locale locale = localeLoader.getLocale();

        ItemStack sellItem = new ItemStack(material);

        if (player.getInventory().containsAtLeast(sellItem, sellItem.getAmount())) {
            player.getInventory().removeItem(sellItem);
            skyMarket.getEconomy().depositPlayer(player, price);

            List<TagResolver.Single> successPlaceholders = new ArrayList<>();
            successPlaceholders.add(Placeholder.parsed("amount", String.valueOf(sellItem.getAmount())));
            successPlaceholders.add(Placeholder.parsed("item", itemName));
            successPlaceholders.add(Placeholder.parsed("price", String.valueOf(price)));
            successPlaceholders.add(Placeholder.parsed("bal", String.valueOf(skyMarket.getEconomy().getBalance(player))));

            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.sellSuccess(), successPlaceholders));
        } else {
            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.notEnoughItems()));
            Bukkit.getScheduler().runTaskLater(skyMarket, () -> closeInventory(skyMarket, player), 1L);
        }
    }

    /**
     * This method contains the logic to purchase a command.
     * @param player The player buying.
     * @param name The name of the command being purchased.
     * @param price The price of the command being purchased.
     * @param buyCommands The list of commands to run.
     */
    private void buyCommand(Player player, String name, double price, List<String> buyCommands) {
        Locale locale = localeLoader.getLocale();

        if (skyMarket.getEconomy().getBalance(player) >= price) {
            for (String command : buyCommands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPIUtil.parsePlaceholders(player, command));
            }

            skyMarket.getEconomy().withdrawPlayer(player, price);

            List<TagResolver.Single> successPlaceholders = new ArrayList<>();
            successPlaceholders.add(Placeholder.parsed("amount", String.valueOf(1)));
            successPlaceholders.add(Placeholder.parsed("item", name));
            successPlaceholders.add(Placeholder.parsed("price", String.valueOf(price)));
            successPlaceholders.add(Placeholder.parsed("bal", String.valueOf(skyMarket.getEconomy().getBalance(player))));

            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.buySuccess(), successPlaceholders));
        } else {
            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.insufficientFunds()));
            Bukkit.getScheduler().runTaskLater(skyMarket, () -> closeInventory(skyMarket, player), 1L);
        }
    }

    /**
     * This method contains the logic to sell a command.
     * @param player The player selling.
     * @param name The name of whatever is being purchased.
     * @param price The price of the item being sold.
     * @param sellCommands The list of commands to run.
     */
    private void sellCommand(Player player, String name, double price, List<String> sellCommands) {
        Locale locale = localeLoader.getLocale();

        skyMarket.getEconomy().depositPlayer(player, price);

        for(String command : sellCommands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPIUtil.parsePlaceholders(player, command));
        }

        List<TagResolver.Single> successPlaceholders = new ArrayList<>();
        successPlaceholders.add(Placeholder.parsed("amount", String.valueOf(1)));
        successPlaceholders.add(Placeholder.parsed("item", name));
        successPlaceholders.add(Placeholder.parsed("price", String.valueOf(price)));
        successPlaceholders.add(Placeholder.parsed("bal", String.valueOf(skyMarket.getEconomy().getBalance(player))));

        player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.sellSuccess(), successPlaceholders));
    }
}
