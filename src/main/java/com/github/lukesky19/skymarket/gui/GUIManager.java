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

import com.github.lukesky19.skylib.paper.api.gui.abstracts.AbstractGUIManager;
import com.github.lukesky19.skylib.paper.api.gui.interfaces.BaseGUI;
import com.github.lukesky19.skymarket.util.MarketIdUUIDKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class tracks open GUIs by market id and player id.
 */
public class GUIManager extends AbstractGUIManager<MarketIdUUIDKey> {
    private final @NotNull Map<UUID, String> playerIdMarketIdMap = new HashMap<>();

    /**
     * Constructor
     */
    public GUIManager() {}

    @Override
    public void addOpenGUI(@NotNull MarketIdUUIDKey identifier, @NotNull BaseGUI<MarketIdUUIDKey> data) {
        super.addOpenGUI(identifier, data);

        playerIdMarketIdMap.put(identifier.uuid(), identifier.marketId());
    }

    @Override
    public void removeOpenGUI(@NotNull MarketIdUUIDKey identifier) {
        super.removeOpenGUI(identifier);

        playerIdMarketIdMap.remove(identifier.uuid());
    }

    @Override
    public void closeOpenGUIs(boolean onDisable) {
        dataMap.entrySet().iterator().forEachRemaining(entry -> {
            playerIdMarketIdMap.remove(entry.getKey().uuid());

            BaseGUI<MarketIdUUIDKey> baseGUI = entry.getValue();
            if(baseGUI != null) baseGUI.unload(onDisable);
        });
    }

    /**
     * Refreshes all open GUIs.
     */
    public void refreshGUIs() {
        dataMap.forEach((identifier, gui) -> gui.refresh());
    }

    /**
     * Refreshes all open GUIs with the matching market id.
     * @param marketId The market id.
     */
    public void refreshGUIs(@NonNull String marketId) {
        dataMap.entrySet()
                .stream()
                .filter(entry -> entry.getKey().marketId().equals(marketId))
                .forEach(entry -> entry.getValue().refresh());
    }

    /**
     * Get the open gui based solely on the player's {@link UUID}.
     * @param playerId The player's {@link UUID}
     * @return A {@link BaseGUI} or null.
     */
    public @Nullable BaseGUI<MarketIdUUIDKey> getOpenGUIByPlayerId(@NotNull UUID playerId) {
        @Nullable String marketId = playerIdMarketIdMap.get(playerId);
        if(marketId == null) return null;

        return getOpenGUI(new MarketIdUUIDKey(marketId, playerId));
    }

    /**
     * Close any GUIs open for the market id provided.
     * @param marketId The market id.
     */
    public void closeGUIsByMarketId(@NotNull String marketId) {
        dataMap.entrySet()
                .stream()
                .filter(entry -> entry.getKey().marketId().equals(marketId))
                .iterator()
                .forEachRemaining(entry -> {
                    playerIdMarketIdMap.remove(entry.getKey().uuid());

                    entry.getValue().unload(false);
                });
    }
}