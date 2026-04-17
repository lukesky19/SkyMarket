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
package com.github.lukesky19.skymarket.data.slot;

import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skymarket.locale.LocaleManager;
import com.github.lukesky19.skymarket.market.config.chest.ChestConfig;
import com.github.lukesky19.skymarket.interfaces.IMarketSlot;
import com.github.lukesky19.skymarket.util.PluginUtils;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * This market slot is used in chest-style markets.
 */
public class ChestMarketSlot implements IMarketSlot {
    private final @NonNull ComponentLogger logger;
    private final @NonNull LocaleManager localeManager;

    private final int pageNum;
    private final int slotNum;

    // Transaction Name
    private final @NonNull String transactionName;

    // Item Config
    private final @NonNull ItemStackConfig displayItemConfig;
    private final @NonNull ItemStackConfig transactionItemConfig;
    private final @Nullable Integer amount;
    private final @Nullable Map<Enchantment, Integer> enchantments;

    // Commands
    private final @NonNull List<String> buyCommands;
    private final @NonNull List<String> sellCommands;

    // Prices
    private final double buyPrice;
    private final double sellPrice;
    private final @NonNull List<ItemStack> buyItems = new ArrayList<>();

    // Limits
    private final int serverPurchaseLimit;
    private final int serverSellLimit;
    private final int playerPurchaseLimit;
    private final int playerSellLimit;

    // Server Counts
    private int serverPurchaseCount;
    private int serverSoldCount;

    // Player Counts
    private final @NonNull Map<UUID, Integer> playerPurchaseCounts = new HashMap<>();
    private final @NonNull Map<UUID, Integer> playerSoldCounts = new HashMap<>();

    private long refreshTime;

    /**
     * Constructor
     * @param logger A {@link ComponentLogger} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param marketEntry The {@link ChestConfig.MarketEntry}.
     * @param pageNum The page number.
     * @param slotNum The slot number.
     * @param serverPurchaseLimit The server buy limit.
     * @param serverSellLimit The server sell limit.
     * @param playerPurchaseLimit The player buy limit.
     * @param playerSellLimit The player sell limit.
     */
    public ChestMarketSlot(
            @NonNull ComponentLogger logger,
            @NonNull LocaleManager localeManager,
            ChestConfig.@NonNull MarketEntry marketEntry,
            int pageNum,
            int slotNum,
            int serverPurchaseLimit,
            int serverSellLimit,
            int playerPurchaseLimit,
            int playerSellLimit) {
        this.logger = logger;
        this.localeManager = localeManager;

        this.pageNum = pageNum;
        this.slotNum = slotNum;

        // Transaction Name
        assert marketEntry.transactionName() != null; // Config is validated on load
        transactionName = marketEntry.transactionName();

        // Item
        this.displayItemConfig = marketEntry.displayItem();
        this.transactionItemConfig = marketEntry.transactionItem();
        this.amount = PluginUtils.getRandomAmount(marketEntry.amount().fixed(), marketEntry.amount().min(), marketEntry.amount().max());
        this.enchantments = PluginUtils.getRandomEnchantments(
                transactionItemConfig.itemType(),
                marketEntry.randomEnchants().enchantRandomly(),
                marketEntry.randomEnchants().min(),
                marketEntry.randomEnchants().max(),
                marketEntry.randomEnchants().treasure());

        // Commands
        this.buyCommands = marketEntry.buyCommands();
        this.sellCommands = marketEntry.sellCommands();

        // Prices
        ChestConfig.PriceConfig priceConfig = marketEntry.prices();
        if(priceConfig.buyFixed() != null) {
            this.buyPrice = priceConfig.buyFixed();
        } else if (priceConfig.buyMin() != null && priceConfig.buyMax() != null) {
            this.buyPrice = PluginUtils.calculatePrice(priceConfig.buyMin(), priceConfig.buyMax());
        } else {
            this.buyPrice = -1;
        }

        if(priceConfig.sellFixed() != null) {
            sellPrice = priceConfig.sellFixed();
        } else if(priceConfig.sellMin() != null && priceConfig.sellMax() != null) {
            sellPrice = PluginUtils.calculatePrice(priceConfig.sellMin(), priceConfig.sellMax());
        } else {
            this.sellPrice = -1;
        }

        // Buy Items
        priceConfig.buyItems().forEach(itemStackConfig -> {
            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            itemStackBuilder.fromItemStackConfig(itemStackConfig, null, List.of());
            itemStackBuilder.buildItemStack().ifPresent(buyItems::add);
        });

        // Limits
        this.serverPurchaseLimit = serverPurchaseLimit;
        this.serverSellLimit = serverSellLimit;
        this.playerPurchaseLimit = playerPurchaseLimit;
        this.playerSellLimit = playerSellLimit;
    }

