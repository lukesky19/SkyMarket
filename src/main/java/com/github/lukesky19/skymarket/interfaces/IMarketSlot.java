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

import org.jspecify.annotations.NonNull;

/**
 * This interface is used to create a market slot.
 */
public interface IMarketSlot {
    /**
     * Get the transaction name. This is used in chat messages when a transaction is completed.
     * @return The transaction name.
     */
    @NonNull String getTransactionName();

    /**
     * Get the page number.
     * @return The page number.
     */
    int getPageNumber();

    /**
     * Get the slot number.
     * @return The slot number.
     */
    int getSlotNumber();

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