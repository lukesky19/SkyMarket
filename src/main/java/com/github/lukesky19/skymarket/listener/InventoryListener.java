package com.github.lukesky19.skymarket.listener;

import com.github.lukesky19.skymarket.gui.MarketGUI;
import com.github.lukesky19.skymarket.gui.MerchantGUI;
import com.github.lukesky19.skymarket.manager.MarketManager;
import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

/**
 * This class listens to a bunch of Inventory events and passes them to any GUIs that are open.
 */
public class InventoryListener implements Listener {
    private final MarketManager marketManager;

    /**
     * Constructor
     * @param marketManager A MarketManager instance.
     */
    public InventoryListener(MarketManager marketManager) {
        this.marketManager = marketManager;
    }

    /**
     * Sends click events to the respective open GUIs.
     * @param inventoryClickEvent An InventoryClickEvent
     */
    @EventHandler
    public void onClick(InventoryClickEvent inventoryClickEvent) {
        UUID uuid = inventoryClickEvent.getWhoClicked().getUniqueId();

        MarketGUI marketGUI = marketManager.getMarketGUI(uuid);
        MerchantGUI merchantGUI = marketManager.getMerchantGUI(uuid);

        if(marketGUI != null) {
            if(inventoryClickEvent.getClickedInventory() instanceof PlayerInventory) {
                // Bottom Inventory
                marketGUI.handleBottomClick(inventoryClickEvent);
            } else {
                // Top Inventory
                marketGUI.handleTopClick(inventoryClickEvent);
            }

            marketGUI.handleGlobalClick(inventoryClickEvent);
        } else if(merchantGUI != null) {
            if (inventoryClickEvent.getClickedInventory() instanceof PlayerInventory) {
                // Bottom Inventory
                merchantGUI.handleBottomClick(inventoryClickEvent);
            } else {
                // Top Inventory
                merchantGUI.handleTopClick(inventoryClickEvent);
            }

            merchantGUI.handleGlobalClick(inventoryClickEvent);
        }
    }

    /**
     * Sends drag events to the respective open GUIs.
     * @param inventoryDragEvent An InventoryDragEvent
     */
    @EventHandler
    public void onDrag(InventoryDragEvent inventoryDragEvent) {
        UUID uuid = inventoryDragEvent.getWhoClicked().getUniqueId();

        MarketGUI marketGUI = marketManager.getMarketGUI(uuid);
        MerchantGUI merchantGUI = marketManager.getMerchantGUI(uuid);

        if(marketGUI != null) {
            if(inventoryDragEvent.getInventory() instanceof PlayerInventory) {
                // Bottom Inventory
                marketGUI.handleBottomDrag(inventoryDragEvent);
            } else {
                // Top Inventory
                marketGUI.handleTopDrag(inventoryDragEvent);
            }

            marketGUI.handleGlobalDrag(inventoryDragEvent);
        } else if(merchantGUI != null) {
            if (inventoryDragEvent.getInventory() instanceof PlayerInventory) {
                // Bottom Inventory
                merchantGUI.handleBottomDrag(inventoryDragEvent);
            } else {
                // Top Inventory
                merchantGUI.handleTopDrag(inventoryDragEvent);
            }

            merchantGUI.handleGlobalDrag(inventoryDragEvent);
        }
    }

    /**
     * Sends close events to the respective open GUIs.
     * @param inventoryCloseEvent An InventoryCloseEvent
     */
    @EventHandler
    public void onClose(InventoryCloseEvent inventoryCloseEvent) {
        UUID uuid = inventoryCloseEvent.getPlayer().getUniqueId();

        MarketGUI marketGUI = marketManager.getMarketGUI(uuid);
        MerchantGUI merchantGUI = marketManager.getMerchantGUI(uuid);

        if(marketGUI != null) {
            marketGUI.handleClose(inventoryCloseEvent);
        } else if(merchantGUI != null) {
            merchantGUI.handleClose(inventoryCloseEvent);
        }
    }

    /**
     * Sends trade select events to the respective open GUIs.
     * @param tradeSelectEvent A TradeSelectEvent
     */
    @EventHandler
    public void onTradeSelect(TradeSelectEvent tradeSelectEvent) {
        UUID uuid = tradeSelectEvent.getWhoClicked().getUniqueId();

        MerchantGUI merchantGUI = marketManager.getMerchantGUI(uuid);
        if(merchantGUI != null) {
            merchantGUI.handleTradeSelect(tradeSelectEvent);
        }
    }

    /**
     * Sends player trade events to the respective open GUIs.
     * @param playerTradeEvent A PlayerTradeEvent
     */
    @EventHandler
    public void onPlayerTrade(PlayerTradeEvent playerTradeEvent) {
        UUID uuid = playerTradeEvent.getPlayer().getUniqueId();

        MerchantGUI merchantGUI = marketManager.getMerchantGUI(uuid);
        if(merchantGUI != null) {
            merchantGUI.handlePlayerTrade(playerTradeEvent);
        }
    }
}
