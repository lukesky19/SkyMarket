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

import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.api.registry.RegistryUtil;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.data.config.gui.ChestConfig;
import com.github.lukesky19.skymarket.data.MarketData;
import com.github.lukesky19.skymarket.data.PlayerData;
import com.github.lukesky19.skymarket.gui.ChestMarketGUI;
import com.github.lukesky19.skymarket.util.ButtonType;
import com.github.lukesky19.skymarket.util.PluginUtils;
import com.github.lukesky19.skymarket.util.TransactionType;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class manages the creation of {@link GUIButton}s for markets.
 */
public class ButtonManager {
    private final @NotNull SkyMarket skyMarket;
    private final @NotNull MarketDataManager marketDataManager;
    private final @NotNull TransactionManager transactionManager;
    private final @NotNull GUIManager guiManager;

    /**
     * Default Constructor. You should use {@link ButtonManager#ButtonManager(SkyMarket, MarketDataManager, TransactionManager, GUIManager)} instead.
     * @deprecated You should use {@link ButtonManager#ButtonManager(SkyMarket, MarketDataManager, TransactionManager, GUIManager)} instead.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public ButtonManager() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param skyMarket A {@link SkyMarket} instance.
     * @param marketDataManager A {@link MarketDataManager} instance.
     * @param transactionManager A {@link TransactionManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     */
    public ButtonManager(@NotNull SkyMarket skyMarket, @NotNull MarketDataManager marketDataManager, @NotNull TransactionManager transactionManager, @NotNull GUIManager guiManager) {
        this.skyMarket = skyMarket;
        this.marketDataManager = marketDataManager;
        this.transactionManager = transactionManager;
        this.guiManager = guiManager;
    }

