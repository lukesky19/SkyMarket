package com.github.lukesky19.skymarket.listener;

import com.github.lukesky19.skylib.api.gui.impl.UUIDGUIListener;
import com.github.lukesky19.skylib.api.gui.impl.UUIDGUIManager;
import com.github.lukesky19.skylib.api.gui.interfaces.BaseGUI;
import com.github.lukesky19.skylib.api.gui.templates.MerchantGUI;
import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * This class listens to a bunch of Inventory events and passes them to any GUIs that are open.
 */
public class InventoryListener extends UUIDGUIListener {
    /**
     * Constructor
     * @param guiManager A {@link UUIDGUIManager} instance.
     */
    public InventoryListener(@NotNull UUIDGUIManager guiManager) {
        super(guiManager);
    }

    /**
     * Sends trade select events to the respective open GUIs.
     * @param tradeSelectEvent A {@link TradeSelectEvent}
     */
    @EventHandler
    public void onTradeSelect(TradeSelectEvent tradeSelectEvent) {
        UUID uuid = tradeSelectEvent.getWhoClicked().getUniqueId();

        @Nullable BaseGUI<UUID> baseGUI = guiManager.getOpenGUI(uuid);
        if(baseGUI == null) return;

        if(baseGUI instanceof MerchantGUI<UUID> tradeGUI) {
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

        @Nullable BaseGUI<UUID> baseGUI = guiManager.getOpenGUI(uuid);
        if(baseGUI == null) return;

        if(baseGUI instanceof MerchantGUI<UUID> tradeGUI) {
            tradeGUI.handlePlayerTrade(playerTradeEvent);
        }
    }
}
