package com.github.lukesky19.skymarket.configuration.record.gui;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import com.github.lukesky19.skymarket.configuration.record.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;

@ConfigSerializable
public record Chest(
        @Nullable String configVersion,
        @Nullable String refreshTime,
        @Nullable String marketName,
        @NotNull GuiData guiData,
        @NotNull LinkedHashMap<Integer, ItemEntry> items) {

    @ConfigSerializable
    public record GuiData(
            @Nullable String guiType,
            @Nullable String guiName,
            LinkedHashMap<Integer, GuiEntry> entries) {}

    @ConfigSerializable
    public record GuiEntry(
            @Nullable String type,
            int slot,
            @NotNull Item item) {}

    @ConfigSerializable
    public record ItemEntry(
            @Nullable String type,
            @NotNull Integer limit,
            @NotNull Item item,
            @NotNull Commands commands,
            @NotNull Price price) {}

    @ConfigSerializable
    public record Commands(
            @Nullable List<String> buyCommands,
            @Nullable List<String> sellCommands) {}

    @ConfigSerializable
    public record Price(
            @NotNull List<Item> buyItems,
            @Nullable Double buyFixed,
            @Nullable Double sellFixed,
            @Nullable Double buyMin,
            @Nullable Double buyMax,
            @Nullable Double sellMin,
            @Nullable Double sellMax) {}
}
