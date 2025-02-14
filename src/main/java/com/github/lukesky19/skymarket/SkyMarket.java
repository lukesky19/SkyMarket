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

import com.github.lukesky19.skylib.format.FormatUtil;
import com.github.lukesky19.skymarket.commands.CommandAliases;
import com.github.lukesky19.skymarket.commands.SkyMarketCommand;
import com.github.lukesky19.skymarket.configuration.loader.LocaleLoader;
import com.github.lukesky19.skymarket.configuration.loader.SettingsLoader;
import com.github.lukesky19.skymarket.configuration.loader.MarketLoader;
import com.github.lukesky19.skymarket.listener.InventoryListener;
import com.github.lukesky19.skymarket.manager.MarketManager;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The initial class that represents SkyMarket.
 */
public final class SkyMarket extends JavaPlugin {
    private SettingsLoader settingsLoader;
    private LocaleLoader localeLoader;
    private MarketLoader marketLoader;
    private MarketManager marketManager;
    private Economy economy;

    /**
     * Gets the economy that Vault returned.
     * @return An Economy instance.
     */
    @NotNull
    public Economy getEconomy() {
        return this.economy;
    }

    /**
     * Sets-up the plugin when started.
     */
    @Override
    public void onEnable() {
        boolean skyLib = checkSkyLibVersion();
        if(!skyLib) {
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        boolean econ = setupEconomy();
        if(!econ) {
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        settingsLoader = new SettingsLoader(this);
        localeLoader = new LocaleLoader(this, this.settingsLoader);
        marketLoader = new MarketLoader(this);
        marketManager = new MarketManager(this, localeLoader, marketLoader);

        this.getServer().getPluginManager().registerEvents(new InventoryListener(marketManager), this);

        reload(true);
    }

    /**
     * Cleans up any data when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        if(this.marketManager != null) {
            this.marketManager.closeMarkets(true);
        }
    }

    /**
     * Reloads the plugin.
     * @param onEnable If the reload is occurring on startup or not.
     */
    public void reload(boolean onEnable) {
        this.settingsLoader.reload();
        this.localeLoader.reload();
        this.marketLoader.reload();
        this.marketManager.reload();

        if(onEnable) {
            SkyMarketCommand skyMarketCommand = new SkyMarketCommand(this, localeLoader, marketManager);
            CommandAliases commandAliases = new CommandAliases(settingsLoader, marketManager);
            this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
                Commands commandRegistrar = commands.registrar();

                commandRegistrar.register(skyMarketCommand.createCommand(),
                        "Command to manage and use the SkyMarket plugin.", List.of("market", "skm"));

                for(LiteralCommandNode<CommandSourceStack> cmd : commandAliases.getAliases()) {
                    commandRegistrar.register(cmd);
                }
            });
        }
    }

    /**
     * Sets up the Economy by getting it from Vault.
     * @return true of setup successfully, false if not.
     */
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                this.economy = rsp.getProvider();
                return true;
            }
        } else {
            getComponentLogger().error(MiniMessage.miniMessage().deserialize("<red>SkyShop has been disabled due to no Vault dependency found!</red>"));
        }

        return false;
    }

    /**
     * Checks if the Server has the proper SkyLib version.
     * @return true if it does, false if not.
     */
    @SuppressWarnings("UnstableApiUsage")
    private boolean checkSkyLibVersion() {
        PluginManager pluginManager = this.getServer().getPluginManager();
        Plugin skyLib = pluginManager.getPlugin("SkyLib");
        if (skyLib != null) {
            String version = skyLib.getPluginMeta().getVersion();
            String[] splitVersion = version.split("\\.");
            int second = Integer.parseInt(splitVersion[1]);

            if(second >= 2) {
                return true;
            } else {
                this.getComponentLogger().error(FormatUtil.format("SkyLib Version 1.2.0.0 or newer is required to run this plugin."));
                return false;
            }
        }

        return true;
    }
}
