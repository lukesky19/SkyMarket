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
package com.github.lukesky19.skymarket.gui.guis;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skylib.api.gui.templates.ChestGUI;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.interfaces.IMarketSlot;
import com.github.lukesky19.skymarket.locale.LocaleManager;
import com.github.lukesky19.skymarket.market.config.chest.ChestConfig;
import com.github.lukesky19.skymarket.data.market.ChestMarketData;
import com.github.lukesky19.skymarket.data.slot.ChestMarketSlot;
import com.github.lukesky19.skymarket.gui.GUIManager;
import com.github.lukesky19.skymarket.interfaces.IMarketData;
import com.github.lukesky19.skymarket.transaction.TransactionManager;
import com.github.lukesky19.skymarket.util.MarketIdUUIDKey;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.*;

/**
 * This class is used to create chest-style GUIs for markets.
 */
public class ChestMarketGUI extends ChestGUI<MarketIdUUIDKey> {
    private final @NonNull LocaleManager localeManager;
    private final @NonNull TransactionManager transactionManager;

    private final @NotNull GUIType guiType;
    private final @NotNull String guiName;

    private int pageNum = 0;
    private final @NonNull List<Integer> dynamicSlots = new ArrayList<>();

    private final @NonNull ChestMarketData marketData;
    private final ChestConfig.@NonNull GuiData guiData;

    /**
     * Constructor
     * @param skyMarket A {@link SkyMarket} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param player The {@link Player} this GUI is being created for.
     * @param identifier The {@link MarketIdUUIDKey} for this GUI.
     * @param localeManager A {@link LocaleManager} instance.
     * @param transactionManager A {@link TransactionManager} instance.
     * @param guiType The {@link GUIType} of this GUI.
     * @param guiName The name to use for the Inventory.
     * @param marketData The {@link IMarketData}.
     */
    public ChestMarketGUI(
            @NotNull SkyMarket skyMarket,
            @NotNull GUIManager guiManager,
            @NotNull Player player,
            @NotNull MarketIdUUIDKey identifier,
            @NonNull LocaleManager localeManager,
            @NonNull TransactionManager transactionManager,
            @NotNull GUIType guiType,
            @NotNull String guiName,
            @NonNull ChestMarketData marketData) {
        super(skyMarket, guiManager, identifier, player);
        this.localeManager = localeManager;
        this.transactionManager = transactionManager;

        this.guiType = guiType;
        this.guiName = guiName;
        this.marketData = marketData;
        this.guiData = marketData.getGUIData();
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        return create(guiType, guiName, List.of());
    }

    @Override
    public boolean update() {
        // If the InventoryView was not created, log a warning and return false.
        if(inventoryView == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add buttons to the GUI as the InventoryView was not created."));
            return false;
        }

        // Get the GUI size
        int guiSize = inventoryView.getTopInventory().getSize();

        // Clear the GUI of buttons
        clearButtons();

        // Clear dynamic slots
        dynamicSlots.clear();

        createFillerButtons(guiSize);

        createDummyButtons();

        createMarketButtons();

        createExitButton();

        if(!marketData.getMarketSlotsAsMap(pageNum + 1).isEmpty()) {
            createNextPageButton();
        }

        if(pageNum > 0) {
            createPreviousPageButton();
        }

        createTimeButton();

        return super.update();
    }

    @Override
    public boolean refresh() {
        recreateDynamicButtons();

        return super.update();
    }

