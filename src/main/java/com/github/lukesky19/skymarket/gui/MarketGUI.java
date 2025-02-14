package com.github.lukesky19.skymarket.gui;

import com.github.lukesky19.skylib.gui.GUIType;
import com.github.lukesky19.skylib.gui.abstracts.ChestGUI;
import com.github.lukesky19.skylib.gui.GUIButton;
import com.github.lukesky19.skymarket.configuration.record.gui.Chest;
import com.github.lukesky19.skymarket.manager.MarketManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * This class is used to create MarketGUIs.
 */
public class MarketGUI extends ChestGUI {
    private final MarketManager marketManager;
    private final HashMap<Integer, GUIButton> buttons;

    /**
     * Constructor
     * @param config The configuration for this GUI.
     * @param buttons The buttons for this GUI.
     * @param player The player this GUI belongs to.
     * @param marketManager A MarketManager instance.
     */
    public MarketGUI(
            Chest config,
            HashMap<Integer, GUIButton> buttons,
            Player player,
            MarketManager marketManager) {
        this.marketManager = marketManager;
        this.buttons = buttons;

        GUIType type = GUIType.valueOf(config.guiData().guiType());
        String guiName = config.guiData().guiName();
        if(guiName == null) throw new RuntimeException("GUI name cannot be null!");

        createInventory(player, type, guiName, null);

        update();
    }

    @Override
    public void update() {
        clearButtons();

        setButtons(buttons);

        super.update();
    }

    @Override
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        marketManager.removeActiveGui(inventoryCloseEvent.getPlayer().getUniqueId());
    }
}
