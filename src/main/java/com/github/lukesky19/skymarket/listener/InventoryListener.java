/*
    SkyMarket is a shop that rotates it's inventory after a set period of time.
    Copyright (C) 2024  lukeskywlker19

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

import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.gui.MarketGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * This class supports the creation of inventory GUIs.
 */
public class InventoryListener implements Listener {
    final SkyMarket skyMarket;

    public InventoryListener(SkyMarket skyMarket) {
        this.skyMarket = skyMarket;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if(inventory == null) return;

        if(inventory.getHolder(false) instanceof MarketGUI marketGUI) {
            marketGUI.handleClick(event);
        }
    }
}
