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
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.configuration.manager.LocaleLoader;
import com.github.lukesky19.skymarket.configuration.record.Locale;
import com.github.lukesky19.skymarket.manager.MarketManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.*;

public class SkyMarketCommand implements CommandExecutor, TabCompleter {
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

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Locale locale = localeLoader.getLocale();

        switch(args.length) {
            case 0 -> {
                if(sender instanceof Player player) {
                    if(sender.hasPermission("skymarket.commands.skymarket")) {
                        if(marketManager.getMarketGUI() != null) {
                            marketManager.getMarketGUI().openInventory(skyMarket, player);
                            return true;
                        } else {
                            player.sendMessage(FormatUtil.format(locale.prefix() + locale.marketOpenError()));
                            return false;
                        }
                    } else {
                        sender.sendMessage(FormatUtil.format(player, locale.prefix() + locale.noPermission()));
                        return false;
                    }
                } else {
                    skyMarket.getComponentLogger().info(locale.inGameOnly());
                    return false;
                }
            }

            case 1 -> {
                switch(args[0].toLowerCase()) {
                    case "reload" -> {
                        if (sender instanceof Player player) {
                            if (player.hasPermission("skymarket.commands.skymarket.reload")) {
                                skyMarket.reload();

                                locale = localeLoader.getLocale();

                                sender.sendMessage(FormatUtil.format(player, locale.prefix() + locale.configReload()));
                                return true;
                            } else {
                                sender.sendMessage(FormatUtil.format(player, locale.prefix() + locale.noPermission()));
                                return false;
                            }
                        } else {
                            skyMarket.reload();
                            skyMarket.getComponentLogger().info(FormatUtil.format(locale.configReload()));
                            return true;
                        }
                    }

                    case "refresh" -> {
                        if(sender instanceof Player player) {
                            if(player.hasPermission("skymarket.commands.skymarket.refresh")) {
                                marketManager.refreshMarket();

                                return true;
                            }
                        }
                    }

                    case "time" -> {
                        if(sender instanceof Player player) {
                            if(player.hasPermission("skymarket.commands.skymarket.time")) {
                                SimpleDateFormat simpleDateFormat = marketManager.getSimpleDateFormat();

                                Date date = new Date(marketManager.getResetTime());

                                List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("time", simpleDateFormat.format(date)));

                                player.sendMessage(FormatUtil.format(locale.prefix() + locale.marketRefreshTime(), placeholders));

                                return true;
                            } else {
                                sender.sendMessage(FormatUtil.format(player, locale.prefix() + locale.noPermission()));
                                return false;
                            }
                        }
                    }

                    default -> {
                        if (sender instanceof Player player) {
                            player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.unknownArgument(), new ArrayList<>()));
                        } else {
                            skyMarket.getComponentLogger().info(FormatUtil.format(locale.unknownArgument(), new ArrayList<>()));
                        }

                        return false;
                    }
                }
            }

            default -> {
                if(sender instanceof Player player) {
                    player.sendMessage(FormatUtil.format(player,locale.prefix() + locale.unknownArgument()));
                } else {
                    skyMarket.getComponentLogger().info(FormatUtil.format(locale.unknownArgument()));
                }
                return false;
            }
        }

        return false;
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 1) {
            ArrayList<String> subCmds = new ArrayList<>();
            if(sender instanceof Player) {
                if(sender.hasPermission("skymarket.commands.skymarket.reload")) subCmds.add("reload");
                if(sender.hasPermission("skymarket.commands.skymarket.time")) subCmds.add("time");
            } else {
                subCmds.add("reload");
                subCmds.add("time");
            }

            return subCmds;
        }

        return Collections.emptyList();
    }
}
