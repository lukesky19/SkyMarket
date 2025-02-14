package com.github.lukesky19.skymarket.commands;

import com.github.lukesky19.skymarket.configuration.loader.SettingsLoader;
import com.github.lukesky19.skymarket.configuration.record.Settings;
import com.github.lukesky19.skymarket.manager.MarketManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to create command aliases to open markets.
 */
public class CommandAliases {
    private final SettingsLoader settingsLoader;
    private final MarketManager marketManager;

    /**
     * Constructor
     * @param settingsLoader A SettingsLoader instance.
     * @param marketManager A MarketManager instance.
     */
    public CommandAliases(SettingsLoader settingsLoader, MarketManager marketManager) {
        this.settingsLoader = settingsLoader;
        this.marketManager = marketManager;
    }

    /**
     * Gets a {@literal List<LiteralCommandNode<CommandSourceStack>>} that represents the command aliases.
     * @return A {@literal List<LiteralCommandNode<CommandSourceStack>>}
     */
    public List<LiteralCommandNode<CommandSourceStack>> getAliases() {
        List<LiteralCommandNode<CommandSourceStack>> commandAlises = new ArrayList<>();

        Settings settings = settingsLoader.getSettingsConfig();

        if(settings != null) {
            for(Settings.Alias alias : settings.aliases()) {
                commandAlises.add(createCommand(alias.alias(), alias.marketId()));
            }
        }

        return commandAlises;
    }

    /**
     * Creates a single {@literal LiteralCommandNode<CommandSourceStack>} that represents an alias command.
     * @param alias The command name.
     * @param marketId The market id that represents the market this command alias should open.
     * @return A {@literal LiteralCommandNode<CommandSourceStack>}
     */
    @NotNull
    private LiteralCommandNode<CommandSourceStack> createCommand(String alias, String marketId) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(alias)
                .requires(ctx ->
                        ctx.getSender() instanceof Player
                        && ctx.getSender().hasPermission("skymarket.commands.skymarket")
                        && ctx.getSender().hasPermission("skymarket.commands.skymarket.open"))
                .executes(ctx -> (marketManager.openMarket(marketId, ctx.getSource().getSender())) ? 1 : 0);

        return builder.build();
    }
}
