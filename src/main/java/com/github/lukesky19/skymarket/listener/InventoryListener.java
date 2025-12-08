package com.github.lukesky19.skymarket.listener;

import com.github.lukesky19.skylib.api.gui.interfaces.BaseGUI;
import com.github.lukesky19.skylib.api.gui.templates.MerchantGUI;
import com.github.lukesky19.skymarket.manager.GUIManager;
import com.github.lukesky19.skymarket.util.MarketIdUUIDKey;
import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * When an inventory is clicked, check if the Inventory is a GUI created by the plugin.
     * If so, call the handleClick method for the specific GUI.
     * @param inventoryClickEvent InventoryClickEvent
     */
    @EventHandler
    public void onClick(InventoryClickEvent inventoryClickEvent) {
        UUID uuid = inventoryClickEvent.getWhoClicked().getUniqueId();
        Inventory inventory = inventoryClickEvent.getClickedInventory();

        @Nullable BaseGUI<MarketIdUUIDKey> baseGUI = guiManager.getOpenGUIByPlayerId(uuid);
        if(baseGUI == null) return;

        baseGUI.handleGlobalClick(inventoryClickEvent);

        if(inventory instanceof PlayerInventory) {
            baseGUI.handleBottomClick(inventoryClickEvent);
        } else {
            baseGUI.handleTopClick(inventoryClickEvent);
        }
    }

    /**
     * When an inventory is dragged, check if the Inventory is a GUI created by the plugin.
     * If so, call the handleDrag method for the specific GUI.
     * @param inventoryDragEvent InventoryClickEvent
     */
    @EventHandler
    public void onDrag(InventoryDragEvent inventoryDragEvent) {
        UUID uuid = inventoryDragEvent.getWhoClicked().getUniqueId();
        Inventory inventory = inventoryDragEvent.getInventory();

        @Nullable BaseGUI<MarketIdUUIDKey> baseGUI = guiManager.getOpenGUIByPlayerId(uuid);
        if(baseGUI == null) return;


        baseGUI.handleGlobalDrag(inventoryDragEvent);

        if(inventory instanceof PlayerInventory) {
            baseGUI.handleBottomDrag(inventoryDragEvent);
        } else {
            baseGUI.handleTopDrag(inventoryDragEvent);
        }
    }

    /**
     * When an inventory is closed, check if the inventory is a GUI created by the plugin.
     * If so, call the handleClose method for the specific GUI.
     * @param inventoryCloseEvent InventoryCloseEvent
     */
    @EventHandler
    public void onClose(InventoryCloseEvent inventoryCloseEvent) {
        UUID uuid = inventoryCloseEvent.getPlayer().getUniqueId();

        @Nullable BaseGUI<MarketIdUUIDKey> baseGUI = guiManager.getOpenGUIByPlayerId(uuid);
        if(baseGUI == null) return;

        baseGUI.handleClose(inventoryCloseEvent);
    }

    /**
     * Sends trade select events to the respective open GUIs.
     * @param tradeSelectEvent A {@link TradeSelectEvent}
     */
    @EventHandler
    public void onTradeSelect(TradeSelectEvent tradeSelectEvent) {
        UUID uuid = tradeSelectEvent.getWhoClicked().getUniqueId();

        @Nullable BaseGUI<MarketIdUUIDKey> baseGUI = guiManager.getOpenGUIByPlayerId(uuid);
        if(baseGUI == null) return;

        if(baseGUI instanceof MerchantGUI<MarketIdUUIDKey> tradeGUI) {
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

        @Nullable BaseGUI<MarketIdUUIDKey> baseGUI = guiManager.getOpenGUIByPlayerId(uuid);
        if(baseGUI == null) return;

        if(baseGUI instanceof MerchantGUI<MarketIdUUIDKey> tradeGUI) {
            tradeGUI.handlePlayerTrade(playerTradeEvent);
        }
    }
}
