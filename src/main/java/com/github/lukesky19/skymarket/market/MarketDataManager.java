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
package com.github.lukesky19.skymarket.market;

import com.github.lukesky19.skymarket.interfaces.IMarketData;
import com.github.lukesky19.skymarket.data.market.ChestMarketData;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * This class stores the {@link IMarketData} for active markets.
 */
public class MarketDataManager {
    private final @NonNull Map<String, IMarketData> markets = new HashMap<>();

    /**
     * Default Constructor.
     */
    public MarketDataManager() {}

    /**
     * Get the {@link IMarketData} for the provided market id.
     * @param marketId The market id.
     * @return The {@link IMarketData} for the provided market id. May be null.
     */
    public @Nullable IMarketData getMarketData(@NonNull String marketId) {
        return markets.get(marketId);
    }

    /**
     * Get the {@link Map} mapping market ids to {@link IMarketData}.
     * @return The {@link Map} mapping market ids to {@link IMarketData}.
     */
    public @NonNull Map<String, IMarketData> getMarketData() {
        return markets;
    }

    /**
     * Store {@link ChestMarketData} for the market id provided.
     * @param marketId The market id.
     * @param marketData The {@link ChestMarketData}.
     */
    public void setMarketData(@NonNull String marketId, @NonNull IMarketData marketData) {
        markets.put(marketId, marketData);
    }

    /**
     * Clears the stored market data.
     */
    public void clearMarketData() {
        markets.clear();
    }
}