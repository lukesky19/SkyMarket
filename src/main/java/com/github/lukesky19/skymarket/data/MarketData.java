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
package com.github.lukesky19.skymarket.data;

import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skymarket.gui.ChestMarketGUI;
import com.github.lukesky19.skymarket.gui.MerchantMarketGUI;
import com.github.lukesky19.skymarket.util.MarketType;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This class contains the data for active markets.
 */
public class MarketData {
    private final @NotNull String marketName;
    private final @NotNull MarketType marketType;
    private final @NotNull GUIType guiType;
    private final @NotNull String guiName;
    private @NotNull Map<Integer, GUIButton> buttons;
    private @NotNull List<MerchantRecipe> trades;
    private final @NotNull Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private @Nullable BukkitTask refreshTask;
    private long refreshTime;

    /**
     * Default Constructor. You should use {@link MarketData#MarketData(String, MarketType, GUIType, String, Map, List)} instead.
     * @deprecated You should use {@link MarketData#MarketData(String, MarketType, GUIType, String, Map, List)} instead.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public MarketData() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param marketName The name of the market.
     * @param marketType The {@link MarketType}.
     * @param guiType The {@link GUIType} for the market.
     * @param guiName The gui name for the market.
     * @param buttons The {@link Map} mapping slots as an {@link Integer} to {@link GUIButton}s.
     * @param trades The {@link List} of {@link MerchantRecipe}s.
     */
    public MarketData(
            @NotNull String marketName,
            @NotNull MarketType marketType,
            @NotNull GUIType guiType,
            @NotNull String guiName,
            @NotNull Map<Integer, GUIButton> buttons,
            @NotNull List<MerchantRecipe> trades) {
        this.marketName = marketName;
        this.marketType = marketType;
        this.guiType = guiType;
        this.guiName = guiName;
        this.buttons = buttons;
        this.trades = trades;
    }

    /**
     * Get the {@link MarketType}.
     * @return The {@link MarketType}.
     */
    public @NotNull MarketType getMarketType() {
        return marketType;
    }

    /**
     * Set the {@link Map} mapping slots as {@link Integer}s to {@link GUIButton}s for use in the {@link ChestMarketGUI}.
     * @param buttons A {@link Map} mapping {@link Integer}s to {@link GUIButton}s.
     */
    public void setButtons(@NotNull Map<Integer, GUIButton> buttons) {
        this.buttons = buttons;
    }

    /**
     * Get the {@link Map} mapping slots as {@link Integer}s to {@link GUIButton}s for use in the {@link ChestMarketGUI}.
     * @return A {@link Map} mapping {@link Integer}s to {@link GUIButton}s.
     */
    public @NotNull Map<Integer, GUIButton> getButtons() {
        return buttons;
    }

    /**
     * Set the {@link List} of {@link MerchantRecipe}s for use in the {@link MerchantMarketGUI}.
     * @param trades A {@link List} of {@link MerchantRecipe}s.
     */
    public void setTrades(@NotNull List<MerchantRecipe> trades) {
        this.trades = trades;
    }

    /**
     * Get the {@link List} of {@link MerchantRecipe}s for use in the {@link MerchantMarketGUI}.
     * @return A {@link List} of {@link MerchantRecipe}s.
     */
    public @NotNull List<MerchantRecipe> getTrades() {
        return trades;
    }

    /**
     * Set the {@link PlayerData} for the provided {@link UUID}.
     * @param uuid The {@link UUID} of the player.
     * @param playerData The {@link PlayerData} for the player.
     */
    public void setPlayerData(@NotNull UUID uuid, PlayerData playerData) {
        playerDataMap.put(uuid, playerData);
    }

    /**
     * Get the {@link PlayerData} for the provided {@link UUID}.
     * @param uuid The {@link UUID} of the player.
     * @return The {@link PlayerData} for the player.
     */
    public @NotNull PlayerData getPlayerData(@NotNull UUID uuid) {
        PlayerData playerData = playerDataMap.get(uuid);

        if(playerData == null) return new PlayerData(new HashMap<>(), new HashMap<>(), new ArrayList<>());

        return playerData;
    }

    /**
     * Set the {@link BukkitTask} that is handling the refresh of the market.
     * @param refreshTask The {@link BukkitTask} that is handling the refresh of the market.
     */
    public void setRefreshTask(@Nullable BukkitTask refreshTask) {
        this.refreshTask = refreshTask;
    }

    /**
     * Get the {@link BukkitTask} that is handling the refresh of the market.
     * @return The {@link BukkitTask} that is handling the refresh of the market.
     */
    public @Nullable BukkitTask getRefreshTask() {
        return refreshTask;
    }

    /**
     * Set the expected time when the market should refresh.
     * @param refreshTime The milliseconds since epoch when the market will refresh.
     */
    public void setRefreshTime(long refreshTime) {
        this.refreshTime = refreshTime;
    }

    /**
     * Get the expected time when the market should refresh.
     * @return The milliseconds since epoch when the market will refresh.
     */
    public long getRefreshTime() {
        return refreshTime;
    }

    /**
     * Get the name of the market.
     * @return The name of the market.
     */
    public @NotNull String getMarketName() {
        return marketName;
    }

    /**
     * Get the {@link GUIType} for the market.
     * @return A {@link GUIType}.
     */
    public @NotNull GUIType getGuiType() {
        return guiType;
    }

    /**
     * Get the name to use within the GUI.
     * @return The name to use within the GUI.
     */
    public @NotNull String getGuiName() {
        return guiName;
    }
}