    /**
     * Create the {@link ItemStack} to display in the GUI.
     * @param playerId The {@link UUID} of the player viewing the GUI.
     * @return An {@link Optional} {@link ItemStack}.
     */
    public @NonNull Optional<ItemStack> createDisplayStack(@NonNull UUID playerId) {
        if(displayItemConfig.itemType() == null || amount == null || amount <= 0) return Optional.empty();

        List<TagResolver.Single> placeholders = createPlaceholders(playerId);

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(displayItemConfig, null, placeholders);
        itemStackBuilder.setAmount(amount);
        if(enchantments != null) {
            enchantments.forEach(itemStackBuilder::addEnchantment);
        }

        return itemStackBuilder.buildItemStack();
    }

    /**
     * Create the transaction {@link ItemStack}.
     * @return An {@link Optional} {@link ItemStack}.
     */
    public @NonNull Optional<ItemStack> createTransactionStack() {
        if(transactionItemConfig.itemType() == null || amount == null || amount <= 0) return Optional.empty();

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.fromItemStackConfig(transactionItemConfig, null, List.of());
        itemStackBuilder.setAmount(amount);
        if(enchantments != null) {
            enchantments.forEach(itemStackBuilder::addEnchantment);
        }

        return itemStackBuilder.buildItemStack();
    }

    /**
     * Get the {@link List} of {@link String}s for the buy commands.
     * @return A {@link List} of {@link String}s.
     */
    public @NonNull List<String> getBuyCommands() {
        return buyCommands;
    }

    /**
     * Get the {@link List} of {@link String}s for the sell commands.
     * @return A {@link List} of {@link String}s.
     */
    public @NonNull List<String> getSellCommands() {
        return sellCommands;
    }

    /**
     * Get the transaction name.
     * @return The transaction name.
     */
    @Override
    public @NonNull String getTransactionName() {
        return transactionName;
    }

    /**
     * Get the page number.
     * @return The page number.
     */
    @Override
    public int getPageNumber() {
        return pageNum;
    }

    /**
     * Get the slot number.
     * @return The slot number.
     */
    @Override
    public int getSlotNumber() {
        return slotNum;
    }

    /**
     * Get the buy price.
     * @return The buy price.
     */
    public double getBuyPrice() {
        return buyPrice;
    }

    /**
     * Get the sell price.
     * @return The sell price.
     */
    public double getSellPrice() {
        return sellPrice;
    }

    /**
     * Get the {@link List} of {@link ItemStack}s to remove when buying.
     * @return A {@link List} of {@link ItemStack}s
     */
    public @NonNull List<ItemStack> getBuyItems() {
        return buyItems;
    }

    /**
     * Get the server purchase limit.
     * @return The server purchase limit.
     */
    public int getServerPurchaseLimit() {
        return serverPurchaseLimit;
    }

    /**
     * Get the server sell limit.
     * @return The server sell limit.
     */
    public int getServerSellLimit() {
        return serverSellLimit;
    }

    /**
     * Get the player purchase limit.
     * @return The player purchase limit.
     */
    public int getPlayerPurchaseLimit() {
        return playerPurchaseLimit;
    }