    /**
     * Gets a {@link Map} of the corresponding slots and GUIButtons to populate a {@link ChestMarketGUI} with.
     * @param guiType The {@link GUIType} of the {@link ChestMarketGUI}.
     * @param marketConfig The {@link ChestConfig} to load data from.
     * @param marketId The market id.
     * @return A {@link Map} of the corresponding slots and {@link GUIButton}s to populate a {@link ChestMarketGUI} with.
     */
    public @NotNull Map<Integer, GUIButton> createButtons(
            @NotNull GUIType guiType,
            @NotNull ChestConfig marketConfig,
            @NotNull String marketId) {
        ComponentLogger logger = skyMarket.getComponentLogger();
        Map<Integer, GUIButton> buttons = new HashMap<>();

        List<ChestConfig.Button> buttonList = marketConfig.guiData().buttons();
        List<ChestConfig.ItemConfig> itemsList = new ArrayList<>(marketConfig.items());

        for(ChestConfig.Button buttonConfig : buttonList) {
            ButtonType buttonType = buttonConfig.buttonType();
            if(buttonType == null) continue;

            switch(buttonType) {
                case FILLER -> {
                    Optional<ItemStack> optionalItemStack = new ItemStackBuilder(logger).fromItemStackConfig(buttonConfig.displayItem(), null, null, List.of()).buildItemStack();
                    if(optionalItemStack.isEmpty()) continue;
                    ItemStack itemStack = optionalItemStack.get();

                    GUIButton guiButton = new GUIButton.Builder().setItemStack(itemStack).build();

                    for(int i = 0; i <= guiType.getSize() - 1; i++) {
                        buttons.put(i, guiButton);
                    }
                }

                case RETURN -> {
                    Optional<ItemStack> optionalItemStack = new ItemStackBuilder(logger).fromItemStackConfig(buttonConfig.displayItem(), null, null, List.of()).buildItemStack();
                    if(optionalItemStack.isEmpty()) continue;
                    ItemStack itemStack = optionalItemStack.get();

                    GUIButton guiButton = new GUIButton.Builder()
                            .setItemStack(itemStack)
                            .setAction(event -> {
                                Player player = (Player) event.getWhoClicked();

                                skyMarket.getServer().getScheduler().runTaskLater(skyMarket, () -> {
                                    player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

                                    guiManager.removeOpenGUI(player.getUniqueId());
                                }, 1L);
                            })
                            .build();

                    buttons.put(buttonConfig.slot(), guiButton);
                }

                case PLACEHOLDER -> {
                    if (itemsList.isEmpty()) continue;

                    int randomIndex = new Random().nextInt(itemsList.size());
                    ChestConfig.ItemConfig randomConfig = itemsList.get(randomIndex);
                    itemsList.remove(randomIndex);

                    String transactionName = randomConfig.transactionName();
                    if(transactionName == null) continue;

                    double buyPrice;
                    double sellPrice;
                    List<ItemStack> buyItems = new ArrayList<>();

                    if(randomConfig.prices().buyFixed() != null) {
                        buyPrice = randomConfig.prices().buyFixed();
                    } else if (randomConfig.prices().buyMin() != null && randomConfig.prices().buyMax() != null) {
                        buyPrice = PluginUtils.calculatePrice(randomConfig.prices().buyMin(), randomConfig.prices().buyMax());
                    } else {
                        continue;
                    }

                    if(randomConfig.prices().sellFixed() != null) {
                        sellPrice = randomConfig.prices().sellFixed();
                    } else if(randomConfig.prices().sellMin() != null && randomConfig.prices().sellMax() != null) {
                        sellPrice = PluginUtils.calculatePrice(randomConfig.prices().sellMin(), randomConfig.prices().sellMax());
                    } else {
                        continue;
                    }

                    List<TagResolver.Single> placeholders = new ArrayList<>();
                    placeholders.add(Placeholder.parsed("buy_price", String.valueOf(buyPrice)));
                    placeholders.add(Placeholder.parsed("sell_price", String.valueOf(sellPrice)));
                    placeholders.add(Placeholder.parsed("buy_limit", String.valueOf(randomConfig.buyLimit())));
                    placeholders.add(Placeholder.parsed("sell_limit", String.valueOf(randomConfig.sellLimit())));

                    for(int i = 0; i < randomConfig.prices().buyItems().size(); i++) {
                        ItemStackConfig itemStackConfig = randomConfig.prices().buyItems().get(i);
                        Optional<ItemStack> optionalItemStack = new ItemStackBuilder(logger).fromItemStackConfig(itemStackConfig, null, null, List.of()).buildItemStack();
                        if(optionalItemStack.isEmpty()) continue;

                        buyItems.add(optionalItemStack.get());
                    }

                    TransactionType transactionType = randomConfig.transactionType();
                    if (transactionType == null) continue;

                    if(transactionType.equals(TransactionType.ITEM)) {
                        if(randomConfig.transactionItem().itemType() == null) continue;
                        @NotNull Optional<ItemType> optionalItemType = RegistryUtil.getItemType(logger, randomConfig.transactionItem().itemType());
                        if(optionalItemType.isEmpty()) continue;
                        ItemType itemType = optionalItemType.get();

                        Integer randomAmount = PluginUtils.getRandomAmount(randomConfig.amount().fixed(), randomConfig.amount().min(), randomConfig.amount().max());
                        Map<Enchantment, Integer> randomEnchantments = PluginUtils.getRandomEnchantments(itemType, randomConfig.randomEnchants().enchantRandomly(), randomConfig.randomEnchants().min(), randomConfig.randomEnchants().max(), randomConfig.randomEnchants().treasure());

                        Optional<ItemStack> optionalDisplayStack = PluginUtils.createItemStack(logger, randomConfig.displayItem(), randomAmount, randomEnchantments, placeholders);
                        if (optionalDisplayStack.isEmpty()) continue;

                        Optional<ItemStack> optionalPlayerItem = PluginUtils.createItemStack(logger, randomConfig.transactionItem(), randomAmount, randomEnchantments, placeholders);
                        if (optionalPlayerItem.isEmpty()) continue;

                        GUIButton guiButton = new GUIButton.Builder()
                                .setItemStack(optionalDisplayStack.get())
                                .setAction(inventoryClickEvent -> {
                                    Player player = (Player) inventoryClickEvent.getWhoClicked();
                                    UUID uuid = player.getUniqueId();

                                    MarketData marketData = marketDataManager.getMarketData(marketId);
                                    if(marketData == null) return;
                                    PlayerData playerData = marketData.getPlayerData(uuid);

                                    if(inventoryClickEvent.getClick().isLeftClick()) {
                                        transactionManager.buyItem(
                                                player,
                                                playerData,
                                                optionalPlayerItem.get(),
                                                buyPrice,
                                                buyItems,
                                                buttonConfig.slot(),
                                                randomConfig.buyLimit());
                                    } else if(inventoryClickEvent.getClick().isRightClick()) {
                                        transactionManager.sellItem(
                                                player,
                                                playerData,
                                                optionalPlayerItem.get(),
                                                sellPrice,
                                                buttonConfig.slot(),
                                                randomConfig.sellLimit());
                                    }
                                })
                                .build();

                        buttons.put(buttonConfig.slot(), guiButton);
                    } else {
                        Optional<ItemStack> optionalDisplayStack = new ItemStackBuilder(logger).fromItemStackConfig(randomConfig.displayItem(), null, null, placeholders).buildItemStack();
                        if(optionalDisplayStack.isEmpty()) continue;

                        GUIButton guiButton = new GUIButton.Builder()
                                .setItemStack(optionalDisplayStack.get())
                                .setAction(inventoryClickEvent -> {
                                    Player player = (Player) inventoryClickEvent.getWhoClicked();
                                    UUID uuid = player.getUniqueId();

                                    MarketData marketData = marketDataManager.getMarketData(marketId);
                                    if(marketData == null) return;
                                    PlayerData playerData = marketData.getPlayerData(uuid);

                                    if(inventoryClickEvent.getClick().isLeftClick()) {
                                        transactionManager.buyCommand(
                                                player,
                                                playerData,
                                                transactionName,
                                                buyPrice,
                                                buyItems,
                                                randomConfig.buyCommands(),
                                                buttonConfig.slot(),
                                                randomConfig.buyLimit());
                                    } else if(inventoryClickEvent.getClick().isRightClick()) {
                                        transactionManager.sellCommand(
                                                player,
                                                playerData,
                                                transactionName,
                                                sellPrice,
                                                randomConfig.sellCommands(),
                                                buttonConfig.slot(),
                                                randomConfig.sellLimit());
                                    }
                                })
                                .build();

                        buttons.put(buttonConfig.slot(), guiButton);
                    }
                }
            }
        }

        return buttons;
    }
}
