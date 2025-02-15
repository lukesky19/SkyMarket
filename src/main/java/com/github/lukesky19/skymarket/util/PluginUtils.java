package com.github.lukesky19.skymarket.util;

import com.github.lukesky19.skylib.format.FormatUtil;
import com.github.lukesky19.skylib.format.PlaceholderAPIUtil;
import com.github.lukesky19.skylib.gui.GUIType;
import com.github.lukesky19.skylib.gui.GUIButton;
import com.github.lukesky19.skylib.player.PlayerUtil;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.configuration.record.Item;
import com.github.lukesky19.skymarket.configuration.record.Locale;
import com.github.lukesky19.skymarket.configuration.record.gui.Chest;
import com.github.lukesky19.skymarket.configuration.record.gui.Merchant;
import com.github.lukesky19.skymarket.enums.ActionType;
import com.github.lukesky19.skymarket.manager.MarketManager;
import io.papermc.paper.potion.SuspiciousEffectEntry;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

/**
 * This class has a bunch of methods to purchase items, commands, create GUIButtons, ItemStacks, and MerchantRecipes.
 */
public class PluginUtils {
    /**
     * Used when a button is clicked to purchase an item.
     * @param skyMarket A SkyMarket plugin instance.
     * @param marketManager A MarketManager instance.
     * @param locale The plugin's locale.
     * @param marketId The market id for the market where the purchase is being made.
     * @param player The player purchasing the item.
     * @param itemStack The item to purchase.
     * @param price The buy price of the item.
     * @param buyItems The items to take in exchange for the item.
     * @param slot The slot of the button clicked.
     * @param limit The limit of how many times this item can be purchased.
     */
    public static void buyItem(SkyMarket skyMarket, MarketManager marketManager, Locale locale, String marketId, Player player, ItemStack itemStack, double price, List<ItemStack> buyItems, int slot, int limit) {
        if(limit != -1) {
            HashMap<Integer, Integer> map = marketManager.getBuyButtonLimits(marketId, player.getUniqueId());
            if (map != null) {
                Integer playerLimit = map.get(slot);
                if(playerLimit != null && playerLimit >= limit) {
                    player.sendMessage(FormatUtil.format(locale.prefix() + locale.buyLimitReached()));
                    return;
                }
            } else {
                throw new RuntimeException("No limit mapping for market id " + marketId);
            }
        }

        if(price == -1 && buyItems.isEmpty()) {
            player.sendMessage(FormatUtil.format(locale.prefix() + locale.unbuyable()));
            return;
        }

        if(price != -1) {
            if(!buyItems.isEmpty()) {
                if(skyMarket.getEconomy().getBalance(player) >= price) {
                    boolean containsItems = true;
                    for(ItemStack item : buyItems) {
                        if(!player.getInventory().containsAtLeast(item, item.getAmount())) {
                            containsItems = false;
                            break;
                        }
                    }

                    if(!containsItems) {
                        player.sendMessage(FormatUtil.format(locale.prefix() + locale.insufficientItems()));
                        Bukkit.getScheduler().runTaskLater(skyMarket, () -> player.closeInventory(), 1L);
                        return;
                    }

                    skyMarket.getEconomy().withdrawPlayer(player, price);

                    for(ItemStack item : buyItems) {
                        player.getInventory().removeItem(item);
                    }

                    PlayerUtil.giveItem(player.getInventory(), itemStack, itemStack.getAmount(), player.getLocation());

                    String playerItem = locale.itemFormat();
                    playerItem = playerItem.replace("<item_name>", FormatUtil.formatMaterialNameLowercase(itemStack.getType().name()));
                    playerItem = playerItem.replace("<item_amount>", String.valueOf(itemStack.getAmount()));

                    DecimalFormat df = new DecimalFormat("#.##");
                    df.setRoundingMode(RoundingMode.CEILING);

                    BigDecimal bigPrice = BigDecimal.valueOf(price);
                    String formattedPrice = df.format(bigPrice);

                    BigDecimal bigBalance = BigDecimal.valueOf(skyMarket.getEconomy().getBalance(player));
                    String bal = df.format(bigBalance);

                    List<TagResolver.Single> successPlaceholders = new ArrayList<>();
                    successPlaceholders.add(Placeholder.parsed("item", playerItem));
                    successPlaceholders.add(Placeholder.parsed("price", formattedPrice));
                    successPlaceholders.add(Placeholder.parsed("bal", bal));

                    for(int i = 0; i < buyItems.size() - 1; i++) {
                        ItemStack buyStack = buyItems.get(i);

                        String item = locale.itemFormat();
                        item = item.replace("<item_name>", FormatUtil.formatMaterialNameLowercase(buyStack.getType().name()));
                        item = item.replace("<item_amount>", String.valueOf(buyStack.getAmount()));

                        successPlaceholders.add(Placeholder.parsed("item" + i, item));
                    }

                    player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.buySuccess(), successPlaceholders));

                    if(limit != -1) {
                        marketManager.addToBuyButtonLimit(marketId, player.getUniqueId(), slot);
                    }
                } else {
                    player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.insufficientFunds()));
                    Bukkit.getScheduler().runTaskLater(skyMarket, () -> player.closeInventory(), 1L);
                }
            } else {
                if(skyMarket.getEconomy().getBalance(player) >= price) {
                    skyMarket.getEconomy().withdrawPlayer(player, price);

                    PlayerUtil.giveItem(player.getInventory(), itemStack, itemStack.getAmount(), player.getLocation());

                    String playerItem = locale.itemFormat();
                    playerItem = playerItem.replace("<item_name>", FormatUtil.formatMaterialNameLowercase(itemStack.getType().name()));
                    playerItem = playerItem.replace("<item_amount>", String.valueOf(itemStack.getAmount()));

                    DecimalFormat df = new DecimalFormat("#.##");
                    df.setRoundingMode(RoundingMode.CEILING);

                    BigDecimal bigPrice = BigDecimal.valueOf(price);
                    String formattedPrice = df.format(bigPrice);

                    BigDecimal bigBalance = BigDecimal.valueOf(skyMarket.getEconomy().getBalance(player));
                    String bal = df.format(bigBalance);

                    List<TagResolver.Single> successPlaceholders = new ArrayList<>();
                    successPlaceholders.add(Placeholder.parsed("item", playerItem));
                    successPlaceholders.add(Placeholder.parsed("price", formattedPrice));
                    successPlaceholders.add(Placeholder.parsed("bal", bal));

                    player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.buySuccess(), successPlaceholders));

                    if(limit != -1) {
                        marketManager.addToBuyButtonLimit(marketId, player.getUniqueId(), slot);
                    }
                } else {
                    player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.insufficientFunds()));
                    Bukkit.getScheduler().runTaskLater(skyMarket, () -> player.closeInventory(), 1L);
                }
            }
        } else {
            boolean containsItems = true;
            for(ItemStack item : buyItems) {
                if(!player.getInventory().containsAtLeast(item, item.getAmount())) {
                    containsItems = false;
                    break;
                }
            }

            if(!containsItems) {
                player.sendMessage(FormatUtil.format(locale.prefix() + "<red>You do not have enough items to purchase this item!</red>"));
                Bukkit.getScheduler().runTaskLater(skyMarket, () -> player.closeInventory(), 1L);
                return;
            }

            for(ItemStack item : buyItems) {
                player.getInventory().removeItem(item);
            }

            PlayerUtil.giveItem(player.getInventory(), itemStack, itemStack.getAmount(), player.getLocation());

            String playerItem = locale.itemFormat();
            playerItem = playerItem.replace("<item_name>", FormatUtil.formatMaterialNameLowercase(itemStack.getType().name()));
            playerItem = playerItem.replace("<item_amount>", String.valueOf(itemStack.getAmount()));

            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);

            BigDecimal bigBalance = BigDecimal.valueOf(skyMarket.getEconomy().getBalance(player));
            String bal = df.format(bigBalance);

            List<TagResolver.Single> successPlaceholders = new ArrayList<>();
            successPlaceholders.add(Placeholder.parsed("item", playerItem));
            successPlaceholders.add(Placeholder.parsed("price", ""));
            successPlaceholders.add(Placeholder.parsed("bal", bal));

            for(int i = 0; i < buyItems.size() - 1; i++) {
                ItemStack buyStack = buyItems.get(i);

                String item = locale.itemFormat();
                item = item.replace("<item_name>", FormatUtil.formatMaterialNameLowercase(buyStack.getType().name()));
                item = item.replace("<item_amount>", String.valueOf(buyStack.getAmount()));

                successPlaceholders.add(Placeholder.parsed("item" + i, item));
            }

            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.buySuccess(), successPlaceholders));

            if(limit != -1) {
                marketManager.addToBuyButtonLimit(marketId, player.getUniqueId(), slot);
            }
        }
    }

    /**
     * Used when a button is clicked to sell an item.
     * @param skyMarket A SkyMarket plugin instance.
     * @param marketManager A MarketManager instance.
     * @param locale The plugin's locale.
     * @param marketId The market id for the market where the item being sold is being made.
     * @param player The player selling the item.
     * @param itemStack The item to sell.
     * @param price The sell price of the item.
     * @param slot The slot of the button clicked.
     * @param limit The limit of how many times this item can be sold.
     */
    public static void sellItem(SkyMarket skyMarket, MarketManager marketManager, Locale locale,  String marketId, Player player, ItemStack itemStack, double price, int slot, int limit) {
        if(limit != -1) {
            HashMap<Integer, Integer> map = marketManager.getSellButtonLimits(marketId, player.getUniqueId());
            if (map != null) {
                Integer playerLimit = map.get(slot);
                if(playerLimit != null && playerLimit >= limit) {
                    player.sendMessage(FormatUtil.format(locale.prefix() + locale.sellLimitReached()));
                    return;
                }
            } else {
                throw new RuntimeException("No limit mapping for market id " + marketId);
            }
        }

        if(price == -1.0) {
            player.sendMessage(FormatUtil.format(locale.prefix() + locale.unsellable()));
            return;
        }

        if(player.getInventory().containsAtLeast(itemStack, itemStack.getAmount())) {
            player.getInventory().removeItem(itemStack);
            skyMarket.getEconomy().depositPlayer(player, price);

            String playerItem = locale.itemFormat();
            playerItem = playerItem.replace("<item_name>", FormatUtil.formatMaterialNameLowercase(itemStack.getType().name()));
            playerItem = playerItem.replace("<item_amount>", String.valueOf(itemStack.getAmount()));

            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);

            BigDecimal bigBalance = BigDecimal.valueOf(skyMarket.getEconomy().getBalance(player));
            String bal = df.format(bigBalance);

            List<TagResolver.Single> successPlaceholders = new ArrayList<>();
            successPlaceholders.add(Placeholder.parsed("item", playerItem));
            successPlaceholders.add(Placeholder.parsed("price", ""));
            successPlaceholders.add(Placeholder.parsed("bal", bal));

            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.sellSuccess(), successPlaceholders));

            if(limit != -1) {
                marketManager.addToSellButtonLimit(marketId, player.getUniqueId(), slot);
            }
        } else {
            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.notEnoughItems()));
            Bukkit.getScheduler().runTaskLater(skyMarket, () -> player.closeInventory(), 1L);
        }
    }

    /**
     * Used when a button is clicked to buy a command. (Runs a command through console, doesn't give the player access to the command.)
     * @param skyMarket A SkyMarket plugin instance.
     * @param marketManager A MarketManager instance.
     * @param locale The plugin's locale.
     * @param marketId The market id for the market where the command being purchased is being made.
     * @param player The player buying the command.
     * @param name The name of the command being purchased. Taken from the GUI configuration.
     * @param price The price of the command.
     * @param buyItems The items to take in exchange for the command.
     * @param buyCommands The commands to run once the transaction takes place.
     * @param slot The slot of the button clicked.
     * @param limit The limit of how many times this item can be purchased.
     */
    public static void buyCommand(SkyMarket skyMarket, MarketManager marketManager, Locale locale, String marketId, Player player, String name, double price, List<ItemStack> buyItems, List<String> buyCommands, int slot, int limit) {
        if(limit != -1) {
            HashMap<Integer, Integer> map = marketManager.getBuyButtonLimits(marketId, player.getUniqueId());
            if(map != null) {
                Integer playerLimit = map.get(slot);
                if(playerLimit != null && playerLimit >= limit) {
                    player.sendMessage(FormatUtil.format(locale.prefix() + locale.buyLimitReached()));
                    return;
                }
            } else {
                throw new RuntimeException("No limit mapping for market id " + marketId);
            }
        }

        if(price == -1 && buyItems.isEmpty()) {
            player.sendMessage(FormatUtil.format(locale.prefix() + locale.unbuyable()));
            return;
        }

        if(price != -1) {
            if(!buyItems.isEmpty()) {
                boolean containsItems = true;
                for(ItemStack item : buyItems) {
                    if(!player.getInventory().containsAtLeast(item, item.getAmount())) {
                        containsItems = false;
                        break;
                    }
                }

                if(!containsItems) {
                    player.sendMessage(FormatUtil.format(locale.prefix() + locale.insufficientItems()));
                    Bukkit.getScheduler().runTaskLater(skyMarket, () -> player.closeInventory(), 1L);
                    return;
                }

                if (skyMarket.getEconomy().getBalance(player) >= price) {
                    for(ItemStack item : buyItems) {
                        player.getInventory().removeItem(item);
                    }

                    skyMarket.getEconomy().withdrawPlayer(player, price);

                    for (String command : buyCommands) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPIUtil.parsePlaceholders(player, command));
                    }

                    DecimalFormat df = new DecimalFormat("#.##");
                    df.setRoundingMode(RoundingMode.CEILING);

                    BigDecimal bigPrice = BigDecimal.valueOf(price);
                    String formattedPrice = df.format(bigPrice);

                    BigDecimal bigBalance = BigDecimal.valueOf(skyMarket.getEconomy().getBalance(player));
                    String bal = df.format(bigBalance);

                    List<TagResolver.Single> successPlaceholders = new ArrayList<>();
                    successPlaceholders.add(Placeholder.parsed("item", name));
                    successPlaceholders.add(Placeholder.parsed("price", formattedPrice));
                    successPlaceholders.add(Placeholder.parsed("bal", bal));

                    for(int i = 0; i < buyItems.size() - 1; i++) {
                        ItemStack buyStack = buyItems.get(i);

                        String item = locale.itemFormat();
                        item = item.replace("<item_name>", FormatUtil.formatMaterialNameLowercase(buyStack.getType().name()));
                        item = item.replace("<item_amount>", String.valueOf(buyStack.getAmount()));

                        successPlaceholders.add(Placeholder.parsed("item" + i, item));
                    }

                    player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.buySuccess(), successPlaceholders));

                    if(limit != -1) {
                        marketManager.addToBuyButtonLimit(marketId, player.getUniqueId(), slot);
                    }
                } else {
                    player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.insufficientFunds()));
                    Bukkit.getScheduler().runTaskLater(skyMarket, () -> player.closeInventory(), 1L);
                }
            } else {
                if(skyMarket.getEconomy().getBalance(player) >= price) {
                    skyMarket.getEconomy().withdrawPlayer(player, price);

                    for (String command : buyCommands) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPIUtil.parsePlaceholders(player, command));
                    }

                    DecimalFormat df = new DecimalFormat("#.##");
                    df.setRoundingMode(RoundingMode.CEILING);

                    BigDecimal bigPrice = BigDecimal.valueOf(price);
                    String formattedPrice = df.format(bigPrice);

                    BigDecimal bigBalance = BigDecimal.valueOf(skyMarket.getEconomy().getBalance(player));
                    String bal = df.format(bigBalance);

                    List<TagResolver.Single> successPlaceholders = new ArrayList<>();
                    successPlaceholders.add(Placeholder.parsed("item", name));
                    successPlaceholders.add(Placeholder.parsed("price", formattedPrice));
                    successPlaceholders.add(Placeholder.parsed("bal", bal));

                    for(int i = 0; i < buyItems.size() - 1; i++) {
                        ItemStack buyStack = buyItems.get(i);

                        String item = locale.itemFormat();
                        item = item.replace("<item_name>", FormatUtil.formatMaterialNameLowercase(buyStack.getType().name()));
                        item = item.replace("<item_amount>", String.valueOf(buyStack.getAmount()));

                        successPlaceholders.add(Placeholder.parsed("item" + i, item));
                    }

                    player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.buySuccess(), successPlaceholders));

                    if(limit != -1) {
                        marketManager.addToBuyButtonLimit(marketId, player.getUniqueId(), slot);
                    }
                } else {
                    player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.insufficientFunds()));
                    Bukkit.getScheduler().runTaskLater(skyMarket, () -> player.closeInventory(), 1L);
                }
            }
        } else {
            boolean containsItems = true;
            for(ItemStack item : buyItems) {
                if(!player.getInventory().containsAtLeast(item, item.getAmount())) {
                    containsItems = false;
                    break;
                }
            }

            if(!containsItems) {
                player.sendMessage(FormatUtil.format(locale.prefix() + locale.insufficientItems()));
                Bukkit.getScheduler().runTaskLater(skyMarket, () -> player.closeInventory(), 1L);
                return;
            }

            for(ItemStack item : buyItems) {
                player.getInventory().removeItem(item);
            }

            for (String command : buyCommands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPIUtil.parsePlaceholders(player, command));
            }
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);

            BigDecimal bigBalance = BigDecimal.valueOf(skyMarket.getEconomy().getBalance(player));
            String bal = df.format(bigBalance);

            List<TagResolver.Single> successPlaceholders = new ArrayList<>();
            successPlaceholders.add(Placeholder.parsed("item", name));
            successPlaceholders.add(Placeholder.parsed("price", ""));
            successPlaceholders.add(Placeholder.parsed("bal", bal));

            for(int i = 0; i < buyItems.size() - 1; i++) {
                ItemStack buyStack = buyItems.get(i);

                String item = locale.itemFormat();
                item = item.replace("<item_name>", FormatUtil.formatMaterialNameLowercase(buyStack.getType().name()));
                item = item.replace("<item_amount>", String.valueOf(buyStack.getAmount()));

                successPlaceholders.add(Placeholder.parsed("item" + i, item));
            }

            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.buySuccess(), successPlaceholders));

            if(limit != -1) {
                marketManager.addToBuyButtonLimit(marketId, player.getUniqueId(), slot);
            }
        }


    }

    /**
     * Used when a button is clicked to sell a command. (Runs a command through console, doesn't take away the player access to the command.)
     * @param skyMarket A SkyMarket plugin instance.
     * @param marketManager A MarketManager instance.
     * @param locale The plugin's locale.
     * @param marketId The market id for the market where the command being sold is being made.
     * @param player The player selling the command.
     * @param name The name of the command being sold. Taken from the GUI configuration.
     * @param price The price of the command.
     * @param sellCommands The commands to run once the transaction takes place.
     * @param slot The slot of the button clicked.
     * @param limit The limit of how many times this item can be sold.
     */
    public static void sellCommand(SkyMarket skyMarket, MarketManager marketManager, Locale locale, String marketId, Player player, String name, double price, List<String> sellCommands, int slot, int limit) {
        if(limit != -1) {
            HashMap<Integer, Integer> map = marketManager.getSellButtonLimits(marketId, player.getUniqueId());
            if (map != null) {
                Integer playerLimit = map.get(slot);
                if(playerLimit != null && playerLimit >= limit) {
                    player.sendMessage(FormatUtil.format(locale.prefix() + locale.sellLimitReached()));
                    return;
                }
            } else {
                throw new RuntimeException("No limit mapping for market id " + marketId);
            }
        }

        if(price == -1.0) {
            player.sendMessage(FormatUtil.format(locale.prefix() + locale.unsellable()));
            return;
        }

        skyMarket.getEconomy().depositPlayer(player, price);

        for(String command : sellCommands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPIUtil.parsePlaceholders(player, command));
        }

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        BigDecimal bigPrice = BigDecimal.valueOf(price);
        String formattedPrice = df.format(bigPrice);

        BigDecimal bigBalance = BigDecimal.valueOf(skyMarket.getEconomy().getBalance(player));
        String bal = df.format(bigBalance);

        List<TagResolver.Single> successPlaceholders = new ArrayList<>();
        successPlaceholders.add(Placeholder.parsed("item", name));
        successPlaceholders.add(Placeholder.parsed("price", formattedPrice));
        successPlaceholders.add(Placeholder.parsed("bal", bal));

        player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.sellSuccess(), successPlaceholders));

        if(limit != -1) {
            marketManager.addToSellButtonLimit(marketId, player.getUniqueId(), slot);
        }
    }

    /**
     * Gets the ItemStack to display inside a GUI.
     * @param skyMarket A SkyMarket plugin instance.
     * @param item The Item configuration that will be used to build the ItemStack.
     * @param placeholders The placeholders to replace in the lore of the item.
     * @return An ItemStack or null if it failed to be created.
     */
    @Nullable
    private static ItemStack getDisplayItem(SkyMarket skyMarket, Item item, List<TagResolver.Single> placeholders) {
        ComponentLogger logger = skyMarket.getComponentLogger();

        if(item.material() == null) return null;

        Material material = Material.getMaterial(item.material());
        if (material == null) return null;

        ItemStack itemStack = new ItemStack(material);

        if(item.amount().fixed() != null) {
            itemStack.setAmount(item.amount().fixed());
        } else {
            if(item.amount().max() != null && item.amount().min() != null) {
                int amount = (int) (Math.random() * (item.amount().max() - item.amount().min()) + item.amount().min());
                itemStack.setAmount(amount);
            }
        }

        if(item.name() != null) {
            ItemMeta fillerMeta = itemStack.getItemMeta();
            fillerMeta.displayName(FormatUtil.format(item.name()));
            itemStack.setItemMeta(fillerMeta);
        }

        List<Component> lore = item.lore().stream().map(line -> FormatUtil.format(line, placeholders)).toList();
        itemStack.lore(lore);

        for (String flagName : item.itemFlags()) {
            try {
                ItemFlag flag = ItemFlag.valueOf(flagName);
                itemStack.addItemFlags(flag);
            } catch (IllegalArgumentException ignored) {
                logger.warn(FormatUtil.format("Unknown item flag: " + flagName));
            }
        }

        return setItemMeta(logger, item, itemStack);
    }

    /**
     * Gets the ItemStack to be given to the Player.
     * @param skyMarket A SkyMarket plugin instance.
     * @param item The Item configuration that will be used to build the ItemStack.
     * @param amount The amount of items this ItemStack should have.
     * @return An ItemStack or null if it failed to be created.
     */
    @Nullable
    private static ItemStack getPlayerItem(SkyMarket skyMarket, Item item, int amount) {
        ComponentLogger logger = skyMarket.getComponentLogger();

        if(item.material() == null) return null;

        Material material = Material.getMaterial(item.material());
        if (material == null) return null;

        ItemStack itemStack = new ItemStack(material);
        itemStack.setAmount(amount);

        return setItemMeta(logger, item, itemStack);
    }

    /**
     * Gets the ItemStack to be given to the Player.
     *
     * @param skyMarket A SkyMarket plugin instance.
     * @param item The Item configuration that will be used to build the ItemStack.
     * @return An ItemStack or null if it failed to be created.
     */
    @Nullable
    private static ItemStack getPlayerItem(SkyMarket skyMarket, Item item) {
        ComponentLogger logger = skyMarket.getComponentLogger();

        if(item.material() == null) return null;

        Material material = Material.getMaterial(item.material());
        if (material == null) return null;

        ItemStack itemStack = new ItemStack(material);

        if(item.amount().fixed() != null) {
            itemStack.setAmount(item.amount().fixed());
        } else {
            if(item.amount().max() != null && item.amount().min() != null) {
                int amount = (int) (Math.random() * (item.amount().max() - item.amount().min()) + item.amount().min());
                itemStack.setAmount(amount);
            }
        }

        return setItemMeta(logger, item, itemStack);
    }

    /**
     * Used to set the ItemStack's ItemMeta.
     * @param logger A ComponentLogger (it should come from the plugin running this method.)
     * @param item The Item configuration that will be used to set the ItemMeta.
     * @param itemStack The ItemStack to set the ItemMeta for.
     * @return An ItemStack with the updated ItemMeta set.
     */
    @NotNull
    private static ItemStack setItemMeta(ComponentLogger logger, Item item, ItemStack itemStack) {
        // Get the Enchantment Registry
        Registry<@NotNull Enchantment> enchantmentRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        // Get the Potion Registries
        Registry<@NotNull PotionType> potionTypeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.POTION);
        Registry<@NotNull PotionEffectType> potionEffectRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.MOB_EFFECT);

        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setEnchantmentGlintOverride(item.enchantmentGlint());
        itemStack.setItemMeta(itemMeta);

        switch (itemMeta) {
            case SuspiciousStewMeta susMeta -> {
                for (Item.PotionType effect : item.stewEffects()) {
                    if (effect.type() == null) continue;

                    NamespacedKey effectKey = NamespacedKey.fromString(effect.type());
                    if (effectKey == null) {
                        logger.warn(FormatUtil.format("Unknown stew effect: " + effect.type()));
                        continue;
                    }

                    PotionEffectType type = potionEffectRegistry.get(effectKey);
                    if (type == null) {
                        logger.warn(FormatUtil.format("Unknown stew effect: " + effect.type()));
                        continue;
                    }

                    SuspiciousEffectEntry susEntry = SuspiciousEffectEntry.create(type, (int) (effect.duration() * 20));
                    susMeta.addCustomEffect(susEntry, true);
                }

                itemStack.setItemMeta(susMeta);
            }

            case PotionMeta potionMeta -> {
                for (Item.PotionEffect effect : item.potionEffects()) {
                    if (potionMeta.getBasePotionType() == null) {
                        if (effect.baseEffect() != null) {
                            if(effect.baseEffect().type() != null) {
                                NamespacedKey effectKey = NamespacedKey.fromString(effect.baseEffect().type());
                                if (effectKey == null) {
                                    logger.warn(FormatUtil.format("Unknown key for base effect type " + effect.baseEffect().type()));
                                    continue;
                                }

                                PotionType potionType = potionTypeRegistry.get(effectKey);
                                if (potionType != null) {
                                    potionMeta.setBasePotionType(potionType);
                                } else {
                                    logger.warn(FormatUtil.format("Unknown potion type for base effect type " + effect.baseEffect().type()));
                                    continue;
                                }
                            }
                        }
                    }

                    if(effect.extraEffects() != null) {
                        for (Item.PotionEffectType effectType : effect.extraEffects()) {
                            if (effectType.type() != null) {
                                NamespacedKey effectKey = NamespacedKey.fromString(effectType.type());
                                if (effectKey == null) {
                                    logger.warn(FormatUtil.format("Unknown key for extra potion effect: " + effectType.type()));
                                    continue;
                                }

                                PotionEffectType potionEffectType = potionEffectRegistry.get(effectKey);
                                if (potionEffectType != null) {
                                    PotionEffect potionEffect = potionEffectType.createEffect((int) (effectType.duration() * 20), effectType.level());
                                    potionMeta.addCustomEffect(potionEffect, true);
                                } else {
                                    logger.warn(FormatUtil.format("Unknown potion effect type for extra effect type " + effectType.type()));
                                }
                            }
                        }
                    }
                }

                itemStack.setItemMeta(potionMeta);
            }

            case LeatherArmorMeta armorMeta -> {
                int red;
                int green;
                int blue;

                if (item.color().random()) {
                    red = (int) (Math.random() * 256);
                    green = (int) (Math.random() * 256);
                    blue = (int) (Math.random() * 256);

                } else {
                    red = item.color().red();
                    green = item.color().green();
                    blue = item.color().blue();
                }

                armorMeta.setColor(Color.fromRGB(red, green, blue));

                itemStack.setItemMeta(armorMeta);
            }

            case EnchantmentStorageMeta enchMeta -> {
                ItemStack dummyStack = new ItemStack(Material.BOOK);

                Item.Enchants displayEnchants = item.enchants();
                if (displayEnchants.enchantRandomly()) {
                    dummyStack = dummyStack.enchantWithLevels(new Random().nextInt(displayEnchants.min(), displayEnchants.max()), displayEnchants.treasure(), new Random());

                    for (Map.Entry<Enchantment, Integer> entry : dummyStack.getEnchantments().entrySet()) {
                        Enchantment enchant = entry.getKey();
                        int level = entry.getValue();

                        enchMeta.addStoredEnchant(enchant, level, true);
                    }
                }

                if(displayEnchants.enchantments() != null) {
                    for (Item.Enchantment enchantConfig : displayEnchants.enchantments().values()) {
                        if (enchantConfig.enchantment() == null) continue;

                        NamespacedKey enchantKey = NamespacedKey.fromString(enchantConfig.enchantment());
                        if (enchantKey == null) {
                            logger.warn(FormatUtil.format("Unknown enchantment key for: " + enchantConfig.enchantment()));
                            continue;
                        }

                        Enchantment enchantment = enchantmentRegistry.get(enchantKey);
                        if (enchantment == null) {
                            logger.warn(FormatUtil.format("Unknown enchantment for: " + enchantConfig.enchantment()));
                            continue;
                        }

                        enchMeta.addStoredEnchant(enchantment, enchantConfig.level(), true);
                    }
                }

                itemStack.setItemMeta(enchMeta);
            }

            default -> {
                Item.Enchants displayEnchants = item.enchants();

                if (displayEnchants.enchantRandomly()) {
                    itemStack = itemStack.enchantWithLevels(new Random().nextInt(displayEnchants.min(), displayEnchants.max()), displayEnchants.treasure(), new Random());
                }

                // Add enchantments to the reward item
                if(displayEnchants.enchantments() != null) {
                    for (Item.Enchantment enchantConfig : displayEnchants.enchantments().values()) {
                        if (enchantConfig.enchantment() == null) continue;

                        NamespacedKey enchantKey = NamespacedKey.fromString(enchantConfig.enchantment());
                        if (enchantKey == null) {
                            logger.warn(FormatUtil.format("Unknown enchantment key for: " + enchantConfig.enchantment()));
                            continue;
                        }

                        Enchantment enchantment = enchantmentRegistry.get(enchantKey);
                        if (enchantment == null) {
                            logger.warn(FormatUtil.format("Unknown enchantment for: " + enchantConfig.enchantment()));
                            continue;
                        }

                        itemStack.addEnchantment(enchantment, enchantConfig.level());
                    }
                }
            }
        }

        return itemStack;
    }

    /**
     * Calculates a random price from a min and max.
     * @param min The min price.
     * @param max The max price.
     * @return A double representing a price.
     */
    private static double calculatePrice(double min, double max) {
        double price;
        if (max <= 0.0 && min <= 0.0) {
            price = 0.0;
        } else {
            price = BigDecimal.valueOf(Math.random() * (max - min) + min).setScale(2, RoundingMode.CEILING).doubleValue();
        }

        return price;
    }

    /**
     * Gets a HashMap of the random trades to populate a MerchantGUI with.
     * @param skyMarket The plugin's instance.
     * @param merchantConfig The config to load data from.
     * @return A HashMap representing the slot and corresponding Merchant recipe.
     */
    public static List<MerchantRecipe> getTrades(SkyMarket skyMarket, Merchant merchantConfig) {
        final int totalTrades = merchantConfig.numOfTrades();
        List<MerchantRecipe> trades = new ArrayList<>();

        int addedTrades = 0;
        LinkedHashMap<Integer, Merchant.Trade> tradesMap = new LinkedHashMap<>(merchantConfig.trades());
        List<Integer> keysList = new ArrayList<>(tradesMap.keySet());

        while (addedTrades != totalTrades) {
            if (!keysList.isEmpty()) {
                int randomIndex = new Random().nextInt(keysList.size());
                int randomKey = keysList.get(randomIndex);

                Merchant.Trade randomTrade = tradesMap.get(randomKey);

                keysList.remove(randomIndex);
                tradesMap.remove(randomKey);

                ItemStack outputStack = getDisplayItem(skyMarket, randomTrade.output(), new ArrayList<>());
                ItemStack inputStack1 = getDisplayItem(skyMarket, randomTrade.input1(), new ArrayList<>());
                ItemStack inputStack2 = getDisplayItem(skyMarket, randomTrade.input2(), new ArrayList<>());

                if(outputStack != null) {
                    MerchantRecipe recipe = new MerchantRecipe(outputStack, 999999999);
                    if(randomTrade.limit() != -1) {
                        recipe = new MerchantRecipe(outputStack, randomTrade.limit());
                    }

                    if(inputStack1 != null) {
                        recipe.addIngredient(inputStack1);

                        if(inputStack2 != null) {
                            recipe.addIngredient(inputStack2);
                        }

                        recipe.setIgnoreDiscounts(true);

                        trades.add(recipe);

                        addedTrades++;
                    }
                }
            } else {
                return trades;
            }
        }

        return trades;
    }

    /**
     * Gets a HashMap of the corresponding slots and GUIButtons for the ChestConfig.
     * @param skyMarket A SkyMarket plugin instance.
     * @param marketManager A MarketManager instance.
     * @param locale The plugin's locale.
     * @param guiType The GUIType where the buttons will be placed.
     * @param chestConfig The configuration of the buttons to create.
     * @param marketId The market id of these buttons belong to.
     * @return A HashMap of the corresponding slots and GUIButtons for the ChestConfig.
     */
    @NotNull
    public static HashMap<Integer, GUIButton> getButtons(SkyMarket skyMarket, MarketManager marketManager, Locale locale, GUIType guiType, Chest chestConfig, String marketId) {
        HashMap<Integer, GUIButton> buttons = new HashMap<>();

        if(chestConfig.guiData().guiName() != null) {
            LinkedHashMap<Integer, Chest.ItemEntry> items = new LinkedHashMap<>(chestConfig.items());
            List<Integer> keysList = new ArrayList<>(items.keySet());

            for(Map.Entry<Integer, Chest.GuiEntry> guiEntry : chestConfig.guiData().entries().entrySet()) {
                Chest.GuiEntry entry = guiEntry.getValue();
                ActionType entryType = ActionType.valueOf(entry.type());

                switch (entryType) {
                    case FILLER -> {
                        Item item = entry.item();

                        ItemStack itemStack = getDisplayItem(skyMarket, item, new ArrayList<>());
                        if(itemStack == null) continue;

                        GUIButton.Builder buttonBuilder = new GUIButton.Builder();
                        buttonBuilder.setItemStack(itemStack);
                        buttonBuilder.setAction(event -> event.setCancelled(true));
                        GUIButton button = buttonBuilder.build();

                        for(int i = 0; i <= guiType.getSize() - 1; i++) {
                            buttons.put(i, button);
                        }
                    }

                    case RETURN -> {
                        Item item = entry.item();

                        ItemStack itemStack = getDisplayItem(skyMarket, item, new ArrayList<>());
                        if(itemStack == null) continue;

                        GUIButton.Builder buttonBuilder = new GUIButton.Builder();
                        buttonBuilder.setItemStack(itemStack);
                        buttonBuilder.setAction(event -> {
                            event.setCancelled(true);

                            Player player = (Player) event.getWhoClicked();

                            skyMarket.getServer().getScheduler().runTaskLater(skyMarket, () -> player.closeInventory(), 1L);
                        });
                        GUIButton button = buttonBuilder.build();

                        buttons.put(entry.slot(), button);
                    }

                    case PLACEHOLDER -> {
                        if(!keysList.isEmpty()) {
                            int randomIndex = new Random().nextInt(keysList.size());
                            int randomKey = keysList.get(randomIndex);
                            Chest.ItemEntry randomEntry = items.get(randomKey);

                            keysList.remove(randomIndex);
                            items.remove(randomKey);

                            List<TagResolver.Single> placeholders = new ArrayList<>();

                            double buyPrice;
                            double sellPrice;
                            List<ItemStack> buyItems = new ArrayList<>();

                            if (randomEntry.price().buyFixed() != null) {
                                buyPrice = randomEntry.price().buyFixed();
                            } else if(randomEntry.price().buyMin() != null && randomEntry.price().buyMax() != null) {
                                buyPrice = calculatePrice(randomEntry.price().buyMin(), randomEntry.price().buyMax());
                            } else {
                                throw new RuntimeException("Buy prices are invalid for item at: " + randomKey);
                            }

                            placeholders.add(Placeholder.parsed("buy_price", String.valueOf(buyPrice)));

                            for (Item item : randomEntry.price().buyItems()) {
                                ItemStack itemStack = getPlayerItem(skyMarket, item);
                                if (itemStack != null) {
                                    buyItems.add(itemStack);
                                }
                            }

                            if(!buyItems.isEmpty()) {
                                for (int i = 0; i <= buyItems.size() - 1; i++) {
                                    ItemStack itemStack = buyItems.get(i);
                                    String materialName = FormatUtil.formatMaterialNameLowercase(itemStack.getType().name());

                                    placeholders.add(Placeholder.parsed("buy_item" + i, materialName + " x" + itemStack.getAmount()));
                                }
                            }

                            if (randomEntry.price().sellFixed() != null) {
                                sellPrice = randomEntry.price().sellFixed();
                            } else if (randomEntry.price().sellMin() != null && randomEntry.price().sellMax() != null) {
                                sellPrice = calculatePrice(randomEntry.price().sellMin(), randomEntry.price().sellMax());
                            } else {
                                throw new RuntimeException("Sell prices are invalid for item at: " + randomKey);
                            }

                            placeholders.add(Placeholder.parsed("sell_price", String.valueOf(sellPrice)));
                            placeholders.add(Placeholder.parsed("buy_limit", String.valueOf(randomEntry.buyLimit())));
                            placeholders.add(Placeholder.parsed("sell_limit", String.valueOf(randomEntry.sellLimit())));

                            // Display Item
                            ItemStack displayItem = getDisplayItem(skyMarket, randomEntry.item(), placeholders);
                            if (displayItem != null) {
                                ActionType itemType = ActionType.valueOf(randomEntry.type());
                                if (itemType.equals(ActionType.ITEM)) {
                                    GUIButton.Builder buttonBuilder = new GUIButton.Builder();
                                    buttonBuilder.setItemStack(displayItem);

                                    // Item given to player when purchased
                                    ItemStack playerItem = getPlayerItem(skyMarket, randomEntry.item(), displayItem.getAmount());

                                    if (playerItem != null) {
                                        buttonBuilder.setAction(event -> {
                                            event.setCancelled(true);

                                            if (event.getClick().isLeftClick()) {
                                                buyItem(skyMarket, marketManager, locale, marketId, (Player) event.getWhoClicked(), playerItem, buyPrice, buyItems, entry.slot(), randomEntry.buyLimit());
                                            } else if (event.getClick().isRightClick()) {
                                                sellItem(skyMarket, marketManager, locale, marketId, (Player) event.getWhoClicked(), playerItem, sellPrice, entry.slot(), randomEntry.sellLimit());
                                            }
                                        });
                                    }

                                    GUIButton button = buttonBuilder.build();

                                    buttons.put(entry.slot(), button);
                                } else if (itemType.equals(ActionType.COMMAND)) {
                                    GUIButton.Builder buttonBuilder = new GUIButton.Builder();
                                    buttonBuilder.setItemStack(displayItem);
                                    buttonBuilder.setAction(event -> {
                                        event.setCancelled(true);

                                        if (event.getClick().isLeftClick()) {
                                            if (randomEntry.commands().buyCommands() != null) {
                                                buyCommand(skyMarket, marketManager, locale, marketId, (Player) event.getWhoClicked(), randomEntry.item().name(), buyPrice, buyItems, randomEntry.commands().buyCommands(), entry.slot(), randomEntry.buyLimit());
                                            }
                                        } else if (event.getClick().isRightClick()) {
                                            if (randomEntry.commands().sellCommands() != null) {
                                                sellCommand(skyMarket, marketManager, locale, marketId, (Player) event.getWhoClicked(), randomEntry.item().name(), sellPrice, randomEntry.commands().sellCommands(), entry.slot(), randomEntry.sellLimit());
                                            }
                                        }
                                    });
                                    GUIButton button = buttonBuilder.build();

                                    buttons.put(entry.slot(), button);
                                }
                            }
                        }
                    }
                }
            }
        }

        return buttons;
    }
}