    /**
     * Get the player sell limit.
     * @return The player sell limit.
     */
    public int getPlayerSellLimit() {
        return playerSellLimit;
    }

    /**
     * Set the amount of times any player has purchased this slot.
     * @param amount The purchase count.
     */
    public void setServerPurchaseCount(int amount) {
        this.serverPurchaseCount = Math.max(0, amount);
    }

    /**
     * Add the amount to the total count any player has purchased this slot.
     * @param amount The purchase count.
     */
    public void addServerPurchaseCount(int amount) {
        this.serverPurchaseCount += Math.max(0, amount);
    }

    /**
     * Get the server purchase count.
     * @return  The server purchase count.
     */
    public int getServerPurchaseCount() {
        return serverPurchaseCount;
    }

    /**
     * Set the amount of times the player has purchased this slot.
     * @param playerId The {@link UUID} of the player.
     * @param amount The purchase count.
     */
    public void setPlayerPurchaseCount(@NonNull UUID playerId, int amount) {
        playerPurchaseCounts.put(playerId, Math.max(0, amount));
    }

    /**
     * Add the amount to the total count the player has purchased this slot.
     * @param playerId The {@link UUID} of the player.
     * @param amount The purchase count.
     */
    public void addPlayerPurchaseCount(@NonNull UUID playerId, int amount) {
        int currentAmount = playerPurchaseCounts.getOrDefault(playerId, 0);
        int updatedAmount = currentAmount + Math.max(0, amount);

        playerPurchaseCounts.put(playerId, updatedAmount);
    }

    /**
     * Get the player purchase count.
     * @param playerId The {@link UUID} of the player.
     * @return  The player purchase count.
     */
    public int getPlayerPurchasedCount(@NonNull UUID playerId) {
        return playerPurchaseCounts.getOrDefault(playerId, 0);
    }

    /**
     * Set the amount of times any player has sold this slot.
     * @param amount The sold count.
     */
    public void setServerSoldCount(int amount) {
        this.serverSoldCount = (Math.max(0, amount));
    }

    /**
     * Add the amount to the total count any player has sold this slot.
     * @param amount The sold count.
     */
    public void addServerSoldCount(int amount) {
        this.serverSoldCount += Math.max(0, amount);
    }

    /**
     * Get the server sold count.
     * @return  The server sold count.
     */
    public int getServerSoldCount() {
        return serverSoldCount;
    }

    /**
     * Set the amount of times the player has sold this slot.
     * @param playerId The {@link UUID} of the player.
     * @param amount The sold count.
     */
    public void setPlayerSoldCount(@NonNull UUID playerId, int amount) {
        playerSoldCounts.put(playerId, Math.max(0, amount));
    }

    /**
     * Add the amount to the total count the player has sold this slot.
     * @param playerId The {@link UUID} of the player.
     * @param amount The sold count.
     */
    public void addPlayerSoldCount(@NonNull UUID playerId, int amount) {
        int currentAmount = playerSoldCounts.getOrDefault(playerId, 0);
        int updatedAmount = currentAmount + Math.max(0, amount);

        playerSoldCounts.put(playerId, updatedAmount);
    }

    /**
     * Get the player sold count.
     * @param playerId The {@link UUID} of the player.
     * @return  The player sold count.
     */
    public int getPlayerSoldCount(@NonNull UUID playerId) {
        return playerSoldCounts.getOrDefault(playerId, 0);
    }

    @Override
    public void setRefreshTime(long refreshTime) {
        this.refreshTime = Math.max(0, refreshTime);
    }

    @Override
    public long getRefreshTime() {
        return refreshTime;
    }

    /**
     * Create the {@link List} of {@link TagResolver.Single} for the placeholders to use on the creation of the display item.
     * @param playerId The {@link UUID} of the player.
     * @return A {@link List} of {@link TagResolver.Single}.
     */
    private @NonNull List<TagResolver.Single> createPlaceholders(@NonNull UUID playerId) {
        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("buy_price", String.valueOf(buyPrice)));
        placeholders.add(Placeholder.parsed("sell_price", String.valueOf(sellPrice)));

