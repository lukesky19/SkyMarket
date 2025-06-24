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
package com.github.lukesky19.skymarket.configuration;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.data.config.item.AmountConfig;
import com.github.lukesky19.skymarket.data.config.gui.ChestConfig;
import com.github.lukesky19.skymarket.data.config.gui.MerchantConfig;
import com.github.lukesky19.skymarket.data.config.item.RandomEnchantConfig;
import com.github.lukesky19.skymarket.util.ButtonType;
import com.github.lukesky19.skymarket.util.TransactionType;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.inventory.ItemStack;
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
 * This class manages market configuration files.
 */
public class MarketConfigManager {
    private final @NotNull SkyMarket skyMarket;
    private final @NotNull HashMap<String, ChestConfig> chestConfigs = new HashMap<>();
    private final @NotNull HashMap<String, MerchantConfig> merchantConfigs = new HashMap<>();

    /**
     * Constructor
     * @param skyMarket A {@link SkyMarket} instance.
     */
    public MarketConfigManager(@NotNull SkyMarket skyMarket) {
        this.skyMarket = skyMarket;
    }

    /**
     * Get a {@link ChestConfig} for a market GUI.
     * @param marketId The market id to get the config for.
     * @return A {@link ChestConfig} record. May be null.
     */
    public @Nullable ChestConfig getChestConfig(@NotNull String marketId) {
        return chestConfigs.get(marketId);
    }

    /**
     * Get a {@link MerchantConfig} for a market GUI.
     * @param marketId The market id to get the config for.
     * @return A {@link MerchantConfig} record. May be null.
     */
    public @Nullable MerchantConfig getMerchantConfig(@NotNull String marketId) {
        return merchantConfigs.get(marketId);
    }

    /**
     * Get a {@link Map} mapping market ids to {@link ChestConfig} records.
     * @return A {@link Map} mapping market ids to {@link ChestConfig} records.
     */
    public @NotNull Map<String, ChestConfig> getChestConfigs() {
        return chestConfigs;
    }

    /**
     * Get a {@link Map} mapping market ids to {@link MerchantConfig} records.
     * @return A {@link Map} mapping market ids to {@link MerchantConfig} records.
     */
    public @NotNull Map<String, MerchantConfig> getMerchantConfigs() {
        return merchantConfigs;
    }

    /**
     * Reloads all configuration files.
     */
    public void reload() {
        ComponentLogger logger = skyMarket.getComponentLogger();

        chestConfigs.clear();
        merchantConfigs.clear();

        try(Stream<Path> pathStream = Files.walk(Paths.get(skyMarket.getDataFolder() + File.separator + "markets" + File.separator + "chest")).filter(Files::isRegularFile)) {
            pathStream.forEach(path -> {
                YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
                try {
                    String marketId = getFileNameWithoutExtension(path);
                    ChestConfig marketConfig = loader.load().get(ChestConfig.class);
                    boolean result = isChestConfigValid(logger, marketId, marketConfig);

                    if(result) chestConfigs.put(marketId, marketConfig);
                } catch (ConfigurateException e) {
                    logger.error(AdventureUtil.serialize("Failed to load configuration for " + path.toFile() + ". " + e.getMessage()));
                }
            });
        } catch (IOException e) {
            logger.error(AdventureUtil.serialize("Failed to walk through chest configuration files. " + e.getMessage()));
        }

        try(Stream<Path> pathStream = Files.walk(Paths.get(skyMarket.getDataFolder() + File.separator + "markets" + File.separator + "merchant")).filter(Files::isRegularFile)) {
            pathStream.forEach(path -> {
                YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
                try {
                    String marketId = getFileNameWithoutExtension(path);
                    MerchantConfig tradeConfig = loader.load().get(MerchantConfig.class);
                    boolean result = isMerchantConfigValid(logger, marketId, tradeConfig);

                    if(result) merchantConfigs.put(marketId, tradeConfig);
                } catch (ConfigurateException e) {
                    logger.error(AdventureUtil.serialize("Failed to load configuration for " + path.toFile() + ". " + e.getMessage()));
                }
            });
        } catch (IOException e) {
            logger.error(AdventureUtil.serialize("Failed to walk through merchant configuration files. " + e.getMessage()));
        }
    }

