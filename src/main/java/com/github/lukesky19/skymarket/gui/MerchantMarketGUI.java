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
package com.github.lukesky19.skymarket.gui;

import com.github.lukesky19.skylib.api.gui.abstracts.MerchantGUI;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.manager.GUIManager;
import com.github.lukesky19.skymarket.manager.MarketManager;
import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * This class is used to create merchant-style GUIs for markets.
 */
public class MerchantMarketGUI extends MerchantGUI {
    private final @NotNull String marketId;
    private final @NotNull String guiName;
    private final @NotNull MarketManager marketManager;

    /**
     * Constructor
     * @param skyMarket A {@link SkyMarket} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param player The {@link Player} to create the GUI for.
     * @param marketId The market id.
     * @param guiName The gui name to use.
     * @param trades A {@link List} of {@link MerchantRecipe} to use.
     * @param marketManager A {@link MarketManager} instance.
     */
    public MerchantMarketGUI(
            @NotNull SkyMarket skyMarket,
            @NotNull GUIManager guiManager,
            @NotNull Player player,
            @NotNull String marketId,
            @NotNull String guiName,
            @NotNull List<MerchantRecipe> trades,
            @NotNull MarketManager marketManager) {
        super(skyMarket, guiManager, player);

        this.marketId = marketId;
        this.guiName = guiName;
        this.marketManager = marketManager;

        setTrades(trades);
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        return create(guiName);
    }

    /**
     * Updates the player's trades then calls the super method.
     */
    @Override
    public void close() {
        Optional<List<MerchantRecipe>> optionalTrades = getLiveTrades();
        optionalTrades.ifPresent(list -> marketManager.updatePlayerTrades(marketId, uuid, trades));

        super.close();
    }

    /**
     * Updates the player's trades then calls the super method.
     */
    @Override
    public void unload(boolean onDisable) {
        Optional<List<MerchantRecipe>> optionalTrades = getLiveTrades();
        optionalTrades.ifPresent(list -> marketManager.updatePlayerTrades(marketId, uuid, trades));

        super.unload(onDisable);
    }

    /**
     * When the GUI is closed, remove it from the active GUIs and save the live trades for later use.
     * @param inventoryCloseEvent An InventoryCloseEvent
     */
    @Override
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED)) return;

        guiManager.removeOpenGUI(uuid);

        Optional<List<MerchantRecipe>> optionalTrades = getLiveTrades();
        optionalTrades.ifPresent(list -> marketManager.updatePlayerTrades(marketId, uuid, trades));
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
     * Handles when the GUI's inventory is clicked. This method does nothing.
     * @param inventoryClickEvent An {@link InventoryClickEvent}
     */
    @Override
    public void handleTopClick(@NotNull InventoryClickEvent inventoryClickEvent) {}

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

    /**
     * When a player makes a trade, reset the demand on recipes.
     * @param playerTradeEvent A {@link PlayerTradeEvent}.
     */
    @Override
    public void handlePlayerTrade(PlayerTradeEvent playerTradeEvent) {
        for(MerchantRecipe recipe : playerTradeEvent.getVillager().getRecipes()) {
            recipe.setDemand(0);
        }
    }

    /**
     * When a player selects a trade, reset the demand on recipes.
     * @param tradeSelectEvent A {@link TradeSelectEvent}.
     */
    @Override
    public void handleTradeSelect(TradeSelectEvent tradeSelectEvent) {
        MerchantInventory merchantInventory = tradeSelectEvent.getInventory();

        for(MerchantRecipe recipe : merchantInventory.getMerchant().getRecipes()) {
            recipe.setDemand(0);
        }
    }
}
