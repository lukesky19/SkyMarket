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
package com.github.lukesky19.skymarket.integration;

import com.github.lukesky19.skylib.api.integration.Hook;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.integration.hooks.EconomyHook;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * This class manages hooks into different plugins.
 */
public class HookManager {
    private final @NotNull Map<Class<?>, Hook> hooks = new HashMap<>();

    /**
     * Constructor
     * @param skyMarket A {@link SkyMarket} instance.
     */
    public HookManager(@NotNull SkyMarket skyMarket) {
        registerHook(EconomyHook.class, new EconomyHook(skyMarket));
    }

    /**
     * Register a hook.
     * @param hookClass The class.
     * @param hook The class instance.
     * @param <T> Parameter for any class that extends {@link Hook}.
     */
    public <T extends Hook> void registerHook(@NotNull Class<T> hookClass, @NotNull Hook hook) {
        hooks.put(hookClass, hook);
        hook.initialize();
    }

    /**
     * Get a hook.
     * @param hookClass The class.
     * @param <T> Parameter for any class that extends {@link Hook}.
     * @return The class instance.
     */
    public @NotNull <T extends Hook> T getHook(@NotNull Class<T> hookClass) {
        return hookClass.cast(hooks.get(hookClass));
    }
}