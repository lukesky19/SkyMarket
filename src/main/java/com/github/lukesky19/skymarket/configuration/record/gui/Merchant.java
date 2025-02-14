package com.github.lukesky19.skymarket.configuration.record.gui;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import com.github.lukesky19.skymarket.configuration.record.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

@ConfigSerializable
public record Merchant(
        @Nullable String configVersion,
        @Nullable String refreshTime,
        @Nullable String marketName,
        @Nullable String guiName,
        int numOfTrades,
        @NotNull HashMap<Integer, Trade> trades) {
    @ConfigSerializable
    public record Trade(@NotNull Integer limit, @NotNull Item input1, @NotNull Item input2,  @NotNull Item output) {}
}