    /**
     * Create and add the filler buttons.
     * @param guiSize The size of the Inventory/GUI.
     */
    private void createFillerButtons(int guiSize) {
        // Get the ItemStackConfig
        ItemStackConfig itemConfig = guiData.filler().item();

        // Create the ItemStackBuilder and pass the ItemStackConfig.
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(itemConfig, player, List.of());

        // If an ItemStack was created, create the GUIButton and add it to the GUI.
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
            guiButtonBuilder.setItemStack(itemStack);

            GUIButton fillerButton = guiButtonBuilder.build();

            for(int i = 0; i <= (guiSize - 1); i++) {
                setButton(i, fillerButton);
            }
        });
    }

    /**
     * Create the buttons for the market items.
     */
    private void createMarketButtons() {
        marketData.getMarketSlotsAsCollection(pageNum).forEach(marketSlot -> {
            if(marketSlot instanceof ChestMarketSlot chestMarketSlot) {
                Optional<ItemStack> optionalItemStack = chestMarketSlot.createDisplayStack(uuid);
                if(optionalItemStack.isPresent()) {
                    if(marketSlot.getRefreshTime() > 0) {
                        dynamicSlots.add(marketSlot.getSlotNumber());
                    }

                    this.slotButtons.put(marketSlot.getSlotNumber(), createMarketButton(optionalItemStack.get(), chestMarketSlot));
                }
            }
        });
    }

    /**
     * Recreate the dynamic buttons.
     */
    private void recreateDynamicButtons() {
        createTimeButton();

        Map<Integer, IMarketSlot> marketSlotMap = marketData.getMarketSlotsAsMap(pageNum);
        if(marketSlotMap.isEmpty()) return;

        dynamicSlots.forEach(slotNum -> {
            IMarketSlot marketSlot = marketSlotMap.get(slotNum);
            if(marketSlot instanceof ChestMarketSlot chestMarketSlot) {
                Optional<ItemStack> optionalItemStack = chestMarketSlot.createDisplayStack(uuid);
                optionalItemStack.ifPresent(itemStack ->
                        this.slotButtons.put(marketSlot.getSlotNumber(), createMarketButton(itemStack, chestMarketSlot)));
            }
        });
    }

    /**
     * Create the {@link GUIButton} for the market button.
     * @param itemStack The {@link ItemStack}.
     * @param marketSlot The {@link ChestMarketSlot}.
     * @return The {@link GUIButton}.
     */
    private @NonNull GUIButton createMarketButton(@NonNull ItemStack itemStack, @NonNull ChestMarketSlot marketSlot) {
        GUIButton.Builder builder = new GUIButton.Builder();
        builder.setItemStack(itemStack);
        builder.setAction(inventoryClickEvent -> {
            if(inventoryClickEvent.getClick().isLeftClick()) {
                transactionManager.buy(
                        player,
                        uuid,
                        marketSlot);
            } else if(inventoryClickEvent.getClick().isRightClick()) {
                transactionManager.sell(
                        player,
                        uuid,
                        marketSlot);
            }
        });

        return builder.build();
    }

    /**
     * Create the button to go to the previous page.
     */
    private void createPreviousPageButton() {
        // Check if the slot is not configured and send a warning.
        if(guiData.prevPage().slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add a previous page button due to a slot not being configured."));
            return;
        }

        // Get the ItemStackConfig
        ItemStackConfig itemConfig = guiData.prevPage().item();

        // Create the ItemStackBuilder and pass the ItemStackConfig.
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(itemConfig, player, List.of());

        // If an ItemStack was created, create the GUIButton and add it to the GUI.
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
            guiButtonBuilder.setItemStack(itemStack);
            guiButtonBuilder.setAction(event -> {
                pageNum--;

                this.update();
            });

            setButton(guiData.prevPage().slot(), guiButtonBuilder.build());
        });
    }

    /**
     * Create the button to go to the next page.
     */
    private void createNextPageButton() {
        // Check if the slot is not configured and send a warning.
        if(guiData.nextPage().slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add a next page button due to a slot not being configured."));
            return;
        }

        // Get the ItemStackConfig
        ItemStackConfig itemConfig = guiData.nextPage().item();

        // Create the ItemStackBuilder and pass the ItemStackConfig.
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(itemConfig, player, List.of());

        // If an ItemStack was created, create the GUIButton and add it to the GUI.
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
            guiButtonBuilder.setItemStack(itemStack);
            guiButtonBuilder.setAction(event -> {
                pageNum++;

                this.update();
            });

            setButton(guiData.nextPage().slot(), guiButtonBuilder.build());
        });
    }

    /**
     * Create the button to exit the GUI.
     */
    private void createExitButton() {
        // Check if the slot is not configured and send a warning.
        if(guiData.exit().slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add a exit button due to a slot not being configured."));
            return;
        }

        // Get the ItemStackConfig
        ItemStackConfig itemConfig = guiData.exit().item();

        // Create the ItemStackBuilder and pass the ItemStackConfig.
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(itemConfig, player, List.of());

        // If an ItemStack was created, create the GUIButton and add it to the GUI.
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
            guiButtonBuilder.setItemStack(itemStack);
            guiButtonBuilder.setAction(event -> close());

            setButton(guiData.exit().slot(), guiButtonBuilder.build());
        });
    }

    /**
     * Create the button that displays the time until the market is refreshed.
     */
    private void createTimeButton() {
        // Check if the slot is not configured and send a warning.
        if(guiData.time().slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add a time button due to a slot not being configured."));
            return;
        }

        // Get the ItemStackConfig
        ItemStackConfig itemConfig = guiData.time().item();

        // Placeholders
        List<TagResolver.Single> placeholders =  new ArrayList<>();

        if(marketData.getRefreshTime() > 0) {
            placeholders.add(Placeholder.parsed("remaining_time", localeManager.getTimeText(marketData.getRefreshTime() - System.currentTimeMillis())));
        }

        // Create the ItemStackBuilder and pass the ItemStackConfig.
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(itemConfig, player, placeholders);

        // If an ItemStack was created, create the GUIButton and add it to the GUI.
        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder guiButtonBuilder = new GUIButton.Builder();
            guiButtonBuilder.setItemStack(itemStack);

            setButton(guiData.time().slot(), guiButtonBuilder.build());
        });
    }

    /**
     * Create the dummy buttons for the GUI.
     */
    private void createDummyButtons() {
        guiData.dummyButtons().forEach(buttonConfig -> {
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.deserialize("Unable to add a dummy button to the unlocks shop GUI due to an invalid slot."));
                return;
            }

            ItemStackConfig itemStackConfig = buttonConfig.item();
            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(itemStackConfig, player, List.of());
            Optional<@NotNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
            optionalItemStack.ifPresent(itemStack -> {
                GUIButton.Builder builder = new GUIButton.Builder();

                builder.setItemStack(itemStack);

                setButton(buttonConfig.slot(), builder.build());
            });
        });
    }

    /**
     * Handles when the inventory is closed. Ignores closures with reason UNLOADED.
     * @param inventoryCloseEvent An {@link InventoryCloseEvent}
     */
    @Override
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED)) return;

        guiManager.removeOpenGUI(identifier);
    }

    /**
     * Handles when items are dragged across the player's inventory. This method does nothing.
     * @param inventoryDragEvent An {@link InventoryDragEvent}
     */
    @Override
    public void handleBottomDrag(@NotNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * Handles when items are dragged across the entire inventory. This method does nothing.
     * @param inventoryDragEvent An {@link InventoryDragEvent}
     */
    @Override
    public void handleGlobalDrag(@NotNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * Handles when the player's inventory is clicked. This method does nothing.
     * @param inventoryClickEvent An {@link InventoryClickEvent}
     */
    @Override
    public void handleBottomClick(@NotNull InventoryClickEvent inventoryClickEvent) {}

    /**
     * Handles when a click occurs in either inventory. This method does nothing.
     * @param inventoryClickEvent An {@link InventoryClickEvent}
     */
    @Override
    public void handleGlobalClick(@NotNull InventoryClickEvent inventoryClickEvent) {}
}