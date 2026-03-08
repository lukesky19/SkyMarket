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
package com.github.lukesky19.skymarket.task;

import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.gui.GUIManager;
import com.github.lukesky19.skymarket.market.MarketDataManager;
import com.github.lukesky19.skymarket.task.tasks.RefreshTask;
import org.bukkit.scheduler.BukkitTask;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * This class manages the plugin's tasks.
 */
public class TaskManager {
    private final @NonNull SkyMarket skyMarket;
    private final @NonNull MarketDataManager marketDataManager;
    private final @NonNull GUIManager guiManager;

    private @Nullable BukkitTask refreshTask;

    /**
     * Constructor
     * @param skyMarket A {@link SkyMarket} instance.
     * @param marketDataManager A {@link MarketDataManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     */
    public TaskManager(
            @NonNull SkyMarket skyMarket,
            @NonNull MarketDataManager marketDataManager,
            @NonNull GUIManager guiManager) {
        this.skyMarket = skyMarket;
        this.marketDataManager = marketDataManager;
        this.guiManager = guiManager;
    }

    /**
     * Start the refresh task.
     */
    public void startRefreshTask() {
        stopRefreshTask();

        refreshTask = new RefreshTask(marketDataManager, guiManager).runTaskTimer(skyMarket, 20L, 20L);
    }

    /**
     * Stop the refresh task.
     */
    public void stopRefreshTask() {
        if(refreshTask == null) return;

        if(!refreshTask.isCancelled()) {
            refreshTask.cancel();
        }

        refreshTask = null;
    }
}