        if(serverPurchaseLimit > 0 && playerPurchaseLimit > 0) {
            placeholders.add(Placeholder.parsed("buy_limit", String.valueOf(Math.min(serverPurchaseLimit, playerPurchaseLimit))));
        } else if(serverPurchaseLimit > 0) {
            placeholders.add(Placeholder.parsed("buy_limit", String.valueOf(serverPurchaseLimit)));
        } else if(playerPurchaseLimit > 0) {
            placeholders.add(Placeholder.parsed("buy_limit", String.valueOf(playerPurchaseLimit)));
        } else {
            placeholders.add(Placeholder.parsed("buy_limit", "0"));
        }

        if(serverSellLimit > 0 && playerSellLimit > 0) {
            placeholders.add(Placeholder.parsed("sell_limit", String.valueOf(Math.min(serverSellLimit, playerSellLimit))));
        } else if(serverSellLimit > 0) {
            placeholders.add(Placeholder.parsed("sell_limit", String.valueOf(serverSellLimit)));
        } else if(playerSellLimit > 0) {
            placeholders.add(Placeholder.parsed("sell_limit", String.valueOf(playerSellLimit)));
        } else {
            placeholders.add(Placeholder.parsed("sell_limit", "0"));
        }

        placeholders.add(Placeholder.parsed("server_buy_limit", String.valueOf(serverPurchaseLimit)));
        placeholders.add(Placeholder.parsed("server_sell_limit", String.valueOf(serverPurchaseLimit)));
        placeholders.add(Placeholder.parsed("player_buy_limit", String.valueOf(playerPurchaseLimit)));
        placeholders.add(Placeholder.parsed("player_sell_limit", String.valueOf(playerSellLimit)));

        int playerPurchaseCount = playerPurchaseCounts.getOrDefault(playerId, 0);
        int playerSoldCount = playerSoldCounts.getOrDefault(playerId, 0);
        if(serverPurchaseCount > 0 && playerPurchaseCount > 0) {
            placeholders.add(Placeholder.parsed("buy_count", String.valueOf(Math.min(serverPurchaseCount, playerPurchaseCount))));
        } else if(serverPurchaseCount > 0) {
            placeholders.add(Placeholder.parsed("buy_count", String.valueOf(serverPurchaseCount)));
        } else if(playerPurchaseCount > 0) {
            placeholders.add(Placeholder.parsed("buy_count", String.valueOf(playerPurchaseCount)));
        } else {
            placeholders.add(Placeholder.parsed("buy_count", "0"));
        }

        if(serverSoldCount > 0 && playerSoldCount > 0) {
            placeholders.add(Placeholder.parsed("sell_count", String.valueOf(Math.min(serverSoldCount, playerSoldCount))));
        } else if(serverSoldCount > 0) {
            placeholders.add(Placeholder.parsed("sell_count", String.valueOf(serverSoldCount)));
        } else if(playerSoldCount > 0) {
            placeholders.add(Placeholder.parsed("sell_count", String.valueOf(playerSoldCount)));
        } else {
            placeholders.add(Placeholder.parsed("sell_count", "0"));
        }

        placeholders.add(Placeholder.parsed("server_buy_count", String.valueOf(serverPurchaseCount)));
        placeholders.add(Placeholder.parsed("server_sell_count", String.valueOf(serverSoldCount)));
        placeholders.add(Placeholder.parsed("player_buy_count", String.valueOf(playerPurchaseCounts.getOrDefault(playerId, 0))));
        placeholders.add(Placeholder.parsed("player_sell_count", String.valueOf(playerSoldCounts.getOrDefault(playerId, 0))));

        if(refreshTime > 0) {
            placeholders.add(Placeholder.parsed("remaining_time", localeManager.getTimeText(refreshTime - System.currentTimeMillis())));
        }

        return placeholders;
    }
}