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
package com.github.lukesky19.skymarket.listener;

import com.github.lukesky19.skylib.paper.api.gui.interfaces.BaseGUI;
import com.github.lukesky19.skylib.paper.api.gui.templates.MerchantGUI;
import com.github.lukesky19.skymarket.gui.GUIManager;
import com.github.lukesky19.skymarket.util.MarketIdUUIDKey;
import io.papermc.paper.event.player.PlayerPurchaseEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
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
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
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
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
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
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
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
     * @param playerPurchaseEvent A {@link PlayerPurchaseEvent}
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerTrade(PlayerPurchaseEvent playerPurchaseEvent) {
        UUID uuid = playerPurchaseEvent.getPlayer().getUniqueId();

        @Nullable BaseGUI<MarketIdUUIDKey> baseGUI = guiManager.getOpenGUIByPlayerId(uuid);
        if(baseGUI == null) return;

        if(baseGUI instanceof MerchantGUI<MarketIdUUIDKey> tradeGUI) {
            tradeGUI.handlePlayerTrade(playerPurchaseEvent);
        }
    }
}