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
package com.github.lukesky19.skymarket.market;

import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.platform.PlatformUtils;
import com.github.lukesky19.skylib.paper.api.gui.GUIType;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.market.config.button.ButtonConfig;
import com.github.lukesky19.skymarket.market.config.chest.ChestConfigV2;
import com.github.lukesky19.skymarket.market.config.common.AmountConfig;
import com.github.lukesky19.skymarket.market.config.chest.ChestConfig;
import com.github.lukesky19.skymarket.market.config.merchant.MerchantConfig;
import com.github.lukesky19.skymarket.market.config.common.RandomEnchantConfig;
import com.github.lukesky19.skymarket.market.config.merchant.MerchantConfigV2;
import com.github.lukesky19.skymarket.settings.Settings;
import com.github.lukesky19.skymarket.settings.SettingsManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.serialize.SerializationException;
import com.github.lukesky19.skylib.libs.configurate.yaml.NodeStyle;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;

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
    private final @NonNull ComponentLogger logger;
    private final @NonNull SettingsManager settingsManager;

    private final @NotNull HashMap<String, ChestConfig> chestConfigs = new HashMap<>();
    private final @NotNull HashMap<String, MerchantConfig> merchantConfigs = new HashMap<>();

    /**
     * Constructor
     * @param skyMarket A {@link SkyMarket} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     */
    public MarketConfigManager(@NotNull SkyMarket skyMarket, @NonNull SettingsManager settingsManager) {
        this.skyMarket = skyMarket;
        this.logger = skyMarket.getComponentLogger();
        this.settingsManager = settingsManager;
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

        // Save bundled config files
        Settings settings = settingsManager.getConfiguration();
        if(settings != null) {
            if(settings.firstRun()) {
                skyMarket.saveResource("markets/chest/skymarket.yml", false);
                skyMarket.saveResource("markets/merchant/villagers.yml", false);

                settingsManager.setFirstRunFalse();
            }
        }

        chestConfigs.clear();
        merchantConfigs.clear();

        try(Stream<Path> pathStream = Files.walk(Paths.get(skyMarket.getDataFolder() + File.separator + "markets" + File.separator + "chest"))
                .filter(Files::isRegularFile)) {
            pathStream.forEach(path -> {
                YamlConfigurationLoader loader = createLoader(path);
                try {
                    ConfigurationNode root = loader.load();

                    String marketId = getFileNameWithoutExtension(path);

                    boolean save = false;
                    ChestConfig marketConfig = null;
                    int version = getVersion(root);
                    if(version == 3) {
                         marketConfig = root.get(ChestConfig.class);
                    } else if(version == 2) {
                        ChestConfigV2 chestConfigV2 = root.get(ChestConfigV2.class);
                        if(chestConfigV2 != null) {
                            save = true;
                            marketConfig = migrateChestConfig(chestConfigV2);
                        }
                    } else {
                        logger.warn(AdventureUtility.plain("Failed to load configuration for " + path.toFile() + " due to an unsupported version."));
                    }

                    boolean result = isChestConfigValid(logger, marketId, marketConfig);

                    if(result) {
                        if(save) {
                            try {
                                ConfigurationNode node = loader.createNode();

                                node.set(ChestConfig.class, marketConfig);

                                loader.save(node);

                                chestConfigs.put(marketId, marketConfig);
                            } catch (ConfigurateException e) {
                                logger.error(AdventureUtility.plain("Failed to save configuration for " + path.toFile() + ". " + e.getMessage()));
                            }
                        } else {
                            chestConfigs.put(marketId, marketConfig);
                        }
                    }
                } catch (ConfigurateException e) {
                    logger.error(AdventureUtility.plain("Failed to load configuration for " + path.toFile() + ". " + e.getMessage()));
                }
            });
        } catch (IOException e) {
            logger.error(AdventureUtility.plain("Failed to walk through chest configuration files. " + e.getMessage()));
        }

        try(Stream<Path> pathStream = Files.walk(Paths.get(skyMarket.getDataFolder() + File.separator + "markets" + File.separator + "merchant")).filter(Files::isRegularFile)) {
            pathStream.forEach(path -> {
                YamlConfigurationLoader loader = createLoader(path);
                try {
                    ConfigurationNode root = loader.load();

                    String marketId = getFileNameWithoutExtension(path);

                    boolean save = false;
                    MerchantConfig marketConfig = null;
                    int version = getVersion(root);
                    if(version == 3) {
                        marketConfig = root.get(MerchantConfig.class);
                    } else if(version == 2) {
                        MerchantConfigV2 merchantConfigV2 = root.get(MerchantConfigV2.class);
                        if(merchantConfigV2 != null) {
                            save = true;
                            marketConfig = migrateMerchantConfig(merchantConfigV2);
                        }
                    } else {
                        logger.warn(AdventureUtility.plain("Failed to load configuration for " + path.toFile() + " due to an unsupported version."));
                    }

                    boolean result = isMerchantConfigValid(logger, marketId, marketConfig);

                    if(result) {
                        if(save) {
                            try {
                                ConfigurationNode node = loader.createNode();

                                node.set(MerchantConfig.class, marketConfig);

                                loader.save(node);

                                merchantConfigs.put(marketId, marketConfig);
                            } catch (ConfigurateException e) {
                                logger.error(AdventureUtility.plain("Failed to save configuration for " + path.toFile() + ". " + e.getMessage()));
                            }
                        } else {
                            merchantConfigs.put(marketId, marketConfig);
                        }
                    }
                } catch (ConfigurateException e) {
                    logger.error(AdventureUtility.plain("Failed to load configuration for " + path.toFile() + ". " + e.getMessage()));
                }
            });
        } catch (IOException e) {
            logger.error(AdventureUtility.plain("Failed to walk through merchant configuration files. " + e.getMessage()));
        }
    }

    /**
     * Migrate the v2 Chest Market config to the lastest format.
     * @param marketConfigV2 The {@link ChestConfigV2}.
     * @return The migrated {@link ChestConfig}.
     */
    public @NonNull ChestConfig migrateChestConfig(@NotNull ChestConfigV2 marketConfigV2) {
        return new ChestConfig(
                3,
                marketConfigV2.refreshTime(),
                marketConfigV2.marketName(),
                new ChestConfig.GuiData(
                        marketConfigV2.guiData().guiType(),
                        marketConfigV2.guiData().guiName(),
                        marketConfigV2.guiData().filler(),
                        new ButtonConfig(
                                new ItemStackConfig(
                                        ItemType.ARROW,
                                        1,
                                        null,
                                        "Next Page",
                                        List.of("<gray>Click to go to the next page</gray>"),
                                        null,
                                        null,
                                        List.of(),
                                        new ItemStackConfig.PotionConfig(null, List.of()),
                                        new ItemStackConfig.ColorConfig(false, null, null, null),
                                        null,
                                        List.of(),
                                        new ItemStackConfig.DecoratedPotConfig(null, null, null, null),
                                        new ItemStackConfig.ArmorTrimConfig(null, null),
                                        List.of(),
                                        new ItemStackConfig.OptionsConfig(null, null, null, null, null)),
                                0),
                        new ButtonConfig(
                                new ItemStackConfig(
                                        ItemType.ARROW,
                                        1,
                                        null,
                                        "Previous Page",
                                        List.of("<gray>Click to go to the previous page</gray>"),
                                        null,
                                        null,
                                        List.of(),
                                        new ItemStackConfig.PotionConfig(null, List.of()),
                                        new ItemStackConfig.ColorConfig(false, null, null, null),
                                        null,
                                        List.of(),
                                        new ItemStackConfig.DecoratedPotConfig(null, null, null, null),
                                        new ItemStackConfig.ArmorTrimConfig(null, null),
                                        List.of(),
                                        new ItemStackConfig.OptionsConfig(null, null, null, null, null)),
                                8),
                        marketConfigV2.guiData().exit(),
                        new ButtonConfig(
                                new ItemStackConfig(
                                        ItemType.CLOCK,
                                        1,
                                        null,
                                        "Time Until Refresh",
                                        List.of("<gray><remaining_time></gray>"),
                                        null,
                                        null,
                                        List.of(),
                                        new ItemStackConfig.PotionConfig(null, List.of()),
                                        new ItemStackConfig.ColorConfig(false, null, null, null),
                                        null,
                                        List.of(),
                                        new ItemStackConfig.DecoratedPotConfig(null, null, null, null),
                                        new ItemStackConfig.ArmorTrimConfig(null, null),
                                        List.of(),
                                        new ItemStackConfig.OptionsConfig(null, null, null, null, null)),
                                4),
                        marketConfigV2.guiData().placeholderSlots().stream()
                                .map(slot -> new ChestConfig.PlaceholderConfig(0, slot)).toList(),
                        marketConfigV2.guiData().dummyButtons()),
                marketConfigV2.items().stream().map(itemConfig -> {
                    AmountConfig legacy = itemConfig.amount();
                    AmountConfig amountConfig = legacy;

                    if(legacy.fixed() == null && legacy.min() == null && legacy.max() == null) {
                        amountConfig = new AmountConfig(1, null, null);
                    }

                    return new ChestConfig.MarketEntry(
                            itemConfig.transactionName(),
                            itemConfig.displayItem(),
                            itemConfig.transactionItem(),
                            amountConfig,
                            itemConfig.randomEnchants(),
                            itemConfig.buyCommands(),
                            itemConfig.sellCommands(),
                            new ChestConfig.PriceConfig(
                                    itemConfig.prices().buyItems(),
                                    itemConfig.prices().buyFixed(),
                                    itemConfig.prices().sellFixed(),
                                    itemConfig.prices().buyMin(),
                                    itemConfig.prices().buyMax(),
                                    itemConfig.prices().sellMin(),
                                    itemConfig.prices().sellMax()),
                            Objects.requireNonNullElse(itemConfig.buyLimit(), 0),
                            Objects.requireNonNullElse(itemConfig.sellLimit(), 0),
                            0,
                            0,
                            null);
                }).toList());
    }

    /**
     * Migrate the v2 Merchant Market config to the lastest format.
     * @param merchantConfigV2 The {@link MerchantConfigV2}.
     * @return The migrated {@link MerchantConfig}.
     */
    public @NonNull MerchantConfig migrateMerchantConfig(@NonNull MerchantConfigV2 merchantConfigV2) {
        return new MerchantConfig(
                3,
                merchantConfigV2.refreshTime(),
                merchantConfigV2.marketName(),
                merchantConfigV2.guiName(),
                merchantConfigV2.numOfTrades(),
                merchantConfigV2.trades().stream().map(trade ->
                        new MerchantConfig.Trade(
                                Objects.requireNonNullElse(trade.limit(), 0),
                                0,
                                null,
                                new MerchantConfig.Item(trade.input1().item(), trade.input1().amount(), trade.input1().randomEnchants()),
                                new MerchantConfig.Item(trade.input2().item(), trade.input2().amount(), trade.input2().randomEnchants()),
                                new MerchantConfig.Item(trade.output().item(), trade.output().amount(), trade.output().randomEnchants()))
                ).toList());
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

        if(marketConfig.refreshTime() == null) {
            logger.error(AdventureUtility.plain("The refresh-time in " + marketId + ".yml is invalid."));
            return false;
        }

        if(marketConfig.marketName() == null) {
            logger.error(AdventureUtility.plain("The market-name in " + marketId + ".yml is invalid."));
            return false;
        }

        ChestConfig.GuiData guiData = marketConfig.guiData();

        GUIType guiType = guiData.guiType();
        if(guiType == null) {
            logger.error(AdventureUtility.plain("The gui type in " + marketId + ".yml is invalid."));
            return false;
        }

        switch(guiType) {
            case CHEST_9, CHEST_18, CHEST_27, CHEST_36, CHEST_45, CHEST_54  -> {
                // Valid type that is supported
            }

            default -> {
                logger.error(AdventureUtility.plain("The gui type in " + marketId + ".yml is not supported by the plugin."));
                return false;
            }
        }

        if(guiData.guiName() == null) {
            logger.error(AdventureUtility.plain("The gui name in " + marketId + ".yml is invalid."));
            return false;
        }

        boolean fillerItemResult = isItemStackConfigValid(logger, guiData.filler().item());
        if(!fillerItemResult) {
            logger.error(AdventureUtility.plain("The ItemStack for the filler buttons in market " + marketId + ".yml due to a configuration error with the ItemStackConfig."));
            return false;
        }

        boolean exitSlotResult = isSlotValid(guiData.exit().slot(), guiType.getSize());
        if(!exitSlotResult) {
            logger.error(AdventureUtility.plain("The slot for the exit button in " + marketId + ".yml is invalid or outside the bounds of this GUI type."));
            return false;
        }
        boolean exitItemResult = isItemStackConfigValid(logger, guiData.exit().item());
        if(!exitItemResult) {
            logger.error(AdventureUtility.plain("The ItemStack for the exit button in market " + marketId + ".yml due to a configuration error with the ItemStackConfig."));
            return false;
        }
        
        for(int i = 0; i < guiData.dummyButtons().size(); i++) {
            ButtonConfig buttonConfig = guiData.dummyButtons().get(i);
            if(buttonConfig == null) continue;

            boolean dummySlotResult = isSlotValid(buttonConfig.slot(), guiType.getSize());
            if(!dummySlotResult) {
                logger.error(AdventureUtility.plain("The slot for the dummy button number " + i + " in " + marketId + ".yml is invalid or outside the bounds of this GUI type."));
                return false;
            }
            boolean dummyItemResult = isItemStackConfigValid(logger, buttonConfig.item());
            if(!dummyItemResult) {
                logger.error(AdventureUtility.plain("The ItemStack for the dummy button number " + i + " in market " + marketId + ".yml due to a configuration error with the ItemStackConfig."));
                return false;
            }
        }

        for(ChestConfig.PlaceholderConfig placeholderConfig : guiData.placeholderSlots()) {
            boolean placeholderSlotResult = isSlotValid(placeholderConfig.slotNum(), guiType.getSize());
            if(!placeholderSlotResult) {
                logger.error(AdventureUtility.plain("The slot " + placeholderConfig.slotNum() + " for a placeholder button in " + marketId + ".yml is invalid or outside the bounds of this GUI type."));
                return false;
            }
        }

        for(int i = 0; i < marketConfig.entries().size(); i++) {
            ChestConfig.MarketEntry marketEntry = marketConfig.entries().get(i);
            if(marketEntry == null) continue;

            if(marketEntry.transactionName() == null) {
                logger.warn(AdventureUtility.plain("The transaction name for entry " + i + " in " + marketId + ".yml is invalid."));
                return false;
            }

            for(ItemStackConfig itemStackConfig : marketEntry.prices().buyItems()) {
                boolean itemResult = isItemStackConfigValid(logger, itemStackConfig);
                if(!itemResult) {
                    logger.error(AdventureUtility.plain("A buy item under price config for entry " + i + " in " + marketId + ".yml is invalid."));
                    return false;
                }
            }

            if(marketEntry.displayItem().itemType() == null) {
                logger.error(AdventureUtility.plain("The display item's ItemStack config for entry " + i + " in " + marketId + ".yml is invalid."));
                return false;
            }

            if(!isItemStackConfigValid(logger, marketEntry.displayItem())) {
                logger.error(AdventureUtility.plain("The display item's ItemStack config for entry " + i + " in " + marketId + ".yml is invalid."));
                return false;
            }

            if(!isAmountConfigValid(marketEntry.amount())) {
                logger.error(AdventureUtility.plain("The amount config for entry " + i + " in " + marketId + ".yml is invalid."));
                return false;
            }

            if(!isRandomEnchantConfigValid(marketEntry.randomEnchants())) {
                logger.error(AdventureUtility.plain("The random enchants config for entry " + i + " in " + marketId + ".yml is invalid."));
                return false;
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

        if(tradeConfig.refreshTime() == null) {
            logger.error(AdventureUtility.plain("The refresh-time in " + marketId + ".yml is invalid."));
            return false;
        }

        if(tradeConfig.marketName() == null) {
            logger.error(AdventureUtility.plain("The market-name in " + marketId + ".yml is invalid."));
            return false;
        }

        if(tradeConfig.guiName() == null) {
            logger.error(AdventureUtility.plain("The gui-name in " + marketId + ".yml is invalid."));
            return false;
        }

        if(tradeConfig.numOfTrades() <= 0) {
            logger.warn(AdventureUtility.plain("The number of trades in " + marketId + ".yml is invalid. (Must be greater than 0)"));
        }

        for(int tradeId = 0; tradeId < tradeConfig.trades().size(); tradeId++) {
            MerchantConfig.Trade trade = tradeConfig.trades().get(tradeId);
            if(trade == null) continue;

            ItemStackConfig input1ItemStackConfig = trade.input1().item();
            ItemStackConfig input2ItemStackConfig = trade.input2().item();
            ItemStackConfig outputItemStackConfig = trade.output().item();

            if(outputItemStackConfig.itemType() == null && input1ItemStackConfig.itemType() == null && input2ItemStackConfig.itemType() == null) {
                logger.warn(AdventureUtility.plain("The trade at trade id " + tradeId + " has no inputs or outputs"));
                continue;
            }

            if(outputItemStackConfig.itemType() == null || input1ItemStackConfig.itemType() == null) {
                logger.warn(AdventureUtility.plain("The trade at trade id " + tradeId + " has no output or first input configured."));
                continue;
            }

            boolean input1Result = isItemStackConfigValid(logger, input1ItemStackConfig)
                    && isAmountConfigValid(trade.input1().amount())
                    && isRandomEnchantConfigValid(trade.input1().randomEnchants());
            if(!input1Result) {
                logger.error(AdventureUtility.plain("The first input (input1) config is invalid for trade " + tradeId + " in " + marketId + ".yml."));
                return false;
            }

            if(input2ItemStackConfig.itemType() != null) {
                boolean input2Result = isItemStackConfigValid(logger, input2ItemStackConfig)
                        && isAmountConfigValid(trade.input2().amount())
                        && isRandomEnchantConfigValid(trade.input2().randomEnchants());
                if(!input2Result) {
                    logger.warn(AdventureUtility.plain("The second input (input2) config is invalid for trade " + tradeId + " in " + marketId + ".yml."));
                    return false;
                }
            }

            boolean outputResult = isItemStackConfigValid(logger, outputItemStackConfig)
                    && isAmountConfigValid(trade.output().amount())
                    && isRandomEnchantConfigValid(trade.output().randomEnchants());
            if(!outputResult) {
                logger.error(AdventureUtility.plain("The output config is invalid for trade " + tradeId + " in " + marketId + ".yml."));
                return false;
            }
        }

        return true;
    }

    /**
     * Is the {@link ItemStackConfig} valid?
     * @param logger A {@link ComponentLogger}.
     * @param itemStackConfig The {@link ItemStackConfig} to validate.
     * @return true if valid or false
     */
    private boolean isItemStackConfigValid(@NotNull ComponentLogger logger, @NotNull ItemStackConfig itemStackConfig) {
        Optional<ItemStack> optionalItemStack = new ItemStackBuilder(logger).fromItemStackConfig(itemStackConfig, null, List.of()).buildItemStack();
        return optionalItemStack.isPresent();
    }

    /**
     * Check if the provided slot is valid.
     * @param slot The slot to validate.
     * @param guiSize The gui size.
     * @return true or false.
     */
    private boolean isSlotValid(@Nullable Integer slot, int guiSize) {
        if(slot == null) return false;

        return slot >= 0 && slot < guiSize;
    }

    /**
     * Is the {@link AmountConfig} valid?
     * @param amountConfig The {@link AmountConfig} to validate.
     * @return true if valid or false
     */
    private boolean isAmountConfigValid(@NotNull AmountConfig amountConfig) {
        return (amountConfig.fixed() != null && amountConfig.fixed() > 0)
                || (amountConfig.min() != null && amountConfig.min() > 0)
                || (amountConfig.max() != null && amountConfig.max() > 0);
    }

    /**
     * Is the {@link RandomEnchantConfig} valid?
     * @param randomEnchantConfig The {@link RandomEnchantConfig} to validate.
     * @return true if valid or false
     */
    private boolean isRandomEnchantConfigValid(@NotNull RandomEnchantConfig randomEnchantConfig) {
        if(randomEnchantConfig.enchantRandomly() != null) {
            if(randomEnchantConfig.enchantRandomly()) {
                return (randomEnchantConfig.min() != null && randomEnchantConfig.min() > 0)
                        && (randomEnchantConfig.max() != null && randomEnchantConfig.max() > 0)
                        && randomEnchantConfig.treasure() != null;
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

    /**
     * Get the version number.
     * @param root The root {@link ConfigurationNode}.
     * @return The config version.
     */
    private int getVersion(@NotNull ConfigurationNode root) {
        ConfigurationNode versionNode = root.node("version");
        int version = versionNode.getInt();

        ConfigurationNode legacyVersionNode = root.node("config-version");
        @Nullable String legacyVersion = legacyVersionNode.virtual() ? null : legacyVersionNode.getString();
        if(legacyVersion != null) {
            try {
                if(legacyVersion.equals("2.0.0.0")) {
                    versionNode.set(2);
                    version = 2;
                } else {
                    logger.warn(AdventureUtility.plain("Failed to convert String-based version to numeric version due to an unrecognized version."));
                    version = 0;
                }
            } catch (SerializationException e) {
                logger.warn(AdventureUtility.plain("Failed to convert String-based version to numeric version"));
                version = 0;
            }
        }

        return version;
    }

    /**
     * Create the {@link YamlConfigurationLoader} for the path provided.
     * @apiNote {@link PlatformUtils#getSerializers()} are included by default.
     * @param path The {@link Path}.
     * @return The {@link YamlConfigurationLoader}.
     */
    protected @NonNull YamlConfigurationLoader createLoader(@NonNull Path path) {
        return YamlConfigurationLoader.builder()
                .path(path)
                .nodeStyle(NodeStyle.BLOCK)
                .indent(4)
                .defaultOptions(configurationOptions ->
                        configurationOptions.serializers(builder ->
                                builder.registerAll(PlatformUtils.getSerializers())))
                .build();
    }
}