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

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.time.Time;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.configuration.LocaleManager;
import com.github.lukesky19.skymarket.data.config.Locale;
import com.github.lukesky19.skymarket.manager.MarketManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This class is used to create the /skymarket command.
 */
public class SkyMarketCommand {
    private final @NotNull SkyMarket skyMarket;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull MarketManager marketManager;

    /**
     * Constructor
     * @param skyMarket A {@link SkyMarket} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param marketManager A {@link MarketManager} instance.
     */
    public SkyMarketCommand(
            @NotNull SkyMarket skyMarket,
            @NotNull LocaleManager localeManager,
            @NotNull MarketManager marketManager) {
        this.skyMarket = skyMarket;
        this.localeManager = localeManager;
        this.marketManager = marketManager;
    }

    /**
     * Creates a {@link LiteralCommandNode} of type {@link CommandSourceStack} that represents the /skymarket command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack}.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("skymarket")
            .requires(ctx -> ctx.getSender().hasPermission("skymarket.commands.skymarket"));

        builder.then(Commands.literal("open")
            .requires(ctx -> ctx.getSender().hasPermission("skymarket.commands.skymarket.open") && ctx.getSender() instanceof Player)
            .then(Commands.argument("id", StringArgumentType.string())
                .suggests((context, suggestionsBuilder) -> {
                    for(String marketId : marketManager.getMarketIds()) {
                        suggestionsBuilder.suggest(marketId);
                    }

                    return suggestionsBuilder.buildFuture();
                })

                .executes(ctx -> {
                    String id = ctx.getArgument("id", String.class);
                    CommandSender sender = ctx.getSource().getSender();

                    return marketManager.openMarket(id, (Player) sender) ? 1 : 0;
                })
            )
        );

        builder.then(Commands.literal("reload")
            .requires(ctx -> ctx.getSender().hasPermission("skymarket.commands.skymarket.reload"))
            .executes(ctx -> {
                skyMarket.reload();

                Locale locale = localeManager.getLocale();

                ctx.getSource().getSender().sendMessage(AdventureUtil.serialize(locale.prefix() + locale.configReload()));

                return 1;
            })
        );

        builder.then(Commands.literal("refresh")
            .requires(ctx -> ctx.getSender().hasPermission("skymarket.commands.skymarket.refresh"))
            .then(Commands.argument("id", StringArgumentType.string())
                .suggests((context, suggestionsBuilder) -> {
                    for(String marketId : marketManager.getMarketIds()) {
                        suggestionsBuilder.suggest(marketId);
                    }

                    return suggestionsBuilder.buildFuture();
                })

                .executes(ctx -> {
                    String id = ctx.getArgument("id", String.class);
                    CommandSender sender = ctx.getSource().getSender();
                    Locale locale = localeManager.getLocale();

                    if(marketManager.refreshMarket(id)) {
                        return 1;
                    } else {
                        sender.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.invalidMarketId()));
                        return 0;
                    }
                })
            )
        );

        builder.then(Commands.literal("time")
            .requires(ctx -> ctx.getSender().hasPermission("skymarket.commands.skymarket.time"))
            .then(Commands.argument("id", StringArgumentType.string())
                .suggests((context, suggestionsBuilder) -> {
                    for(String marketId : marketManager.getMarketIds()) {
                        suggestionsBuilder.suggest(marketId);
                    }

                    return suggestionsBuilder.buildFuture();
                })

                .executes(ctx -> {
                    String id = ctx.getArgument("id", String.class);
                    CommandSender sender = ctx.getSource().getSender();
                    Locale locale = localeManager.getLocale();
                    @Nullable Time refreshTime = marketManager.getRefreshTime(id);
                    String marketName = marketManager.getMarketName(id);

                    if(refreshTime != null && marketName != null) {
                        StringBuilder stringBuilder = new StringBuilder();

                        if (refreshTime.years() > 0) {
                            if (refreshTime.years() > 1) {
                                stringBuilder.append(refreshTime.years()).append(" years ");
                            } else {
                                stringBuilder.append(refreshTime.years()).append(" year ");
                            }
                        }

                        if (refreshTime.months() > 0) {
                            if (refreshTime.months() > 1) {
                                stringBuilder.append(refreshTime.months()).append(" months ");
                            } else {
                                stringBuilder.append(refreshTime.months()).append(" month ");
                            }
                        }

                        if (refreshTime.weeks() > 0) {
                            if (refreshTime.weeks() > 1) {
                                stringBuilder.append(refreshTime.weeks()).append(" weeks ");
                            } else {
                                stringBuilder.append(refreshTime.weeks()).append(" week ");
                            }
                        }

                        if (refreshTime.days() > 0) {
                            if (refreshTime.days() > 1) {
                                stringBuilder.append(refreshTime.days()).append(" days ");
                            } else {
                                stringBuilder.append(refreshTime.days()).append(" day ");
                            }
                        }

                        if (refreshTime.hours() > 0) {
                            if (refreshTime.hours() > 1) {
                                stringBuilder.append(refreshTime.hours()).append(" hours ");
                            } else {
                                stringBuilder.append(refreshTime.hours()).append(" hour ");
                            }
                        }

                        if (refreshTime.minutes() > 0) {
                            if (refreshTime.minutes() > 1) {
                                stringBuilder.append(refreshTime.minutes()).append(" minutes ");
                            } else {
                                stringBuilder.append(refreshTime.minutes()).append(" minute ");
                            }
                        }

                        if (refreshTime.seconds() > 0) {
                            if (refreshTime.seconds() > 1) {
                                stringBuilder.append(refreshTime.seconds()).append(" seconds ");
                            } else {
                                stringBuilder.append(refreshTime.seconds()).append(" second ");
                            }
                        }

                        if (refreshTime.milliseconds() > 0) {
                            if (refreshTime.milliseconds() > 1) {
                                stringBuilder.append(refreshTime.milliseconds()).append(" milliseconds");
                            } else {
                                stringBuilder.append(refreshTime.milliseconds()).append(" millisecond");
                            }
                        }

                        List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("time", stringBuilder.toString()), Placeholder.parsed("market_name", marketName));

                        sender.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.marketRefreshTime(), placeholders));

                        return 1;
                    } else {
                        sender.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.invalidMarketId()));
                        return 0;
                    }
                })
            )
        );

        return builder.build();
    }
}
