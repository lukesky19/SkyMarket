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
package com.github.lukesky19.skymarket.interfaces;

import com.github.lukesky19.skylib.api.gui.GUIType;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * This interface can be used to create market data.
 */
public interface IMarketData {
    /**
     * Get the market id the market data is associated with.
     * @return The market id.
     */
    @NonNull String getMarketId();

    /**
     * Get the market name to use in messages.
     * @return The market name.
     */
    @NonNull String getMarketName();

    /**
     * Get the {@link GUIType}.
     * @return The {@link GUIType}.
     */
    @NonNull GUIType getGUIType();

    /**
     * Get the name to display inside the GUI.
     * @return The name to display inside the GUI.
     */
    @NonNull String getGUIName();

    /**
     * Get the market slot for the page number and slot number provided.
     * @param pageNum The page number.
     * @param slotNum The slot number.
     * @return The {@link IMarketSlot} or null.
     */
    @Nullable IMarketSlot getMarketSlot(int pageNum, int slotNum);

    /**
     * Get the market slots for the page number provided.
     * @param pageNum The page number.
     * @return The {@link Collection} of {@link IMarketSlot}s.
     */
    @NonNull Collection<IMarketSlot> getMarketSlotsAsCollection(int pageNum);

    /**
     * Get the market slots for the page number provided.
     * @param pageNum The page number.
     * @return The {@link Map} mapping slot numbers to {@link IMarketSlot}s.
     */
    @NonNull Map<Integer, IMarketSlot> getMarketSlotsAsMap(int pageNum);

    /**
     * Get all market slots.
     * @return The {@link Collection} of {@link IMarketSlot}s.
     */
    @NonNull Collection<IMarketSlot> getMarketSlots();

    /**
     * Set the {@link IMarketSlot} for the page number and slot number.
     * @param pageNum The page number.
     * @param slotNum The slot number.
     * @param marketSlot The {@link IMarketSlot}.
     */
    void setMarketSlot(int pageNum, int slotNum, @NonNull IMarketSlot marketSlot);

    /**
     * Remove the {@link IMarketSlot} for the page number and slot number.
     * @param pageNum The page number.
     * @param slotNum The slot number.
     */
    void removeMarketSlot(int pageNum, int slotNum);

    /**
     * Clear the {@link IMarketSlot}s for the page number provided.
     * @param pageNum The page number.
     */
    void clearMarketSlots(int pageNum);

    /**
     * Clear the {@link IMarketSlot}s for all pages.
     */
    void clearMarketSlots();

    /**
     * Refresh all market slots.
     * @return true if successful, false if not.
     */
    boolean refreshMarket();

    /**
     * Refresh the specific market slot.
     * @param pageNum The page number.
     * @param slotNum The slot number.
     * @return true if successful, false if not.
     */
    boolean refreshMarketSlot(int pageNum, int slotNum);

    /**
     * Open the GUI to view the market.
     * @param player The {@link Player} to open the market for.
     * @return true if successful, false if not.
     */
    boolean openMarket(@NonNull Player player);

    /**
     * Set the time in milliseconds since epoch when the next market refresh will occur.
     * @param refreshTime The time in milliseconds since epoch when the next market refresh will occur.
     */
    void setRefreshTime(long refreshTime);

    /**
     * Get the time in milliseconds since epoch when the next market refresh will occur.
     * @return The time in milliseconds since epoch when the next market refresh will occur.
     */
    long getRefreshTime();
}