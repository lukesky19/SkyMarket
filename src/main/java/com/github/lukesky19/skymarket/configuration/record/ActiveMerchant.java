package com.github.lukesky19.skymarket.configuration.record;

import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public record ActiveMerchant(
        String marketName,
        List<MerchantRecipe> trades,
        HashMap<UUID, List<MerchantRecipe>> playerTrades,
        BukkitTask refreshTask,
        long resetTime) {}
