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

import com.github.lukesky19.skylib.api.gui.templates.MerchantGUI;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.data.market.MerchantMarketData;
import com.github.lukesky19.skymarket.data.slot.MerchantMarketSlot;
import com.github.lukesky19.skymarket.gui.GUIManager;
import com.github.lukesky19.skymarket.interfaces.IMarketSlot;
import com.github.lukesky19.skymarket.util.MarketIdUUIDKey;
import io.papermc.paper.event.player.PlayerPurchaseEvent;
import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * This class is used to create merchant-style GUIs for markets.
 */
public class MerchantMarketGUI extends MerchantGUI<MarketIdUUIDKey> {
    private final @NotNull String guiName;
    private final @NonNull MerchantMarketData marketData;

    /**
     * Constructor
     * @param skyMarket A {@link SkyMarket} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param player The {@link Player} to create the GUI for.
     * @param identifier The {@link MarketIdUUIDKey} for this GUI.
     * @param guiName The gui name to use.
     * @param marketData A {@link MerchantMarketData}.
     */
    public MerchantMarketGUI(
            @NotNull SkyMarket skyMarket,
            @NotNull GUIManager guiManager,
            @NotNull Player player,
            @NotNull MarketIdUUIDKey identifier,
            @NotNull String guiName,
            @NonNull MerchantMarketData marketData) {
        super(skyMarket, guiManager, identifier, player);

        this.guiName = guiName;
        this.marketData = marketData;
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        return create(guiName, List.of());
    }

    @Override
    public boolean update() {
        setTrades();

        return super.update();
    }

    /**
     * When the GUI is closed, remove it from the active GUIs and save the live trades for later use.
     * @param inventoryCloseEvent An InventoryCloseEvent
     */
    @Override
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED)) return;

        guiManager.removeOpenGUI(identifier);
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
     * When a player makes a trade, reset the demand on recipes and update limits.
     * @param playerPurchaseEvent A {@link PlayerTradeEvent}.
     */
    @Override
    public void handlePlayerTrade(PlayerPurchaseEvent playerPurchaseEvent) {
        Merchant merchant = playerPurchaseEvent.getMerchant();
        List<MerchantRecipe> recipeList = merchant.getRecipes();

        // Reset demand
        recipeList.forEach(recipe -> recipe.setDemand(0));

        // Update Limits
        MerchantRecipe merchantRecipe = playerPurchaseEvent.getTrade();
        int index = recipeList.indexOf(merchantRecipe);

        IMarketSlot marketSlot = marketData.getMarketSlot(0, index);
        if(marketSlot instanceof MerchantMarketSlot merchantMarketSlot) {
            merchantMarketSlot.addServerCount(1);
            merchantMarketSlot.addPlayerCount(uuid, 1);
        }
    }

    /**
     * When a player selects a trade, reset the demand on recipes.
     * @param tradeSelectEvent A {@link TradeSelectEvent}.
     */
    @Override
    public void handleTradeSelect(TradeSelectEvent tradeSelectEvent) {
        // Reset demand
        tradeSelectEvent.getInventory().getMerchant().getRecipes().forEach(recipe -> recipe.setDemand(0));
    }

    /**
     * Set the trades to display inside the market.
     */
    private void setTrades() {
        marketData.getMarketSlotsAsCollection(0)
                .stream()
                .filter(marketSlot -> marketSlot instanceof MerchantMarketSlot)
                .map(marketSlot -> (MerchantMarketSlot) marketSlot)
                .forEach(marketSlot -> {
                    MerchantRecipe merchantRecipe = marketSlot.createMerchantRecipe(uuid);
                    addTrade(merchantRecipe);
                });
    }
}