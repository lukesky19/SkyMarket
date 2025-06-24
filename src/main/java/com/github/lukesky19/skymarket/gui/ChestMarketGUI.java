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

import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.gui.abstracts.ChestGUI;
import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.manager.GUIManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * This class is used to create chest-style GUIs for markets.
 */
public class ChestMarketGUI extends ChestGUI {
    private final @NotNull GUIType guiType;
    private final @NotNull String guiName;

    /**
     * Constructor
     * @param skyMarket A {@link SkyMarket} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param player The {@link Player} this GUI is being created for.
     * @param guiType The {@link GUIType} of this GUI.
     * @param guiName The name to use for the Inventory.
     * @param buttons The {@link Map} of {@link Integer} to {@link GUIButton}s to use.
     */
    public ChestMarketGUI(
            @NotNull SkyMarket skyMarket,
            @NotNull GUIManager guiManager,
            @NotNull Player player,
            @NotNull GUIType guiType,
            @NotNull String guiName,
            @NotNull Map<Integer, GUIButton> buttons) {
        super(skyMarket, guiManager, player);

        this.guiType = guiType;
        this.guiName = guiName;

        this.slotButtons.putAll(buttons);
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        return create(guiType, guiName);
    }

    /**
     * Handles when the inventory is closed. Ignores closures with reason UNLOADED.
     * @param inventoryCloseEvent An {@link InventoryCloseEvent}
     */
    @Override
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED)) return;

        guiManager.removeOpenGUI(uuid);
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
