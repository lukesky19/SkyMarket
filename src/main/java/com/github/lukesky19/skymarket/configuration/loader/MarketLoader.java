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
package com.github.lukesky19.skymarket.configuration.loader;

import com.github.lukesky19.skylib.config.ConfigurationUtility;
import com.github.lukesky19.skylib.format.FormatUtil;
import com.github.lukesky19.skylib.gui.GUIType;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.configuration.record.Item;
import com.github.lukesky19.skymarket.configuration.record.gui.Chest;
import com.github.lukesky19.skymarket.configuration.record.gui.Merchant;
import com.github.lukesky19.skymarket.enums.ActionType;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * This class manages the loading of market configuration files.
 */
public class MarketLoader {
    private final SkyMarket skyMarket;
    private final HashMap<String, Chest> chestGuiConfigurations = new HashMap<>();
    private final HashMap<String, Merchant> merchantGuiConfigurations = new HashMap<>();

    /**
     * Constructor
     * @param skyMarket A SkyMarket plugin instance.
     */
    public MarketLoader(SkyMarket skyMarket) {
        this.skyMarket = skyMarket;
    }

    /**
     * Gets the market configuration specific to a Chest GUI.
     * @param id The id of the file.
     * @return A Chest object representing the configuration.
     */
    @Nullable
    public Chest getChestConfig(String id) {
        return chestGuiConfigurations.get(id);
    }

    /**
     * Gets the market configuration specific to a Merchant GUI.
     * @param id The id of the file.
     * @return A Chest object representing the configuration.
     */
    @Nullable
    public Merchant getMerchantConfig(String id) {
        return merchantGuiConfigurations.get(id);
    }

    /**
     * Gets all loaded market configurations specific to Chest GUIs.
     * @return A HashMap of the market id to Chest configuration object.
     */
    public HashMap<String, Chest> getChestGuiConfigurations() {
        return chestGuiConfigurations;
    }

    /**
     * Gets all the loaded market configurations specific to Merchant GUIs.
     * @return A HashMap of the market id to Merchant configuration object.
     */
    public HashMap<String, Merchant> getMerchantGuiConfigurations() {
        return merchantGuiConfigurations;
    }

