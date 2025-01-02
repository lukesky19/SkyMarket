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
import com.github.lukesky19.skymarket.configuration.manager.LocaleLoader;
import com.github.lukesky19.skymarket.configuration.record.Locale;
import com.github.lukesky19.skymarket.manager.MarketManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

import java.util.*;

public class SkyMarketCommand {
    private final SkyMarket skyMarket;
    private final LocaleLoader localeLoader;
    private final MarketManager marketManager;

    public SkyMarketCommand(
            SkyMarket skyMarket,
            LocaleLoader localeLoader,
            MarketManager marketManager) {
        this.skyMarket = skyMarket;
        this.localeLoader = localeLoader;
        this.marketManager = marketManager;
    }

    public LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("skymarket")
            .requires(ctx -> ctx.getSender().hasPermission("skymarket.commands.skymarket"))
                .executes(ctx -> {
                    Locale locale = localeLoader.getLocale();

                    if(ctx.getSource().getSender() instanceof Player player) {
                        if (marketManager.getMarketGUI() != null) {
                            marketManager.getMarketGUI().openInventory(skyMarket, player);

                            return 1;
                        } else {
                            player.sendMessage(FormatUtil.format(locale.prefix() + locale.marketOpenError()));

                            return 0;
                        }
                    } else {
                        skyMarket.getComponentLogger().info(FormatUtil.format(locale.inGameOnly()));

                        return 0;
                    }
                });

        builder.then(Commands.literal("reload")
            .requires(ctx -> ctx.getSender().hasPermission("skymarket.commands.skymarket.reload"))
            .executes(ctx -> {
                skyMarket.reload();

                Locale locale = localeLoader.getLocale();

                ctx.getSource().getSender().sendMessage(FormatUtil.format(locale.prefix() + locale.configReload()));

                return 1;
            })
        );

        builder.then(Commands.literal("refresh")
            .requires(ctx -> ctx.getSender().hasPermission("skymarket.commands.skymarket.refresh"))
            .executes(ctx -> {
                marketManager.refreshMarket();

                return 1;
            })
        );

        builder.then(Commands.literal("time")
            .requires(ctx -> ctx.getSender().hasPermission("skymarket.commands.skymarket.time"))
            .executes(ctx -> {
                Locale locale = localeLoader.getLocale();
                Time time = FormatUtil.millisToTime(marketManager.getResetTime() - System.currentTimeMillis());

                StringBuilder stringBuilder = new StringBuilder();

                if(time.years() > 0) {
                    if(time.years() > 1) {
                        stringBuilder.append(time.years()).append(" years ");
                    } else {
                        stringBuilder.append(time.years()).append(" year ");
                    }
                }

                if(time.months() > 0) {
                    if(time.months() > 1) {
                        stringBuilder.append(time.months()).append(" months ");
                    } else {
                        stringBuilder.append(time.months()).append(" month ");
                    }
                }

                if(time.weeks() > 0) {
                    if(time.weeks() > 1) {
                        stringBuilder.append(time.weeks()).append(" weeks ");
                    } else {
                        stringBuilder.append(time.weeks()).append(" week ");
                    }
                }

                if(time.days() > 0) {
                    if(time.days() > 1) {
                        stringBuilder.append(time.days()).append(" days ");
                    } else {
                        stringBuilder.append(time.days()).append(" day ");
                    }
                }

                if(time.hours() > 0) {
                    if(time.hours() > 1) {
                        stringBuilder.append(time.hours()).append(" hours ");
                    } else {
                        stringBuilder.append(time.hours()).append(" hour ");
                    }
                }

                if(time.minutes() > 0) {
                    if(time.minutes() > 1) {
                        stringBuilder.append(time.minutes()).append(" minutes ");
                    } else {
                        stringBuilder.append(time.minutes()).append(" minute ");
                    }
                }

                if(time.seconds() > 0) {
                    if(time.seconds() > 1) {
                        stringBuilder.append(time.seconds()).append(" seconds ");
                    } else {
                        stringBuilder.append(time.seconds()).append(" second ");
                    }
                }

                if(time.milliseconds() > 0) {
                    if(time.milliseconds() > 1) {
                        stringBuilder.append(time.milliseconds()).append(" milliseconds");
                    } else {
                        stringBuilder.append(time.milliseconds()).append(" millisecond");
                    }
                }

                List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("time", stringBuilder.toString()));

                ctx.getSource().getSender().sendMessage(FormatUtil.format(locale.prefix() + locale.marketRefreshTime(), placeholders));

                return 1;
            })
        );



        return builder.build();
    }
}
