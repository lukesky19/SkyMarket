package com.github.lukesky19.skymarket.listener;

import com.github.lukesky19.skylib.api.gui.interfaces.BaseGUI;
import com.github.lukesky19.skylib.api.gui.interfaces.TradeGUI;
import com.github.lukesky19.skymarket.manager.GUIManager;
import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * This class listens to a bunch of Inventory events and passes them to any GUIs that are open.
 */
public class InventoryListener implements Listener {
    private final @NotNull GUIManager guiManager;

    /**
     * Constructor
     * @param guiManager A {@link GUIManager} instance.
     */
    public InventoryListener(@NotNull GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    /**
     * Sends click events to the respective open GUIs.
     * @param inventoryClickEvent An {@link InventoryClickEvent}
     */
    @EventHandler
    public void onClick(InventoryClickEvent inventoryClickEvent) {
        UUID uuid = inventoryClickEvent.getWhoClicked().getUniqueId();

        @NotNull Optional<@NotNull BaseGUI> optionalBaseGUI = guiManager.getOpenGUI(uuid);
        if(optionalBaseGUI.isEmpty()) return;
        BaseGUI baseGUI = optionalBaseGUI.get();

        if(inventoryClickEvent.getClickedInventory() instanceof PlayerInventory) {
            // Bottom Inventory
            baseGUI.handleBottomClick(inventoryClickEvent);
        } else {
            // Top Inventory
            baseGUI.handleTopClick(inventoryClickEvent);
        }

        baseGUI.handleGlobalClick(inventoryClickEvent);
    }

    /**
     * Sends drag events to the respective open GUIs.
     * @param inventoryDragEvent An {@link InventoryDragEvent}
     */
    @EventHandler
    public void onDrag(InventoryDragEvent inventoryDragEvent) {
        UUID uuid = inventoryDragEvent.getWhoClicked().getUniqueId();

        @NotNull Optional<@NotNull BaseGUI> optionalBaseGUI = guiManager.getOpenGUI(uuid);
        if(optionalBaseGUI.isEmpty()) return;
        BaseGUI baseGUI = optionalBaseGUI.get();

        if(inventoryDragEvent.getInventory() instanceof PlayerInventory) {
            // Bottom Inventory
            baseGUI.handleBottomDrag(inventoryDragEvent);
        } else {
            // Top Inventory
            baseGUI.handleTopDrag(inventoryDragEvent);
        }

        baseGUI.handleGlobalDrag(inventoryDragEvent);
    }

    /**
     * Sends close events to the respective open GUIs.
     * @param inventoryCloseEvent An {@link InventoryCloseEvent}
     */
    @EventHandler
    public void onClose(InventoryCloseEvent inventoryCloseEvent) {
        UUID uuid = inventoryCloseEvent.getPlayer().getUniqueId();

        @NotNull Optional<@NotNull BaseGUI> optionalBaseGUI = guiManager.getOpenGUI(uuid);
        if(optionalBaseGUI.isEmpty()) return;
        BaseGUI baseGUI = optionalBaseGUI.get();

        baseGUI.handleClose(inventoryCloseEvent);
    }

    /**
     * Sends trade select events to the respective open GUIs.
     * @param tradeSelectEvent A {@link TradeSelectEvent}
     */
    @EventHandler
    public void onTradeSelect(TradeSelectEvent tradeSelectEvent) {
        UUID uuid = tradeSelectEvent.getWhoClicked().getUniqueId();

        @NotNull Optional<@NotNull BaseGUI> optionalBaseGUI = guiManager.getOpenGUI(uuid);
        if(optionalBaseGUI.isEmpty()) return;
        BaseGUI baseGUI = optionalBaseGUI.get();

        if(baseGUI instanceof TradeGUI tradeGUI) {
            tradeGUI.handleTradeSelect(tradeSelectEvent);
        }
    }

    /**
     * Sends player trade events to the respective open GUIs.
     * @param playerTradeEvent A {@link PlayerTradeEvent}
     */
    @EventHandler
    public void onPlayerTrade(PlayerTradeEvent playerTradeEvent) {
        UUID uuid = playerTradeEvent.getPlayer().getUniqueId();

        @NotNull Optional<@NotNull BaseGUI> optionalBaseGUI = guiManager.getOpenGUI(uuid);
        if(optionalBaseGUI.isEmpty()) return;
        BaseGUI baseGUI = optionalBaseGUI.get();

        if(baseGUI instanceof TradeGUI tradeGUI) {
            tradeGUI.handlePlayerTrade(playerTradeEvent);
        }
    }
}