    /**
     * Reloads all configuration files.
     */
    public void reload() {
        chestGuiConfigurations.clear();
        merchantGuiConfigurations.clear();

        try(Stream<Path> pathStream = Files.walk(Paths.get(skyMarket.getDataFolder() + File.separator + "markets" + File.separator + "chest")).filter(Files::isRegularFile)) {
            pathStream.forEach(path -> {
                YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
                try {
                    chestGuiConfigurations.put(com.google.common.io.Files.getNameWithoutExtension(path.toFile().getName()), loader.load().get(Chest.class));
                } catch (ConfigurateException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try(Stream<Path> pathStream = Files.walk(Paths.get(skyMarket.getDataFolder() + File.separator + "markets" + File.separator + "merchant")).filter(Files::isRegularFile)) {
            pathStream.forEach(path -> {
                YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
                try {
                    merchantGuiConfigurations.put(com.google.common.io.Files.getNameWithoutExtension(path.toFile().getName()), loader.load().get(Merchant.class));
                } catch (ConfigurateException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        validateMarketConfig();
        validateMerchantConfig();
    }

    /**
     * Validates all known market configurations for Chest GUIs.
     */
    public void validateMarketConfig() {
        ComponentLogger logger = skyMarket.getComponentLogger();

        for(Map.Entry<String, Chest> entry : chestGuiConfigurations.entrySet()) {
            String marketId = entry.getKey();
            Chest chestConfig = entry.getValue();

            if(chestConfig.configVersion() == null) {
                logger.error(FormatUtil.format("The config-version in " + marketId + ".yml is invalid."));
                return;
            }

            if(chestConfig.refreshTime() == null) {
                logger.error(FormatUtil.format("The refresh-time in " + marketId + ".yml is invalid."));
                return;
            }

            if(chestConfig.marketName() == null) {
                logger.error(FormatUtil.format("The market-name in " + marketId + ".yml is invalid."));
                return;
            }

            Chest.GuiData data = chestConfig.guiData();

            GUIType guiType = GUIType.getType(data.guiType());
            if(guiType == null) {
                logger.error(FormatUtil.format("The gui-type in " + marketId + ".yml is invalid."));
                return;
            }

            switch(guiType) {
                case CHEST_9, CHEST_18, CHEST_27, CHEST_36, CHEST_45, CHEST_54  -> {
                    // Valid type that is supported
                }

                default -> {
                    logger.error(FormatUtil.format("The gui-type in " + marketId + ".yml is not supported by the plugin."));
                    return;
                }
            }

            if(data.guiName() == null) {
                logger.error(FormatUtil.format("The gui-name in " + marketId + ".yml is invalid."));
                return;
            }

            for(Map.Entry<Integer, Chest.GuiEntry> guiEntries : data.entries().entrySet()) {
                Integer entryId = guiEntries.getKey();
                Chest.GuiEntry guiEntry = guiEntries.getValue();

                ActionType actionType = ActionType.getType(guiEntry.type());
                if(actionType == null) {
                    logger.error(FormatUtil.format("The type for entry " + entryId + " in " + marketId + ".yml is invalid."));
                    return;
                }

                switch(actionType) {
                    case FILLER -> {
                        boolean result = validateItem(entryId, marketId, guiEntry.item());
                        if(!result) return;
                    }

                    case RETURN -> {
                        if(guiEntry.slot() < 0 || guiEntry.slot() >= guiType.getSize()) {
                            logger.error(FormatUtil.format("The slot for entry " + entryId + " in " + marketId + ".yml is outside the bounds of this GUI type."));
                            return;
                        }

                        boolean result = validateItem(entryId, marketId, guiEntry.item());
                        if(!result) return;
                    }

                    case PLACEHOLDER -> {
                        if(guiEntry.slot() < 0 || guiEntry.slot() >= guiType.getSize()) {
                            logger.error(FormatUtil.format("The slot for entry " + entryId + " in " + marketId + ".yml is outside the bounds of this GUI type."));
                            return;
                        }
                    }

                    case ITEM, COMMAND -> {
                        logger.error(FormatUtil.format("The type for entry " + entryId + " in " + marketId + ".yml is not allowed in this context."));
                        return;
                    }
                }
            }

            for(Map.Entry<Integer, Chest.ItemEntry> entryMap : chestConfig.items().entrySet()) {
                Integer entryId = entryMap.getKey();
                Chest.ItemEntry itemEntry = entryMap.getValue();

                ActionType actionType = ActionType.getType(itemEntry.type());
                if(actionType == null) {
                    logger.error(FormatUtil.format("The type for entry " + entryId + " in " + marketId + ".yml is invalid."));
                    return;
                }

                switch(actionType) {
                    case FILLER, RETURN, PLACEHOLDER -> {
                        logger.error(FormatUtil.format("The type for entry " + entryId + " in " + marketId + ".yml is not allowed in this context."));
                        return;
                    }

                    case ITEM, COMMAND -> {
                        // Valid type in this context
                    }
                }

                boolean result = validateItem(entryId, marketId, itemEntry.item());
                if(!result) return;

                boolean hasBuyPrice = false;
                boolean hasSellPrice = false;
                boolean hasBuyItems = false;

                Chest.Price priceConfig = itemEntry.price();
                if(priceConfig.buyFixed() != null || (priceConfig.buyMin() != null && priceConfig.buyMax() != null)) {
                    if(priceConfig.buyFixed() != null) {
                        if(priceConfig.buyFixed() <= 0) {
                            logger.warn(FormatUtil.format("The price config for entry " + entryId + " in " + marketId + ".yml is invalid."));
                            logger.warn(FormatUtil.format("The fixed buy price is less than or equal to 0! This will allow the item to be purchased for free!"));
                        } else {
                            hasBuyPrice = true;
                        }
                    }

                    if(priceConfig.buyMin() != null && priceConfig.buyMax() != null) {
                        if(priceConfig.buyMin() <= 0 || priceConfig.buyMax() <= 0) {
                            logger.error(FormatUtil.format("The price config for entry " + entryId + " in " + marketId + ".yml is invalid."));
                            logger.error(FormatUtil.format("The min or max buy price is less than or equal to 0! This may allow the item to be purchased for free!"));
                            return;
                        } else {
                            hasBuyPrice = true;
                        }
                    }
                } else {
                    logger.error(FormatUtil.format("The price config for entry " + entryId + " in " + marketId + ".yml is invalid."));
                    logger.error(FormatUtil.format("No fixed buy price or min and max buy price were configured."));
                    return;
                }

                if(priceConfig.sellFixed() != null || (priceConfig.sellMin() != null && priceConfig.sellMax() != null)) {
                    if(priceConfig.sellFixed() != null) {
                        if(priceConfig.sellFixed() == 0) {
                            logger.warn(FormatUtil.format("The price config for entry " + entryId + " in " + marketId + ".yml is invalid."));
                            logger.warn(FormatUtil.format("The fixed sell price is 0! This will allow the item to be sold for free!"));
                        } else {
                            hasSellPrice = true;
                        }
                    }

                    if(priceConfig.sellMin() != null && priceConfig.sellMax() != null) {
                        if(priceConfig.sellMin() == 0 || priceConfig.sellMax() == 0) {
                            logger.warn(FormatUtil.format("The price config for entry " + entryId + " in " + marketId + ".yml is invalid."));
                            logger.warn(FormatUtil.format("The min or max sell price is less than or equal to 0! This may allow the item to be purchased for free!"));
                        } else {
                            hasSellPrice = true;
                        }
                    }
                } else {
                    logger.error(FormatUtil.format("The price config for entry " + entryId + " in " + marketId + ".yml is invalid."));
                    logger.error(FormatUtil.format("No fixed sell price or min and max sell price were configured."));
                    return;
                }

                for (Item item : priceConfig.buyItems()) {
                    boolean itemResult = validateItem(entryId, marketId, item);
                    if(itemResult) {
                        hasBuyItems = true;
                    } else {
                        logger.error(FormatUtil.format("A buy item under price config for entry " + entryId + " in " + marketId + ".yml is invalid."));
                        return;
                    }
                }

                if(actionType.equals(ActionType.COMMAND)) {
                    Chest.Commands commandsConfig = itemEntry.commands();
                    if(hasBuyPrice || hasBuyItems) {
                        if(commandsConfig.buyCommands() == null) {
                            logger.warn(FormatUtil.format("No buy commands are configured for entry " + entryId + " in " + marketId + ".yml, but there is a valid buy price and or buy items."));
                        }
                    }

                    if(hasSellPrice) {
                        if(commandsConfig.sellCommands() == null) {
                            logger.warn(FormatUtil.format("No sell commands are configured for entry " + entryId + " in " + marketId + ".yml, but there is a valid sell price and or sell items."));
                        }
                    }
                }
            }
        }
    }

    /**
     * Validates all market configurations for Merchant GUIs
     */
    public void validateMerchantConfig() {
        ComponentLogger logger = skyMarket.getComponentLogger();

        for(Map.Entry<String, Merchant> entry : merchantGuiConfigurations.entrySet()) {
            String marketId = entry.getKey();
            Merchant merchantConfig = entry.getValue();

            if(merchantConfig.configVersion() == null) {
                logger.error(FormatUtil.format("The config-version in " + marketId + ".yml is invalid."));
                return;
            }

            if(merchantConfig.refreshTime() == null) {
                logger.error(FormatUtil.format("The refresh-time in " + marketId + ".yml is invalid."));
                return;
            }

            if(merchantConfig.marketName() == null) {
                logger.error(FormatUtil.format("The market-name in " + marketId + ".yml is invalid."));
                return;
            }

            if(merchantConfig.guiName() == null) {
                logger.error(FormatUtil.format("The gui-name in " + marketId + ".yml is invalid."));
                return;
            }

            if(merchantConfig.numOfTrades() <= 0) {
                logger.warn(FormatUtil.format("The number of trades in " + marketId + ".yml is invalid. (Must be greater than 0)"));
            }

            for(Map.Entry<Integer, Merchant.Trade> tradeEntry : merchantConfig.trades().entrySet()) {
                int tradeId = tradeEntry.getKey();
                Merchant.Trade trade = tradeEntry.getValue();

                boolean input1Result = validateItem(tradeId, marketId, trade.input1());
                if(!input1Result) {
                    logger.error(FormatUtil.format("The first input (input1) is invalid for trade " + tradeId + " in " + marketId + ".yml."));
                    return;
                }

                boolean input2Result = validateItem(tradeId, marketId, trade.input2());
                if(!input2Result) {
                    logger.warn(FormatUtil.format("The second input (input2) is invalid for trade " + tradeId + " in " + marketId + ".yml."));
                    logger.info(FormatUtil.format("This can be safely ignored, as the 2nd trade is optional."));
                }

                boolean outputResult = validateItem(tradeId, marketId, trade.output());
                if(!outputResult) {
                    logger.error(FormatUtil.format("The output is invalid for trade " + tradeId + " in " + marketId + ".yml."));
                    return;
                }
            }
        }
    }

    private boolean validateItem(int entryId, String marketId, Item itemConfig) {
        ComponentLogger logger = skyMarket.getComponentLogger();

        if(itemConfig == null) {
            logger.error(FormatUtil.format("The item for entry " + entryId + " in " + marketId + ".yml is invalid."));
            return false;
        }

        if(itemConfig.material() == null) {
            logger.error(FormatUtil.format("The material for entry " + entryId + " in " + marketId + ".yml is invalid."));
            return false;
        }

        Material material = Material.getMaterial(itemConfig.material());
        if(material == null) {
            logger.error(FormatUtil.format("The material for entry " + entryId + " in " + marketId + ".yml is invalid."));
            return false;
        }

        Item.Amount itemAmount = itemConfig.amount();
        if(itemAmount.fixed() != null && (itemAmount.min() != null || itemAmount.max() != null)) {
            logger.warn(FormatUtil.format("The item amount for entry " + entryId + " in " + marketId + ".yml is has a fixed and a min and or max value."));
            logger.warn(FormatUtil.format("The plugin will ignore the min and or max value."));
        }

        for (String flag : itemConfig.itemFlags()) {
            logger.error(FormatUtil.format("The item flag " + flag + " for entry " + entryId + " in " + marketId + ".yml is invalid."));

            StringBuilder builder = new StringBuilder();

            builder.append("Valid flags: ");

            ItemFlag lastFlag = Arrays.stream(ItemFlag.values()).toList().getLast();
            for (ItemFlag itemFlag : ItemFlag.values()) {
                builder.append(itemFlag.name());

                if (lastFlag.equals(itemFlag)) {
                    builder.append(".");
                } else {
                    builder.append(" ");
                }
            }

            logger.info(builder.toString());
        }

        // Get the Enchantment Registry
        Registry<@NotNull Enchantment> enchantmentRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        // Get the Potion Registries
        Registry<@NotNull PotionType> potionTypeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.POTION);
        Registry<@NotNull PotionEffectType> potionEffectRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.MOB_EFFECT);

        Item.Enchants enchants = itemConfig.enchants();
        if(enchants.enchantments() != null) {
            for(Map.Entry<Integer, Item.Enchantment> enchantMap : enchants.enchantments().entrySet()) {
                Item.Enchantment enchantConfig = enchantMap.getValue();

                if(enchantConfig.enchantment() != null) {
                    NamespacedKey key = NamespacedKey.fromString(enchantConfig.enchantment());
                    if(key != null) {
                        Enchantment enchantment  = enchantmentRegistry.get(key);
                        if(enchantment == null) {
                            logger.warn(FormatUtil.format("The enchantment for enchantment id " + enchantMap.getKey() + " for entry " + entryId + " in " + marketId + ".yml is invalid."));

                            StringBuilder builder = new StringBuilder();

                            builder.append("Valid enchants: ");

                            Enchantment lastEnch = enchantmentRegistry.stream().toList().getLast();
                            for(Enchantment ench : enchantmentRegistry) {
                                builder.append(ench.getKey());

                                if (lastEnch.equals(ench)) {
                                    builder.append(".");
                                } else {
                                    builder.append(" ");
                                }
                            }

                            logger.info(builder.toString());
                        }
                    }
                }
            }
        }

        List<Item.PotionType> potionTypeList = itemConfig.stewEffects();
        for(Item.PotionType potionType : potionTypeList) {
            if(potionType.type() != null) {
                NamespacedKey key = NamespacedKey.fromString(potionType.type());
                if(key != null) {
                    PotionEffectType type  = potionEffectRegistry.get(key);
                    if(type == null) {
                        logger.warn(FormatUtil.format("The potion type for potion effect type id " + potionType.type() + " for entry " + entryId + " in " + marketId + ".yml is invalid."));

                        StringBuilder builder = new StringBuilder();

                        builder.append("Valid potion types: ");

                        @NotNull PotionEffectType lastPotionType = potionEffectRegistry.stream().toList().getLast();
                        for(PotionEffectType potionEffect : potionEffectRegistry) {
                            builder.append(potionEffect.getKey());

                            if (lastPotionType.equals(potionEffect)) {
                                builder.append(".");
                            } else {
                                builder.append(" ");
                            }
                        }

                        logger.info(FormatUtil.format(builder.toString()));
                    }
                }
            }
        }

        List<Item.PotionEffect> potionEffectsList = itemConfig.potionEffects();
        for(Item.PotionEffect effect : potionEffectsList) {
            if(effect != null) {
                if(effect.baseEffect() != null) {
                    if(effect.baseEffect().type() != null) {
                        NamespacedKey key = NamespacedKey.fromString(effect.baseEffect().type());
                        if(key != null) {
                            PotionType type  = potionTypeRegistry.get(key);
                            if(type == null) {
                                logger.warn(FormatUtil.format("The potion type for potion type id " + effect.baseEffect().type() + " for entry " + entryId + " in " + marketId + ".yml is invalid."));

                                StringBuilder builder = new StringBuilder();

                                builder.append("Valid potion types: ");

                                PotionType lastPotionType = potionTypeRegistry.stream().toList().getLast();
                                for(PotionType potion : potionTypeRegistry) {
                                    builder.append(potion.getKey());

                                    if (lastPotionType.equals(potion)) {
                                        builder.append(".");
                                    } else {
                                        builder.append(" ");
                                    }
                                }

                                logger.info(FormatUtil.format(builder.toString()));
                            }
                        }
                    }
                }

                if(effect.extraEffects() != null && !effect.extraEffects().isEmpty()) {
                    for(Item.PotionEffectType effectType : effect.extraEffects()) {
                        if(effectType != null) {
                            if(effectType.type() != null) {
                                NamespacedKey key = NamespacedKey.fromString(effectType.type());
                                if(key != null) {
                                    PotionEffectType potionEffectType = potionEffectRegistry.get(key);
                                    if(potionEffectType == null) {
                                        logger.warn(FormatUtil.format("The potion effect type for potion effect type id " + effectType.type() + " for entry " + entryId + " in " + marketId + ".yml is invalid."));

                                        StringBuilder builder = new StringBuilder();

                                        builder.append("Valid potion effect types: ");

                                        PotionEffectType lastEffect = potionEffectRegistry.stream().toList().getLast();
                                        for(@NotNull PotionEffectType potionEffect : potionEffectRegistry) {
                                            builder.append(potionEffect.getKey());

                                            if(lastEffect.equals(potionEffect)) {
                                                builder.append(".");
                                            } else {
                                                builder.append(" ");
                                            }
                                        }

                                        logger.info(FormatUtil.format(builder.toString()));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }
}
