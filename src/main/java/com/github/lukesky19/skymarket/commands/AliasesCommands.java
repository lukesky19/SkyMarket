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
package com.github.lukesky19.skymarket.commands;

import com.github.lukesky19.skymarket.configuration.SettingsManager;
import com.github.lukesky19.skymarket.data.config.Settings;
import com.github.lukesky19.skymarket.manager.MarketManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This class is used to create command aliases to open markets.
 */
public class AliasesCommands {
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull MarketManager marketManager;

    /**
     * Constructor
     * @param settingsManager A {@link SettingsManager} instance.
     * @param marketManager A {@link MarketManager} instance.
     */
    public AliasesCommands(@NotNull SettingsManager settingsManager, @NotNull MarketManager marketManager) {
        this.settingsManager = settingsManager;
        this.marketManager = marketManager;
    }

    /**
     * Gets a {@link List} containing {@link LiteralCommandNode} of type {@link CommandSourceStack} that represents the command aliases.
     * @return A {@link List} containing {@link LiteralCommandNode} of type {@link CommandSourceStack}.
     */
    public @NotNull List<LiteralCommandNode<CommandSourceStack>> getAliases() {
        Settings settings = settingsManager.getSettingsConfig();
        if(settings == null) return List.of();

        return settings.aliases().stream()
                .filter(aliasConfig -> aliasConfig.alias() != null && aliasConfig.marketId() != null)
                .map(aliasConfig -> createCommand(aliasConfig.alias(), aliasConfig.marketId()))
                .toList();
    }

    /**
     * Creates a single {@link LiteralCommandNode} of type {@link CommandSourceStack} that represents an alias command.
     * @param alias The command name.
     * @param marketId The market id that represents the market this command alias should open.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack}.
     */
    private @NotNull LiteralCommandNode<CommandSourceStack> createCommand(@NotNull String alias, @NotNull String marketId) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(alias)
                .requires(ctx ->
                        ctx.getSender() instanceof Player
                        && ctx.getSender().hasPermission("skymarket.commands.skymarket")
                        && ctx.getSender().hasPermission("skymarket.commands.skymarket.open"))
                .executes(ctx -> (marketManager.openMarket(marketId, (Player) ctx.getSource().getSender())) ? 1 : 0);

        return builder.build();
    }
}