    /**
     * Validates the provided {@link ChestConfig}
     * @param logger A {@link ComponentLogger}.
     * @param marketId The id of the market.
     * @param marketConfig The {@link ChestConfig} to validate.
     * @return true if valid or false
     */
    public boolean isChestConfigValid(@NotNull ComponentLogger logger, @NotNull String marketId, @Nullable ChestConfig marketConfig) {
        if(marketConfig == null) return false;

        if(marketConfig.configVersion() == null) {
            logger.error(AdventureUtil.serialize("The config-version in " + marketId + ".yml is invalid."));
            return false;
        }

        if(marketConfig.refreshTime() == null) {
            logger.error(AdventureUtil.serialize("The refresh-time in " + marketId + ".yml is invalid."));
            return false;
        }

        if(marketConfig.marketName() == null) {
            logger.error(AdventureUtil.serialize("The market-name in " + marketId + ".yml is invalid."));
            return false;
        }

        ChestConfig.GuiData data = marketConfig.guiData();

        GUIType guiType = data.guiType();
        if(guiType == null) {
            logger.error(AdventureUtil.serialize("The gui type in " + marketId + ".yml is invalid."));
            return false;
        }

        switch(guiType) {
            case CHEST_9, CHEST_18, CHEST_27, CHEST_36, CHEST_45, CHEST_54  -> {
                // Valid type that is supported
            }

            default -> {
                logger.error(AdventureUtil.serialize("The gui type in " + marketId + ".yml is not supported by the plugin."));
                return false;
            }
        }

        if(data.guiName() == null) {
            logger.error(AdventureUtil.serialize("The gui name in " + marketId + ".yml is invalid."));
            return false;
        }

        for(int i = 0; i < data.buttons().size(); i++) {
            ChestConfig.Button buttonConfig = data.buttons().get(i);
            if(buttonConfig == null) continue;

            ButtonType buttonType = buttonConfig.buttonType();
            if(buttonType == null) {
                logger.error(AdventureUtil.serialize("The button type for entry " + i + " in " + marketId + ".yml is invalid."));
                return false;
            }

            boolean isSlotInvalid = buttonConfig.slot() < 0 || buttonConfig.slot() >= guiType.getSize();
            switch(buttonType) {
                case FILLER -> {
                    boolean result = isItemStackConfigValid(logger, i, marketId, buttonConfig.displayItem());
                    if(!result) return false;
                }

                case RETURN -> {
                    if(isSlotInvalid) {
                        logger.error(AdventureUtil.serialize("The slot for entry " + i + " in " + marketId + ".yml is outside the bounds of this GUI type."));
                        return false;
                    }

                    boolean result = isItemStackConfigValid(logger, i, marketId, buttonConfig.displayItem());
                    if(!result) return false;
                }

                case PLACEHOLDER -> {
                    if(isSlotInvalid) {
                        logger.error(AdventureUtil.serialize("The slot for entry " + i + " in " + marketId + ".yml is outside the bounds of this GUI type."));
                        return false;
                    }
                }
            }
        }

        for(int i = 0; i < marketConfig.items().size(); i++) {
            ChestConfig.ItemConfig itemConfig = marketConfig.items().get(i);
            if(itemConfig == null) continue;

            TransactionType transactionType = itemConfig.transactionType();
            if(transactionType == null) {
                logger.error(AdventureUtil.serialize("The transaction type for entry " + i + " in " + marketId + ".yml is invalid."));
                return false;
            }

            if(itemConfig.transactionName() == null) {
                logger.warn(AdventureUtil.serialize("The transaction name for entry " + i + " in " + marketId + ".yml is invalid."));
                return false;
            }

            ChestConfig.PriceConfig priceConfig = itemConfig.prices();
            if(priceConfig.buyFixed() == null && (priceConfig.buyMin() == null && priceConfig.buyMax() == null)) {
                logger.error(AdventureUtil.serialize("The price config for entry " + i + " in " + marketId + ".yml is invalid."));
                logger.error(AdventureUtil.serialize("No fixed buy price or min and max buy price were configured."));
                return false;
            }

            if(priceConfig.sellFixed() == null && (priceConfig.sellMin() == null && priceConfig.sellMax() == null)) {
                logger.error(AdventureUtil.serialize("The price config for entry " + i + " in " + marketId + ".yml is invalid."));
                logger.error(AdventureUtil.serialize("No fixed sell price or min and max sell price were configured."));
                return false;
            }

            for(ItemStackConfig itemStackConfig : priceConfig.buyItems()) {
                boolean itemResult = isItemStackConfigValid(logger, i, marketId, itemStackConfig);
                if(!itemResult) {
                    logger.error(AdventureUtil.serialize("A buy item under price config for entry " + i + " in " + marketId + ".yml is invalid."));
                    return false;
                }
            }

            if(transactionType.equals(TransactionType.ITEM)) {
                if(itemConfig.displayItem().itemType() == null) {
                    logger.error(AdventureUtil.serialize("The display item's ItemStack config for entry " + i + " in " + marketId + ".yml is invalid."));
                    return false;
                }

                if(!isItemStackConfigValid(logger, i, marketId, itemConfig.displayItem())) {
                    logger.error(AdventureUtil.serialize("The display item's ItemStack config for entry " + i + " in " + marketId + ".yml is invalid."));
                    return false;
                }

                if(itemConfig.transactionItem().itemType() == null) {
                    logger.error(AdventureUtil.serialize("The transaction item's ItemStack config for entry " + i + " in " + marketId + ".yml is invalid."));
                    return false;
                }

                if(!isItemStackConfigValid(logger, i, marketId, itemConfig.displayItem())) {
                    logger.error(AdventureUtil.serialize("The transaction item's ItemStack config for entry " + i + " in " + marketId + ".yml is invalid."));
                    return false;
                }

                if(!isAmountConfigValid(logger, i, marketId, itemConfig.amount())) {
                    logger.error(AdventureUtil.serialize("The amount config for entry " + i + " in " + marketId + ".yml is invalid."));
                    return false;
                }

                if(!isRandomEnchantConfigValid(logger, i, marketId, itemConfig.randomEnchants())) {
                    logger.error(AdventureUtil.serialize("The random enchants config for entry " + i + " in " + marketId + ".yml is invalid."));
                    return false;
                }
            } else {
                if((priceConfig.buyFixed() != null && priceConfig.buyFixed() > 0) || ((priceConfig.buyMin() != null && priceConfig.buyMin() > 0) && (priceConfig.buyMax() != null && priceConfig.buyMax() > 0))) {
                    if(itemConfig.buyCommands().isEmpty()) {
                        logger.warn(AdventureUtil.serialize("No buy commands are configured for entry " + i + " in " + marketId + ".yml, but there is a valid buy price and or buy items."));
                        return false;
                    }
                }

                if((priceConfig.sellFixed() != null && priceConfig.sellFixed() > 0) || ((priceConfig.sellMin() != null && priceConfig.sellMin() > 0) && (priceConfig.sellMax() != null && priceConfig.sellMax() > 0))) {
                    if(itemConfig.sellCommands().isEmpty()) {
                        logger.warn(AdventureUtil.serialize("No sell commands are configured for entry " + i + " in " + marketId + ".yml, but there is a valid sell price."));
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Is the {@link MerchantConfig} valid?
     * @param logger A {@link ComponentLogger}.
     * @param marketId The id of the market.
     * @param tradeConfig The {@link MerchantConfig} to validate.
     * @return true if valid or false
     */
    public boolean isMerchantConfigValid(@NotNull ComponentLogger logger, @NotNull String marketId, @Nullable MerchantConfig tradeConfig) {
        if(tradeConfig == null) return false;

        if(tradeConfig.configVersion() == null) {
            logger.error(AdventureUtil.serialize("The config-version in " + marketId + ".yml is invalid."));
            return false;
        }

        if(tradeConfig.refreshTime() == null) {
            logger.error(AdventureUtil.serialize("The refresh-time in " + marketId + ".yml is invalid."));
            return false;
        }

        if(tradeConfig.marketName() == null) {
            logger.error(AdventureUtil.serialize("The market-name in " + marketId + ".yml is invalid."));
            return false;
        }

        if(tradeConfig.guiName() == null) {
            logger.error(AdventureUtil.serialize("The gui-name in " + marketId + ".yml is invalid."));
            return false;
        }

        if(tradeConfig.numOfTrades() <= 0) {
            logger.warn(AdventureUtil.serialize("The number of trades in " + marketId + ".yml is invalid. (Must be greater than 0)"));
        }

        for(int tradeId = 0; tradeId < tradeConfig.trades().size(); tradeId++) {
            MerchantConfig.Trade trade = tradeConfig.trades().get(tradeId);
            if(trade == null) continue;

            ItemStackConfig input1ItemStackConfig = trade.input1().item();
            ItemStackConfig input2ItemStackConfig = trade.input2().item();
            ItemStackConfig outputItemStackConfig = trade.output().item();

            if(outputItemStackConfig.itemType() == null && input1ItemStackConfig.itemType() == null && input2ItemStackConfig.itemType() == null) {
                logger.warn(AdventureUtil.serialize("The trade at trade id " + tradeId + " has no inputs or outputs"));
                continue;
            }

            if(outputItemStackConfig.itemType() == null || input1ItemStackConfig.itemType() == null) {
                logger.warn(AdventureUtil.serialize("The trade at trade id " + tradeId + " has no output or first input configured."));
                continue;
            }

            boolean input1Result = isItemStackConfigValid(logger, tradeId, marketId, input1ItemStackConfig)
                    && isAmountConfigValid(logger, tradeId, marketId, trade.input1().amount())
                    && isRandomEnchantConfigValid(logger, tradeId, marketId, trade.input1().randomEnchants());
            if(!input1Result) {
                logger.error(AdventureUtil.serialize("The first input (input1) config is invalid for trade " + tradeId + " in " + marketId + ".yml."));
                return false;
            }

            if(input2ItemStackConfig.itemType() != null) {
                boolean input2Result = isItemStackConfigValid(logger, tradeId, marketId, input2ItemStackConfig)
                        && isAmountConfigValid(logger, tradeId, marketId, trade.input2().amount())
                        && isRandomEnchantConfigValid(logger, tradeId, marketId, trade.input2().randomEnchants());
                if(!input2Result) {
                    logger.warn(AdventureUtil.serialize("The second input (input2) config is invalid for trade " + tradeId + " in " + marketId + ".yml."));
                    return false;
                }
            }

            boolean outputResult = isItemStackConfigValid(logger, tradeId, marketId, outputItemStackConfig)
                    && isAmountConfigValid(logger, tradeId, marketId, trade.output().amount())
                    && isRandomEnchantConfigValid(logger, tradeId, marketId, trade.output().randomEnchants());
            if(!outputResult) {
                logger.error(AdventureUtil.serialize("The output config is invalid for trade " + tradeId + " in " + marketId + ".yml."));
                return false;
            }
        }

        return true;
    }

    /**
     * Is the {@link ItemStackConfig} valid?
     * @param logger A {@link ComponentLogger}.
     * @param entryId The index number of the item being validated.
     * @param marketId The id of the market.
     * @param itemStackConfig The {@link ItemStackConfig} to validate.
     * @return true if valid or false
     */
    private boolean isItemStackConfigValid(@NotNull ComponentLogger logger, int entryId, @NotNull String marketId, @NotNull ItemStackConfig itemStackConfig) {
        Optional<ItemStack> optionalItemStack = new ItemStackBuilder(logger).fromItemStackConfig(itemStackConfig, null, null, List.of()).buildItemStack();
        if(optionalItemStack.isEmpty()) {
            logger.error(AdventureUtil.serialize("Unable to create the ItemStack for entry " + entryId + " in market " + marketId + ".yml due to a configuration error with the ItemStackConfig."));
            return false;
        }

        return true;
    }

    /**
     * Is the {@link AmountConfig} valid?
     * @param logger A {@link ComponentLogger}.
     * @param entryId The index number of the item being validated.
     * @param marketId The id of the market.
     * @param amountConfig The {@link AmountConfig} to validate.
     * @return true if valid or false
     */
    private boolean isAmountConfigValid(@NotNull ComponentLogger logger, int entryId, @NotNull String marketId, @NotNull AmountConfig amountConfig) {
        if((amountConfig.fixed() == null || amountConfig.fixed() <= 0)
                && (amountConfig.min() == null || amountConfig.min() <= 0)
                && (amountConfig.max() == null || amountConfig.max() <= 0)) {
            logger.error(AdventureUtil.serialize("The amount config is invalid for entry " + entryId + " in market " + marketId + ".yml due to a configuration error."));
            return false;
        }

        return true;
    }

    /**
     * Is the {@link RandomEnchantConfig} valid?
     * @param logger A {@link ComponentLogger}.
     * @param entryId The index number of the item being validated.
     * @param marketId The id of the market.
     * @param randomEnchantConfig The {@link RandomEnchantConfig} to validate.
     * @return true if valid or false
     */
    private boolean isRandomEnchantConfigValid(@NotNull ComponentLogger logger, int entryId, @NotNull String marketId, @NotNull RandomEnchantConfig randomEnchantConfig) {
        if(randomEnchantConfig.enchantRandomly() != null) {
            if(randomEnchantConfig.enchantRandomly()) {
                if((randomEnchantConfig.min() == null || randomEnchantConfig.min() <= 0) || (randomEnchantConfig.max() == null || randomEnchantConfig.max() <= 0) || randomEnchantConfig.treasure() == null) {
                    logger.error(AdventureUtil.serialize("The random enchant config is invalid for entry " + entryId + " in market " + marketId + ".yml due to a configuration error."));
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Get the file name from a {@link Path} without the file extension.
     * @param path The {@link Path} to a file. You should ensure the {@link Path} actually points to a file.
     * @return A {@link String} containing the file name.
     * @throws RuntimeException if the {@link Path} is not a file.
     */
    private @NotNull String getFileNameWithoutExtension(@NotNull Path path) {
        if(!path.toFile().isFile()) throw new RuntimeException("Path does not point to a file.");

        String fileName = path.getFileName().toString();

        int lastDotIndex = fileName.lastIndexOf('.');

        if(lastDotIndex == -1) return fileName;

        return fileName.substring(0, lastDotIndex);
    }
}
