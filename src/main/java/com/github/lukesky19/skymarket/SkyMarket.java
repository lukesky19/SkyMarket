/*
    SkyMarket is a shop that rotates it's inventory after a set period of time.
    Copyright (C) 2024  lukeskywlker19

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
package com.github.lukesky19.skymarket;

import com.github.lukesky19.skymarket.commands.SkyMarketCommand;
import com.github.lukesky19.skymarket.configuration.manager.ItemsLoader;
import com.github.lukesky19.skymarket.configuration.manager.LocaleLoader;
import com.github.lukesky19.skymarket.configuration.manager.SettingsLoader;
import com.github.lukesky19.skymarket.configuration.manager.ShopLoader;
import com.github.lukesky19.skymarket.listener.InventoryListener;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SkyMarket extends JavaPlugin {
    private SettingsLoader settingsLoader;
    private LocaleLoader localeLoader;
    private ShopLoader shopLoader;
    private ItemsLoader itemsLoader;
    private Economy economy;
    private Boolean pluginState = true;

    public Economy getEconomy() {
        return this.economy;
    }

    public void setPluginState(Boolean pluginState) {
        this.pluginState = pluginState;
    }

    public Boolean isPluginEnabled() {
        return this.pluginState;
    }

    @Override
    public void onEnable() {
        setupEconomy();
        setupPlaceholderAPI();

        InventoryListener inventoryListener = new InventoryListener(this);
        settingsLoader = new SettingsLoader(this);
        localeLoader = new LocaleLoader(this, this.settingsLoader);
        itemsLoader = new ItemsLoader(this);
        shopLoader = new ShopLoader(this, settingsLoader, localeLoader, itemsLoader);

        SkyMarketCommand skyMarketCommand = new SkyMarketCommand(this, localeLoader, shopLoader);
        Objects.requireNonNull(Bukkit.getPluginCommand("skymarket")).setExecutor(skyMarketCommand);
        Objects.requireNonNull(Bukkit.getPluginCommand("skymarket")).setTabCompleter(skyMarketCommand);

        Bukkit.getPluginManager().registerEvents(inventoryListener, this);

        reload();
    }

    public void reload() {
        pluginState = true;

        this.settingsLoader.reload();
        this.localeLoader.reload();
        this.itemsLoader.reload();
        this.shopLoader.reload();
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                this.economy = rsp.getProvider();
            }
        } else {
            getComponentLogger().error(MiniMessage.miniMessage().deserialize("<red>SkyShop has been disabled due to no Vault dependency found!</red>"));
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void setupPlaceholderAPI() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getComponentLogger().error(MiniMessage.miniMessage().deserialize("<red>SkyShop has been disabled due to no PlaceholderAPI dependency found!</red>"));
            getServer().getPluginManager().disablePlugin(this);
        }
    }
}
