package com.github.lukesky19.skymarket.configuration.record;

import com.github.lukesky19.skylib.gui.GUIButton;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

public record ActiveMarket(
        String marketName,
        HashMap<Integer, GUIButton> buttons,
        HashMap<UUID, HashMap<Integer, Integer>> buySlotLimits,
        HashMap<UUID, HashMap<Integer, Integer>> sellSlotLimits,
        BukkitTask refreshTask,
        long resetTime) {}
