package com.github.lukesky19.skymarket.configuration.record;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

@ConfigSerializable
public record Item(
        @Nullable String material,
        @Nullable String name,
        @NotNull Item.Amount amount,
        @NotNull List<String> lore,
        @NotNull List<String> itemFlags,
        boolean enchantmentGlint,
        @NotNull Item.Enchants enchants,
        @NotNull List<PotionType> stewEffects,
        @NotNull List<PotionEffect> potionEffects,
        @NotNull Item.Color color) {

    @ConfigSerializable
    public record Amount(@Nullable Integer fixed, @Nullable Integer min, @Nullable Integer max) {}

    @ConfigSerializable
    public record Enchants(boolean enchantRandomly, int min, int max, boolean treasure, @Nullable HashMap<Integer, Enchantment> enchantments) {}

    @ConfigSerializable
    public record Enchantment(@Nullable  String enchantment, int level) {}

    @ConfigSerializable
    public record PotionEffect(@Nullable PotionType baseEffect, @Nullable List<PotionEffectType> extraEffects) {}

    @ConfigSerializable
    public record PotionType(@Nullable String type, double duration) {}

    @ConfigSerializable
    public record PotionEffectType(@Nullable String type, int level, double duration) {}

    @ConfigSerializable
    public record Color(boolean random, int red, int green, int blue) {}
}
