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
package com.github.lukesky19.skymarket.manager;

import com.github.lukesky19.skymarket.data.MarketData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * This class stores the {@link MarketData} for active markets.
 */
public class MarketDataManager {
    private final @NotNull Map<String, MarketData> markets = new HashMap<>();

    /**
     * Default Constructor.
     */
    public MarketDataManager() {}

    /**
     * Get the {@link MarketData} for the provided market id.
     * @param marketId The market id.
     * @return The {@link MarketData} for the provided market id. May be null.
     */
    public @Nullable MarketData getMarketData(@NotNull String marketId) {
        return markets.get(marketId);
    }

    /**
     * Store {@link MarketData} for the market id provided.
     * @param marketId The market id.
     * @param marketData The {@link MarketData}.
     */
    public void setMarketData(@NotNull String marketId, @NotNull MarketData marketData) {
        markets.put(marketId, marketData);
    }

    /**
     * Clears the stored market data.
     */
    public void clearMarketData() {
        markets.clear();
    }
}
