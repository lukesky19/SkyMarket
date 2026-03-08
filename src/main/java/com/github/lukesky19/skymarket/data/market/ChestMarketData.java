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
package com.github.lukesky19.skymarket.data.market;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.time.TimeUtil;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.locale.LocaleManager;
import com.github.lukesky19.skymarket.locale.Locale;
import com.github.lukesky19.skymarket.market.config.chest.ChestConfig;
import com.github.lukesky19.skymarket.data.slot.ChestMarketSlot;
import com.github.lukesky19.skymarket.gui.guis.ChestMarketGUI;
import com.github.lukesky19.skymarket.interfaces.IMarketData;
import com.github.lukesky19.skymarket.interfaces.IMarketSlot;
import com.github.lukesky19.skymarket.gui.GUIManager;
import com.github.lukesky19.skymarket.transaction.TransactionManager;
import com.github.lukesky19.skymarket.util.MarketIdUUIDKey;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * This class stores the market data for a chest-style market.
 */
public class ChestMarketData implements IMarketData {
    private final @NonNull SkyMarket skyMarket;
    private final @NonNull ComponentLogger logger;
    private final @NonNull LocaleManager localeManager;
    private final @NonNull GUIManager guiManager;
    private final @NonNull TransactionManager transactionManager;

    private final @NonNull ChestConfig config;
    private @NonNull List<ChestConfig.MarketEntry> entries;

    private final @NonNull String marketId;
    private final @NonNull String marketName;
    private final @NonNull GUIType guiType;
    private final @NonNull String guiName;

    private final @NonNull Map<Integer, Map<Integer, IMarketSlot>> pageMap = new HashMap<>();
    private long refreshTime;

    /**
     * Constructor
     * @param skyMarket A {@link SkyMarket} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param transactionManager A {@link TransactionManager} instance.
     * @param config The {@link ChestConfig}.
     * @param marketId The market id.
     * @param guiType The {@link GUIType}.
     * @param guiName The GUI's name.
     */
    public ChestMarketData(
            @NonNull SkyMarket skyMarket,
            @NonNull LocaleManager localeManager,
            @NonNull GUIManager guiManager,
            @NonNull TransactionManager transactionManager,
            @NonNull ChestConfig config,
            @NonNull String marketId,
            @NonNull GUIType guiType,
            @NonNull String guiName) {
        this.skyMarket = skyMarket;
        this.logger = skyMarket.getComponentLogger();
        this.localeManager = localeManager;
        this.guiManager = guiManager;
        this.transactionManager = transactionManager;
        this.config = config;
        this.entries = new ArrayList<>(config.entries());
        this.marketId = marketId;
        assert config.marketName() != null; // Validated on load
        this.marketName = config.marketName();
        this.guiType = guiType;
        this.guiName = guiName;
    }

    @Override
    public @NonNull String getMarketId() {
        return marketId;
    }

    @Override
    public @NonNull String getMarketName() {
        return marketName;
    }

    @Override
    public @NonNull GUIType getGUIType() {
        return guiType;
    }

    @Override
    public @NonNull String getGUIName() {
        return guiName;
    }

    /**
     * Get the {@link ChestConfig.GuiData}.
     * @return The {@link ChestConfig.GuiData}.
     */
    public ChestConfig.@NonNull GuiData getGUIData() {
        return config.guiData();
    }

    @Override
    public @Nullable IMarketSlot getMarketSlot(int pageNum, int slotNum) {
        Map<Integer, IMarketSlot> slotMap = pageMap.get(pageNum);
        if(slotMap == null) return null;

        return slotMap.get(slotNum);
    }

    @Override
    public @NonNull Collection<IMarketSlot> getMarketSlotsAsCollection(int pageNum) {
        Map<Integer, IMarketSlot> slotMap = pageMap.get(pageNum);
        if(slotMap == null) return new ArrayList<>();

        return slotMap.values();
    }

    @Override
    public @NonNull Map<Integer, IMarketSlot> getMarketSlotsAsMap(int pageNum) {
        Map<Integer, IMarketSlot> slotMap = pageMap.get(pageNum);
        if(slotMap == null) return new HashMap<>();

        return slotMap;
    }

    @Override
    public @NonNull Collection<IMarketSlot> getMarketSlots() {
        Collection<IMarketSlot> collection = new ArrayList<>();

        pageMap.values().forEach(slotMap -> collection.addAll(slotMap.values()));

        return collection;
    }

