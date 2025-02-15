package com.github.lukesky19.skymarket.gui;

import com.github.lukesky19.skymarket.manager.MarketManager;
import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This class is used to create MerchantGUIs.
 */
public class MerchantGUI extends com.github.lukesky19.skylib.gui.abstracts.MerchantGUI {
    private final String marketId;
    private final MarketManager marketManager;

    /**
     * Constructor
     * @param marketId The market id of this MerchantGUI.
     * @param trades The trades to show in the GUI.
     * @param guiName The name of the GUI.
     * @param player The player this GUI belongs to.
     * @param marketManager A MarketManager instance.
     */
    public MerchantGUI(
            String marketId,
            List<MerchantRecipe> trades,
            String guiName,
            Player player,
            MarketManager marketManager) {
        this.marketId = marketId;
        this.marketManager = marketManager;
        createInventory(player, guiName, null, false);

        setTrades(trades);

        this.update();
    }

    /**
     * When an inventory is closed, return any items left inside slot 0 and 1.
     * @param inventoryCloseEvent An InventoryCloseEvent
     */
    @Override
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        marketManager.removeActiveGui(inventoryCloseEvent.getPlayer().getUniqueId());

        Player player = (Player) inventoryCloseEvent.getPlayer();

        marketManager.updatePlayerTrades(marketId, player.getUniqueId(), getMerchant().getRecipes());
    }

    /**
     * When the inventory is dragged, reset the demand on recipes.
     * @param inventoryDragEvent An InventoryDragEvent
     */
    @Override
    public void handleTopDrag(@NotNull InventoryDragEvent inventoryDragEvent) {
        if(inventoryDragEvent.getInventory() instanceof MerchantInventory merchantInventory) {
            for(MerchantRecipe recipe : merchantInventory.getMerchant().getRecipes()) {
                recipe.setDemand(0);
            }
        }
    }

    /**
     * When a player makes a trade, reset the demand on recipes.
     * @param playerTradeEvent An PlayerTradeEvent
     */
    @Override
    public void handlePlayerTrade(PlayerTradeEvent playerTradeEvent) {
        for(MerchantRecipe recipe : playerTradeEvent.getVillager().getRecipes()) {
            recipe.setDemand(0);
        }
    }

    /**
     * When a player selects a trade, reset the demand on recipes.
     * @param tradeSelectEvent An TradeSelectEvent
     */
    @Override
    public void handleTradeSelect(TradeSelectEvent tradeSelectEvent) {
        MerchantInventory merchantInventory = tradeSelectEvent.getInventory();

        for (MerchantRecipe recipe : merchantInventory.getMerchant().getRecipes()) {
            recipe.setDemand(0);
        }
    }
}
