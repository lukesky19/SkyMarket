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
package com.github.lukesky19.skymarket;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skymarket.commands.AliasesCommands;
import com.github.lukesky19.skymarket.commands.SkyMarketCommand;
import com.github.lukesky19.skymarket.configuration.LocaleManager;
import com.github.lukesky19.skymarket.configuration.SettingsManager;
import com.github.lukesky19.skymarket.configuration.MarketConfigManager;
import com.github.lukesky19.skymarket.listener.InventoryListener;
import com.github.lukesky19.skymarket.manager.*;
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
 * This class is the entry point to the plugin.
 */
public final class SkyMarket extends JavaPlugin {
    private SettingsManager settingsLoader;
    private LocaleManager localeLoader;
    private MarketConfigManager marketConfigManager;
    private MarketDataManager marketDataManager;
    private GUIManager guiManager;
    private MarketManager marketManager;
    private Economy economy;

    /**
     * Default Constructor.
     */
    public SkyMarket() {}

    /**
     * Gets the economy that Vault returned.
     * @return An {@link Economy} instance.
     */
    public @NotNull Economy getEconomy() {
        return this.economy;
    }

    /**
     * Sets-up the plugin when started.
     */
    @Override
    public void onEnable() {
        boolean skyLib = checkSkyLibVersion();
        if(!skyLib) return;

        boolean econ = setupEconomy();
        if(!econ) return;

        settingsLoader = new SettingsManager(this);
        localeLoader = new LocaleManager(this, this.settingsLoader);
        guiManager = new GUIManager(this);
        marketConfigManager = new MarketConfigManager(this);
        marketDataManager = new MarketDataManager();
        TransactionManager transactionManager = new TransactionManager(this, localeLoader, guiManager);
        ButtonManager buttonManager = new ButtonManager(this, marketDataManager, transactionManager, guiManager);
        TradeManager tradeManager = new TradeManager(this);
        marketManager = new MarketManager(this, localeLoader, guiManager, marketConfigManager, marketDataManager, buttonManager, tradeManager);

        this.getServer().getPluginManager().registerEvents(new InventoryListener(guiManager), this);

        // Register commands
        SkyMarketCommand skyMarketCommand = new SkyMarketCommand(this, localeLoader, marketManager);
        AliasesCommands commandAliasManager = new AliasesCommands(settingsLoader, marketManager);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            Commands commandRegistrar = commands.registrar();

            commandRegistrar.register(skyMarketCommand.createCommand(),
                    "Command to manage and use the SkyMarket plugin.", List.of("market", "skm"));

            for(LiteralCommandNode<CommandSourceStack> cmd : commandAliasManager.getAliases()) {
                commandRegistrar.register(cmd);
            }
        });

        reload();
    }

    /**
     * Cleans up any data when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        if(this.guiManager != null) {
            this.guiManager.closeOpenGUIs(true);
        }
    }

    /**
     * Reloads the plugin.
     */
    public void reload() {
        this.settingsLoader.reload();
        this.localeLoader.reload();
        this.marketConfigManager.reload();
        this.marketDataManager.clearMarketData();
        this.marketManager.reload();
    }

    /**
     * Checks for Vault as a dependency and sets up the Economy instance.
     */
    private boolean setupEconomy() {
        if(getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                this.economy = rsp.getProvider();

                return true;
            }
        }

        this.getComponentLogger().error(MiniMessage.miniMessage().deserialize("<red>SkyShop has been disabled due to no Vault dependency found!</red>"));
        this.getServer().getPluginManager().disablePlugin(this);
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
        if(skyLib != null) {
            String version = skyLib.getPluginMeta().getVersion();
            String[] splitVersion = version.split("\\.");
            int second = Integer.parseInt(splitVersion[1]);

            if(second >= 3) {
                return true;
            }
        }

        this.getComponentLogger().error(AdventureUtil.serialize("SkyLib Version 1.3.0.0 or newer is required to run this plugin."));
        this.getServer().getPluginManager().disablePlugin(this);
        return false;
    }
}
