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
package com.github.lukesky19.skymarket.commands;

import com.github.lukesky19.skylib.format.FormatUtil;
import com.github.lukesky19.skylib.record.Time;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.configuration.loader.LocaleLoader;
import com.github.lukesky19.skymarket.configuration.record.Locale;
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

import java.util.*;

/**
 * This class is used to create the /skymarket command.
 */
public class SkyMarketCommand {
    private final SkyMarket skyMarket;
    private final LocaleLoader localeLoader;
    private final MarketManager marketManager;

    /**
     * Constructor
     * @param skyMarket A SkyMarket plugin instance.
     * @param localeLoader A LocaleLoader instance.
     * @param marketManager A MarketManager instance.
     */
    public SkyMarketCommand(
            SkyMarket skyMarket,
            LocaleLoader localeLoader,
            MarketManager marketManager) {
        this.skyMarket = skyMarket;
        this.localeLoader = localeLoader;
        this.marketManager = marketManager;
    }

    /**
     * Creates a single {@literal LiteralCommandNode<CommandSourceStack>} that represents the /skymarket command.
     * @return A {@literal LiteralCommandNode<CommandSourceStack>}
     */
    public LiteralCommandNode<CommandSourceStack> createCommand() {
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

                    return (marketManager.openMarket(id, sender)) ? 1 : 0;
                })
            )
        );

        builder.then(Commands.literal("reload")
            .requires(ctx -> ctx.getSender().hasPermission("skymarket.commands.skymarket.reload"))
            .executes(ctx -> {
                skyMarket.reload(false);

                Locale locale = localeLoader.getLocale();

                ctx.getSource().getSender().sendMessage(FormatUtil.format(locale.prefix() + locale.configReload()));

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
                    Locale locale = localeLoader.getLocale();

                    if(marketManager.refreshMarket(id)) {
                        return 1;
                    } else {
                        sender.sendMessage(FormatUtil.format(locale.prefix() + locale.invalidMarketId()));
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
                    Locale locale = localeLoader.getLocale();
                    Time time = marketManager.getRefreshTime(id);
                    String marketName = marketManager.getMarketName(id);
                    if(time != null && marketName != null) {
                        StringBuilder stringBuilder = new StringBuilder();

                        if (time.years() > 0) {
                            if (time.years() > 1) {
                                stringBuilder.append(time.years()).append(" years ");
                            } else {
                                stringBuilder.append(time.years()).append(" year ");
                            }
                        }

                        if (time.months() > 0) {
                            if (time.months() > 1) {
                                stringBuilder.append(time.months()).append(" months ");
                            } else {
                                stringBuilder.append(time.months()).append(" month ");
                            }
                        }

                        if (time.weeks() > 0) {
                            if (time.weeks() > 1) {
                                stringBuilder.append(time.weeks()).append(" weeks ");
                            } else {
                                stringBuilder.append(time.weeks()).append(" week ");
                            }
                        }

                        if (time.days() > 0) {
                            if (time.days() > 1) {
                                stringBuilder.append(time.days()).append(" days ");
                            } else {
                                stringBuilder.append(time.days()).append(" day ");
                            }
                        }

                        if (time.hours() > 0) {
                            if (time.hours() > 1) {
                                stringBuilder.append(time.hours()).append(" hours ");
                            } else {
                                stringBuilder.append(time.hours()).append(" hour ");
                            }
                        }

                        if (time.minutes() > 0) {
                            if (time.minutes() > 1) {
                                stringBuilder.append(time.minutes()).append(" minutes ");
                            } else {
                                stringBuilder.append(time.minutes()).append(" minute ");
                            }
                        }

                        if (time.seconds() > 0) {
                            if (time.seconds() > 1) {
                                stringBuilder.append(time.seconds()).append(" seconds ");
                            } else {
                                stringBuilder.append(time.seconds()).append(" second ");
                            }
                        }

                        if (time.milliseconds() > 0) {
                            if (time.milliseconds() > 1) {
                                stringBuilder.append(time.milliseconds()).append(" milliseconds");
                            } else {
                                stringBuilder.append(time.milliseconds()).append(" millisecond");
                            }
                        }

                        List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("time", stringBuilder.toString()), Placeholder.parsed("market_name", marketName));

                        sender.sendMessage(FormatUtil.format(locale.prefix() + locale.marketRefreshTime(), placeholders));

                        return 1;
                    } else {
                        sender.sendMessage(FormatUtil.format(locale.prefix() + locale.invalidMarketId()));
                        return 0;
                    }
                })
            )
        );

        return builder.build();
    }
}
