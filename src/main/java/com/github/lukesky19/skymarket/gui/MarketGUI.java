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

import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.configuration.manager.ItemsLoader;
import com.github.lukesky19.skymarket.configuration.manager.LocaleLoader;
import com.github.lukesky19.skymarket.configuration.record.Gui;
import com.github.lukesky19.skymarket.configuration.record.Items;
import com.github.lukesky19.skymarket.configuration.record.Locale;
import com.github.lukesky19.skymarket.util.FormatUtil;
import com.github.lukesky19.skymarket.util.PlaceholderAPIUtil;
import com.github.lukesky19.skymarket.util.enums.ActionType;
import com.github.lukesky19.skymarket.util.gui.AbstractGUI;
import com.github.lukesky19.skymarket.util.gui.GUIButton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class MarketGUI extends AbstractGUI {
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

        LinkedHashMap<Integer, Items.Entry> items = itemsLoader.getItems().items();
        List<Integer> keysList = new ArrayList<>(items.keySet());

        for(Map.Entry<Integer, Gui.Entry> guiEntry : guiConfig.gui().entries().entrySet()) {
            Gui.Entry entry = guiEntry.getValue();
            ActionType type = ActionType.valueOf(entry.type());

            switch (type) {
                case FILLER -> {
                    Gui.Item item = entry.item();
                    List<Component> loreList = new ArrayList<>();
                    for (String loreLine : item.lore()) {
                        loreList.add(FormatUtil.format(loreLine));
                    }

                    for (int i = 0; i <= guiConfig.gui().size() - 1; i++) {
                        setButton(i, (new GUIButton.Builder())
                                .setItemStack(new ItemStack(Material.valueOf(item.material())))
                                .setItemName(FormatUtil.format(item.name()))
                                .setLore(loreList)
                                .setAction(event -> {})
                                .build());
                    }
                }

                case RETURN -> {
                    Gui.Item item = entry.item();

                    List<Component> loreList = new ArrayList<>();
                    for (String loreLine : item.lore()) {
                        loreList.add(FormatUtil.format(loreLine));
                    }

                    setButton(entry.slot(), (new GUIButton.Builder())
                            .setItemStack(new ItemStack(Material.valueOf(item.material())))
                            .setItemName(FormatUtil.format(item.name()))
                            .setLore(loreList)
                            .setAction(event -> super.closeInventory(skyMarket, (Player) event.getWhoClicked()))
                            .build());
                }

                case PLACEHOLDER -> {
                    Integer randomKey = keysList.get(new Random().nextInt(keysList.size()));
                    Items.Entry randomEntry = items.get(randomKey);
                    keysList.remove(randomKey);
                    items.remove(randomKey);

                    Items.Item item = randomEntry.item();
                    Items.Prices prices = randomEntry.prices();

                    double buyPrice;
                    double sellPrice;
                    if(prices.buyPriceMax() <= 0.0 && prices.buyPriceMin() <= 0.0) {
                        buyPrice = 0.0;
                    } else {
                        buyPrice = BigDecimal.valueOf(Math.random() * (prices.buyPriceMax() - prices.buyPriceMin()) + prices.buyPriceMin()).setScale(2,  RoundingMode.HALF_UP).doubleValue();
                    }

                    if(prices.sellPriceMax() <= 0.0 && prices.sellPriceMin() <= 0.0) {
                        sellPrice = 0.0;
                    } else {
                        sellPrice = BigDecimal.valueOf(Math.random() * (prices.sellPriceMax() - prices.sellPriceMin()) + prices.sellPriceMin()).setScale(2,  RoundingMode.HALF_UP).doubleValue();
                    }

                    List<TagResolver.Single> placeholders = new ArrayList<>();
                    placeholders.add(Placeholder.parsed("buy_price", String.valueOf(buyPrice)));
                    placeholders.add(Placeholder.parsed("sell_price", String.valueOf(sellPrice)));

                    List<Component> loreList = new ArrayList<>();
                    for (String loreLine : item.lore()) {
                        loreList.add(FormatUtil.format(loreLine, placeholders));
                    }

                    setButton(entry.slot(), (new GUIButton.Builder())
                            .setItemStack(new ItemStack(Material.valueOf(item.material())))
                            .setItemName(FormatUtil.format(item.name(), placeholders))
                            .setLore(loreList)
                            .setAction(event -> {
                                Locale locale = localeLoader.getLocale();
                                Player player = (Player) event.getWhoClicked();

                                if(event.getClick().equals(ClickType.LEFT) || event.getClick().equals(ClickType.SHIFT_LEFT)) {
                                    if(buyPrice <= 0.0) {
                                        event.getWhoClicked().sendMessage(FormatUtil.format(player, locale.prefix() + locale.unbuyable()));
                                        return;
                                    }

                                    ActionType randomType = ActionType.valueOf(randomEntry.type());
                                    if(randomType.equals(ActionType.ITEM)) {
                                        ItemStack buyItem = new ItemStack(Material.valueOf(item.material()));

                                        if (skyMarket.getEconomy().getBalance(player) >= buyPrice) {
                                            skyMarket.getEconomy().withdrawPlayer(player, buyPrice);

                                            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(buyItem);
                                            for(Map.Entry<Integer, ItemStack> leftoverEntry : leftover.entrySet()) {
                                                ItemStack itemStack = leftoverEntry.getValue();
                                                player.getWorld().dropItem(player.getLocation(), itemStack);
                                            }

                                            List<TagResolver.Single> successPlaceholders = new ArrayList<>();
                                            successPlaceholders.add(Placeholder.parsed("amount", String.valueOf(buyItem.getAmount())));
                                            successPlaceholders.add(Placeholder.parsed("item", item.name()));
                                            successPlaceholders.add(Placeholder.parsed("price", String.valueOf(buyPrice)));
                                            successPlaceholders.add(Placeholder.parsed("bal", String.valueOf(skyMarket.getEconomy().getBalance(player))));

                                            event.getWhoClicked().sendMessage(FormatUtil.format(player, locale.prefix() + locale.buySuccess(), successPlaceholders));
                                        } else {
                                            event.getWhoClicked().sendMessage(FormatUtil.format(player, locale.prefix() + locale.insufficientFunds()));
                                            Bukkit.getScheduler().runTaskLater(skyMarket, () -> event.getWhoClicked().closeInventory(), 1L);
                                        }
                                    }

                                    if(randomType.equals(ActionType.COMMAND)) {
                                        if (skyMarket.getEconomy().getBalance(player) >= buyPrice) {
                                            skyMarket.getEconomy().withdrawPlayer(player, buyPrice);

                                            for (String command : randomEntry.commands().buyCommands()) {
                                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPIUtil.parsePlaceholders(player, command));
                                            }

                                            List<TagResolver.Single> successPlaceholders = new ArrayList<>();
                                            successPlaceholders.add(Placeholder.parsed("amount", "1"));
                                            successPlaceholders.add(Placeholder.parsed("item", randomEntry.item().name()));
                                            successPlaceholders.add(Placeholder.parsed("price", String.valueOf(buyPrice)));
                                            successPlaceholders.add(Placeholder.parsed("bal", String.valueOf(skyMarket.getEconomy().getBalance(player))));

                                            event.getWhoClicked().sendMessage(FormatUtil.format(player, locale.prefix() + locale.buySuccess(), successPlaceholders));
                                        } else {
                                            event.getWhoClicked().sendMessage(FormatUtil.format(player, locale.prefix() + locale.insufficientFunds()));
                                            Bukkit.getScheduler().runTaskLater(skyMarket, () -> event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);
                                        }
                                    }
                                }

                                if(event.getClick().equals(ClickType.RIGHT) || event.getClick().equals(ClickType.SHIFT_RIGHT)) {
                                    if (sellPrice <= 0.0) {
                                        event.getWhoClicked().sendMessage(FormatUtil.format(player, locale.prefix() + locale.unsellable()));
                                        return;
                                    }

                                    ActionType randomType = ActionType.valueOf(randomEntry.type());
                                    if (randomType.equals(ActionType.ITEM)) {
                                        ItemStack sellItem = new ItemStack(Material.valueOf(randomEntry.item().material()));

                                        if (player.getInventory().containsAtLeast(sellItem, sellItem.getAmount())) {
                                            player.getInventory().removeItem(sellItem);
                                            skyMarket.getEconomy().depositPlayer(player, sellPrice);

                                            List<TagResolver.Single> successPlaceholders = new ArrayList<>();
                                            successPlaceholders.add(Placeholder.parsed("amount", String.valueOf(sellItem.getAmount())));
                                            successPlaceholders.add(Placeholder.parsed("item", randomEntry.item().name()));
                                            successPlaceholders.add(Placeholder.parsed("price", String.valueOf(sellPrice)));
                                            successPlaceholders.add(Placeholder.parsed("bal", String.valueOf(skyMarket.getEconomy().getBalance(player))));

                                            event.getWhoClicked().sendMessage(FormatUtil.format(player, locale.prefix() + locale.sellSuccess(), successPlaceholders));
                                        } else {
                                            event.getWhoClicked().sendMessage(FormatUtil.format(player, locale.prefix() + locale.notEnoughItems()));
                                            Bukkit.getScheduler().runTaskLater(skyMarket, () -> event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);
                                        }
                                    }

                                    if (randomType.equals(ActionType.COMMAND)) {
                                        skyMarket.getEconomy().depositPlayer(player, sellPrice);

                                        for (String command : randomEntry.commands().buyCommands()) {
                                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPIUtil.parsePlaceholders(player, command));
                                        }

                                        List<TagResolver.Single> successPlaceholders = new ArrayList<>();
                                        successPlaceholders.add(Placeholder.parsed("amount", "1"));
                                        successPlaceholders.add(Placeholder.parsed("item", randomEntry.item().name()));
                                        successPlaceholders.add(Placeholder.parsed("price", String.valueOf(sellPrice)));
                                        successPlaceholders.add(Placeholder.parsed("bal", String.valueOf(skyMarket.getEconomy().getBalance(player))));

                                        event.getWhoClicked().sendMessage(FormatUtil.format(player, locale.prefix() + locale.buySuccess(), successPlaceholders));
                                    }
                                }
                            })
                            .build());
                }
            }
        }

        super.decorate();
    }
}
