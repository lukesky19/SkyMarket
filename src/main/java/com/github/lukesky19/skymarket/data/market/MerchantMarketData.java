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

import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.api.time.TimeUtil;
import com.github.lukesky19.skylib.paper.api.gui.GUIType;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.locale.LocaleManager;
import com.github.lukesky19.skymarket.locale.Locale;
import com.github.lukesky19.skymarket.market.config.merchant.MerchantConfig;
import com.github.lukesky19.skymarket.market.config.common.AmountConfig;
import com.github.lukesky19.skymarket.market.config.common.RandomEnchantConfig;
import com.github.lukesky19.skymarket.data.slot.MerchantMarketSlot;
import com.github.lukesky19.skymarket.gui.guis.MerchantMarketGUI;
import com.github.lukesky19.skymarket.interfaces.IMarketData;
import com.github.lukesky19.skymarket.interfaces.IMarketSlot;
import com.github.lukesky19.skymarket.gui.GUIManager;
import com.github.lukesky19.skymarket.util.MarketIdUUIDKey;
import com.github.lukesky19.skymarket.util.PluginUtils;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * This class stores the market data for a merchant-style market.
 */
public class MerchantMarketData implements IMarketData {
    private final @NonNull SkyMarket skyMarket;
    private final @NonNull ComponentLogger logger;
    private final @NonNull LocaleManager localeManager;
    private final @NonNull GUIManager guiManager;

    private final @NonNull MerchantConfig config;
    private @NonNull List<MerchantConfig.Trade> entries;

    private final @NonNull String marketId;
    private final @NonNull String marketName;
    private final @NonNull String guiName;
    private final @NonNull Map<Integer, Map<Integer, IMarketSlot>> pageMap = new HashMap<>();
    private long refreshTime;

    /**
     * Constructor
     * @param skyMarket A {@link SkyMarket} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param config The {@link MerchantConfig}.
     * @param marketId The market id.
     * @param guiName The GUI's name.
     */
    public MerchantMarketData(
            @NonNull SkyMarket skyMarket,
            @NonNull LocaleManager localeManager,
            @NonNull GUIManager guiManager,
            @NonNull MerchantConfig config,
            @NonNull String marketId,
            @NonNull String guiName) {
        this.skyMarket = skyMarket;
        this.logger = skyMarket.getComponentLogger();
        this.localeManager = localeManager;
        this.guiManager = guiManager;
        this.config = config;
        this.entries = new ArrayList<>(config.trades());
        this.marketId = marketId;
        assert config.marketName() != null; // Validated on load
        this.marketName = config.marketName();
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
        return GUIType.MERCHANT;
    }

    @Override
    public @NonNull String getGUIName() {
        return guiName;
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
        entries = new ArrayList<>(config.trades());

        for(int slot = 0; slot < config.numOfTrades(); slot++) {
            refreshMarketSlot(0, slot);
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
                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.marketRefreshed(), placeholders)));

        return true;
    }

    @Override
    public boolean refreshMarketSlot(int pageNum, int slotNum) {
        if(entries.isEmpty()) {
            logger.warn(AdventureUtility.plain("Unable to refresh a market slot for market " + marketId + " due to no more entries available."));
            this.removeMarketSlot(pageNum, slotNum);
            return false;
        }

        int randomIndex = new Random().nextInt(entries.size());

        MerchantConfig.Trade trade = entries.get(randomIndex);

        entries.remove(randomIndex);

        Optional<ItemStack> optionalInputStack1 = createItemStack(trade.input1().item(), trade.input1().amount(), trade.input1().randomEnchants());
        Optional<ItemStack> optionalInputStack2 = createItemStack(trade.input2().item(), trade.input2().amount(), trade.input2().randomEnchants());
        Optional<ItemStack> optionalOutputStack = createItemStack(trade.output().item(), trade.output().amount(), trade.output().randomEnchants());

        if(optionalInputStack1.isEmpty() || optionalOutputStack.isEmpty()) return false;

        MerchantMarketSlot merchantMarketSlot = new MerchantMarketSlot(
                optionalInputStack1.get(),
                optionalInputStack2.orElse(null),
                optionalOutputStack.get(),
                slotNum,
                trade.serverLimit(),
                trade.playerLimit());

        this.setMarketSlot(pageNum, slotNum, merchantMarketSlot);

        if(trade.refreshTime() != null) {
            long delayMilliseconds = TimeUtil.stringToMillis(trade.refreshTime());
            if(delayMilliseconds > 0) {
                long refreshTime = System.currentTimeMillis() + delayMilliseconds;
                merchantMarketSlot.setRefreshTime(refreshTime);
            } else {
                merchantMarketSlot.setRefreshTime(0);
            }
        } else {
            merchantMarketSlot.setRefreshTime(0);
        }

        return true;
    }

    @Override
    public boolean openMarket(@NonNull Player player) {
        Locale locale = localeManager.getLocale();

        MarketIdUUIDKey identifier = new MarketIdUUIDKey(marketId, player.getUniqueId());
        MerchantMarketGUI tradeGUI = new MerchantMarketGUI(skyMarket, guiManager, player, identifier, guiName, this);
        boolean creationResult = tradeGUI.create();
        if(!creationResult) {
            logger.error(AdventureUtility.plain("Unable to create the InventoryView for a trade GUI of id " + marketId + " for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
            return false;
        }

        boolean updateResult = tradeGUI.update();
        if(!updateResult) {
            logger.error(AdventureUtility.plain("Unable to decorate a trade GUI of id " + marketId + " for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
            return false;
        }

        boolean openResult = tradeGUI.open();
        if(!openResult) {
            logger.error(AdventureUtility.plain("Unable to open a trade GUI of id " + marketId + " for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
            return false;
        }

        return true;
    }

    /**
     * Create the {@link ItemStack}.
     * @param itemStackConfig The {@link ItemStackConfig}.
     * @param amountConfig The {@link AmountConfig}.
     * @param randomEnchantConfig The {@link RandomEnchantConfig}.
     * @return An {@link Optional} {@link ItemStack}.
     */
    private @NonNull Optional<ItemStack> createItemStack(
            @NonNull ItemStackConfig itemStackConfig,
            @NonNull AmountConfig amountConfig,
            @NonNull RandomEnchantConfig randomEnchantConfig) {
        if(itemStackConfig.itemType() == null) return Optional.empty();

        Integer amount = PluginUtils.getRandomAmount(amountConfig.fixed(), amountConfig.min(), amountConfig.max());
        if(amount == null || amount <= 0) return Optional.empty();

        Map<Enchantment, Integer> enchantments =  PluginUtils.getRandomEnchantments(
                itemStackConfig.itemType(),
                randomEnchantConfig.enchantRandomly(),
                randomEnchantConfig.min(),
                randomEnchantConfig.max(),
                randomEnchantConfig.treasure());

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(itemStackConfig, null, List.of());
        itemStackBuilder.setAmount(amount);
        if(enchantments != null) {
            enchantments.forEach(itemStackBuilder::addEnchantment);
        }

        return itemStackBuilder.buildItemStack();
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