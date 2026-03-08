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
package com.github.lukesky19.skymarket.task.tasks;

import com.github.lukesky19.skymarket.gui.GUIManager;
import com.github.lukesky19.skymarket.market.MarketDataManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.jspecify.annotations.NonNull;

/**
 * This task manages refreshing GUIs, markets, and individual market slots.
 */
public class RefreshTask extends BukkitRunnable {
    private final @NonNull MarketDataManager marketDataManager;
    private final @NonNull GUIManager guiManager;

    /**
     * Constructor
     * @param marketDataManager A {@link MarketDataManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     */
    public RefreshTask(@NonNull MarketDataManager marketDataManager, @NonNull GUIManager guiManager) {
        this.marketDataManager = marketDataManager;
        this.guiManager = guiManager;
    }

    /**
     * Runs every interval.
     * Refreshes open GUIs, markets where the refresh time is met, and market slots that the refresh time is met.
     */
    @Override
    public void run() {
        long currentTime = System.currentTimeMillis();
        marketDataManager.getMarketData().forEach((marketId, marketData) -> {
            if(marketData.getRefreshTime() > 0) {
                if(currentTime >= marketData.getRefreshTime()) {
                    marketData.refreshMarket();
                }
            }

            marketData.getMarketSlots().forEach(marketSlot -> {
                if(marketSlot.getRefreshTime() > 0) {
                    if(currentTime >= marketSlot.getRefreshTime()) {
                        marketData.refreshMarketSlot(marketSlot.getPageNumber(), marketSlot.getSlotNumber());
                    }
                }
            });
        });

        guiManager.refreshGUIs();
    }
}