    @Override
    public void setMarketSlot(int pageNum, int slotNum, @NonNull IMarketSlot marketSlot) {
        Map<Integer, IMarketSlot> slotMap = pageMap.computeIfAbsent(pageNum, num -> new HashMap<>());

        slotMap.put(slotNum, marketSlot);
    }

    @Override
    public void removeMarketSlot(int pageNum, int slotNum) {
        Map<Integer, IMarketSlot> slotMap = pageMap.get(pageNum);
        if(slotMap == null) return;

        slotMap.remove(slotNum);

        if(slotMap.isEmpty()) {
            pageMap.remove(pageNum);
        }
    }

    @Override
    public void clearMarketSlots(int pageNum) {
        pageMap.remove(pageNum);
    }

    @Override
    public void clearMarketSlots() {
        pageMap.clear();
    }

    @Override
    public boolean refreshMarket() {
        entries = new ArrayList<>(config.entries());

        for(ChestConfig.PlaceholderConfig placeholderConfig : config.guiData().placeholderSlots()) {
            if(placeholderConfig.pageNum() == null || placeholderConfig.slotNum() == null) continue;

            refreshMarketSlot(placeholderConfig.pageNum(), placeholderConfig.slotNum());
        }

        // Set the refresh time in the market data
        if(config.refreshTime() != null) {
            long delayMilliseconds = TimeUtil.stringToMillis(config.refreshTime());

            if(delayMilliseconds > 0) {
                long refreshTime = System.currentTimeMillis() + delayMilliseconds;
                this.setRefreshTime(refreshTime);
            } else {
                this.setRefreshTime(0);
            }
        } else {
            this.setRefreshTime(0);
        }

        Locale locale = localeManager.getLocale();
        // Create the placeholders list
        List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("market_name", marketName));
        // Tell all online players that the market was refreshed.
        skyMarket.getServer().getOnlinePlayers().forEach(player ->
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.marketRefreshed(), placeholders)));

        return true;
    }

    @Override
    public boolean refreshMarketSlot(int pageNum, int slotNum) {
        if(entries.isEmpty()) {
            logger.warn(AdventureUtil.deserialize("Unable to refresh a market slot for market " + marketId + " due to no more entries available."));
            this.removeMarketSlot(pageNum, slotNum);
            return false;
        }

        int randomIndex = new Random().nextInt(entries.size());

        ChestConfig.MarketEntry marketEntry = entries.get(randomIndex);

        entries.remove(randomIndex);

        ChestMarketSlot chestMarketSlot = new ChestMarketSlot(
                logger, localeManager, marketEntry, pageNum, slotNum,
                marketEntry.serverBuyLimit(), marketEntry.serverSellLimit(),
                marketEntry.playerBuyLimit(), marketEntry.playerSellLimit());

        this.setMarketSlot(pageNum, slotNum, chestMarketSlot);

        if(marketEntry.refreshTime() != null) {
            long delayMilliseconds = TimeUtil.stringToMillis(marketEntry.refreshTime());
            if(delayMilliseconds > 0) {
                long refreshTime = System.currentTimeMillis() + delayMilliseconds;
                chestMarketSlot.setRefreshTime(refreshTime);
            } else {
                chestMarketSlot.setRefreshTime(0);
            }
        } else {
            chestMarketSlot.setRefreshTime(0);
        }

        return true;
    }

    @Override
    public boolean openMarket(@NonNull Player player) {
        Locale locale = localeManager.getLocale();

        MarketIdUUIDKey identifier = new MarketIdUUIDKey(marketId, player.getUniqueId());
        ChestMarketGUI marketGUI = new ChestMarketGUI(skyMarket, guiManager, player, identifier, localeManager, transactionManager, guiType, guiName, this);
        boolean creationResult = marketGUI.create();
        if(!creationResult) {
            logger.error(AdventureUtil.deserialize("Unable to create the InventoryView for a market GUI of id " + marketId + " for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
            return false;
        }

        boolean updateResult = marketGUI.update();
        if(!updateResult) {
            logger.error(AdventureUtil.deserialize("Unable to decorate a market GUI of id " + marketId + " for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
            return false;
        }

        boolean openResult = marketGUI.open();
        if(!openResult) {
            logger.error(AdventureUtil.deserialize("Unable to open a market GUI of id " + marketId + " for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
            return false;
        }

        return true;
    }

    @Override
    public void setRefreshTime(long refreshTime) {
        this.refreshTime = refreshTime;
    }

    @Override
    public long getRefreshTime() {
        return refreshTime;
    }
}