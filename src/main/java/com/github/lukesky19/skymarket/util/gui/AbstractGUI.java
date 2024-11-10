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
package com.github.lukesky19.skymarket.util.gui;

import java.util.HashMap;
import java.util.Map;

import com.github.lukesky19.skymarket.SkyMarket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * This class supports the creation of inventory GUIs.
*/
public abstract class AbstractGUI implements InventoryHolder {
    protected final Map<Integer, GUIButton> buttonMap = new HashMap<>();
    private Inventory inventory;

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
  
    public final void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
  
    public final void setButton(int slot, GUIButton button) {
        this.buttonMap.put(slot, button);
    }

    public final void clearButtons() {
        this.buttonMap.forEach((slot, button) -> inventory.clear(slot));
        this.buttonMap.clear();
    }

    public abstract void createInventory();
  
    public void decorate() {
        this.buttonMap.forEach((slot, button) -> {
            ItemStack icon = button.itemStack();
            ItemMeta iconMeta = icon.getItemMeta();
            iconMeta.displayName(button.itemName());
            iconMeta.lore(button.lore());
            icon.setItemMeta(iconMeta);
            inventory.setItem(slot, icon);
        });
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getSlot();
        GUIButton button = this.buttonMap.get(slot);
        if (button != null) {
            button.action().accept(event);
        }
    }

    public void openInventory(SkyMarket skyMarket, Player player) {
        Bukkit.getScheduler().runTaskLater(skyMarket, () -> player.openInventory(inventory), 1L);
    }

    public void closeInventory(SkyMarket skyMarket, Player player) {
        Bukkit.getScheduler().runTaskLater(skyMarket, () -> player.closeInventory(), 1L);
    }
}
