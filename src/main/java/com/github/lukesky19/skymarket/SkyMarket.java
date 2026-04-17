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

import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import com.github.lukesky19.skymarket.commands.AliasesCommands;
import com.github.lukesky19.skymarket.commands.SkyMarketCommand;
import com.github.lukesky19.skymarket.gui.GUIManager;
import com.github.lukesky19.skymarket.integration.HookManager;
import com.github.lukesky19.skymarket.locale.LocaleManager;
import com.github.lukesky19.skymarket.market.MarketDataManager;
import com.github.lukesky19.skymarket.market.MarketManager;
import com.github.lukesky19.skymarket.settings.SettingsManager;
import com.github.lukesky19.skymarket.market.MarketConfigManager;
import com.github.lukesky19.skymarket.listener.InventoryListener;
import com.github.lukesky19.skymarket.task.TaskManager;
import com.github.lukesky19.skymarket.transaction.TransactionManager;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.List;

/**
 * This class is the entry point to the plugin.
 */
public final class SkyMarket extends SkyPlugin {
    private SettingsManager settingsManager;
    private LocaleManager localeManager;
    private MarketConfigManager marketConfigManager;
    private MarketDataManager marketDataManager;
    private GUIManager guiManager;
    private MarketManager marketManager;
    private TaskManager taskManager;

    /**
     * Default Constructor.
     */
    public SkyMarket() {}

    /**
     * Sets-up the plugin when started.
     */
    @Override
    public void onEnable() {
        boolean skyLib = checkSkyLibVersion();
        if(!skyLib) return;

        settingsManager = new SettingsManager(this);
        localeManager = new LocaleManager(this, this.settingsManager);
        guiManager = new GUIManager();
        marketConfigManager = new MarketConfigManager(this, settingsManager);
        marketDataManager = new MarketDataManager();
        HookManager hookManager = new HookManager(this);
        TransactionManager transactionManager = new TransactionManager(this, localeManager, hookManager);
        marketManager = new MarketManager(this, localeManager, guiManager, transactionManager, marketConfigManager, marketDataManager);
        taskManager = new TaskManager(this, marketDataManager, guiManager);

        this.getServer().getPluginManager().registerEvents(new InventoryListener(guiManager), this);

        // Register commands
        SkyMarketCommand skyMarketCommand = new SkyMarketCommand(this, localeManager, marketManager);
        AliasesCommands commandAliasManager = new AliasesCommands(settingsManager, marketManager);
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

        if(this.taskManager != null) {
            this.taskManager.stopRefreshTask();
        }
    }

    /**
     * Reloads the plugin.
     */
    public void reload() {
        this.guiManager.closeOpenGUIs(false);

        this.settingsManager.loadConfiguration();
        this.localeManager.loadConfiguration();
        this.marketConfigManager.reload();
        this.marketDataManager.clearMarketData();
        this.marketManager.reload();

        this.taskManager.stopRefreshTask();
        this.taskManager.startRefreshTask();
    }

    /**
     * Checks if the Server has the proper SkyLib version.
     * @return true if it does, false if not.
     */
    private boolean checkSkyLibVersion() {
        PluginManager pluginManager = this.getServer().getPluginManager();
        Plugin skyLib = pluginManager.getPlugin("SkyLib");
        if(skyLib != null) {
            String version = skyLib.getPluginMeta().getVersion();
            String[] splitVersion = version.split("\\.");
            int first = Integer.parseInt(splitVersion[0]);

            if(first >= 2) {
                return true;
            }
        }

        this.getComponentLogger().error(AdventureUtility.plain("SkyLib Version 2.0.0.0 or newer is required to run this plugin."));
        this.getServer().getPluginManager().disablePlugin(this);
        return false;
    }
}