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
package com.github.lukesky19.skymarket.locale;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.config.SimpleConfigManager;
import com.github.lukesky19.skylib.api.time.Time;
import com.github.lukesky19.skylib.api.time.TimeUtil;
import com.github.lukesky19.skymarket.SkyMarket;
import com.github.lukesky19.skymarket.settings.SettingsManager;

import java.io.File;
import java.nio.file.Path;

import com.github.lukesky19.skymarket.settings.Settings;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * This class handles the management of the locale configuration.
 */
public class LocaleManager extends SimpleConfigManager<Locale> {
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull Locale DEFAULT_LOCALE = new Locale(
            3,
            "<gold><bold>SkyMarket</bold></gold><gray> ▪ </gray>",
            "<aqua>Configuration files have been reloaded.</aqua>",
            "<red>You do not have enough items to sell.</red>",
            "<red>Insufficient funds.</red>",
            "<red>You do not have enough items to trade.</red>",
            "<white>Purchased <yellow><item></yellow> for <yellow><price></yellow>. Balance: <yellow><bal></yellow></white>",
            "<white>Sold <yellow><item></yellow> for <yellow><price></yellow>. Balance: <yellow><bal></yellow></white>",
            "<red>This item is not able to be purchased.</red>",
            "<red>This item is not able to be sold.</red>",
            "<red>You have reached the purchase limit of this item.</red>",
            "<red>You have reached the sell limit of this item.</red>",
            "<white>The <yellow><market_name></yellow> has been refreshed.</white>",
            "<white>The market will be refreshed in <yellow><time></yellow>.</white>",
            "<red>There is no market with this id.</red>",
            "<yellow><item_name> <white>x</white><item_amount></yellow>",
            "<red>Unable to open this GUI because of a configuration error.</red>");

    /**
     * Constructor
     * @param skyMarket A {@link SkyMarket} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     */
    public LocaleManager(@NotNull SkyMarket skyMarket, @NotNull SettingsManager settingsManager) {
        super(skyMarket, Locale.class);
        this.settingsManager = settingsManager;
    }

    /**
     * Gets the plugin's {@link Locale}.
     * Will return a default copy of the configuration if the user-configured one failed to load.
     * @return The plugin's {@link Locale}
     */
    public @NotNull Locale getLocale() {
        if(configuration == null) return DEFAULT_LOCALE;
        return configuration;
    }

    @Override
    public void loadConfiguration() {
        Settings settings = settingsManager.getConfiguration();
        if(settings == null) {
            logger.error(AdventureUtil.deserialize("<red>Failed to load plugin's locale due to plugin settings being null.</red>"));
            return;
        }
        if(settings.locale() == null) {
            logger.error(AdventureUtil.deserialize("<red>Failed to load plugin's locale to use in settings.yml is null.</red>"));
            return;
        }

        String localeString = settings.locale();
        Path path = Path.of(plugin.getDataFolder() + File.separator + "locale" + File.separator + (localeString + ".yml"));
        setConfigurationPath(path);

        super.loadConfiguration();
    }

    @Override
    public void saveBundledConfig() {
        Path path = Path.of(plugin.getDataFolder() + File.separator + "locale" + File.separator + "en_US.yml");
        if(!path.toFile().exists()) {
            plugin.saveResource("locale" + File.separator + "en_US.yml", false);
        }
    }

    /**
     * Migrate the locale.
     * @param locale The {@link Locale} to migrate.
     * @return The migrated {@link Locale} or null if migration failed.
     */
    @Override
    public @Nullable Locale migrateConfiguration(@NonNull Locale locale) {
        if(locale.version() == 0) {
            return new Locale(
                    3,
                    locale.prefix(),
                    locale.configReload(),
                    locale.notEnoughItems(),
                    locale.insufficientFunds(),
                    locale.insufficientItems(),
                    updatePlaceholders(locale.buySuccess()),
                    updatePlaceholders(locale.sellSuccess()),
                    locale.unbuyable(),
                    locale.unsellable(),
                    locale.buyLimitReached(),
                    locale.sellLimitReached(),
                    locale.marketRefreshed(),
                    locale.marketRefreshTime(),
                    locale.invalidMarketId(),
                    locale.guiOpenError(),
                    locale.itemFormat());
        }

        return locale;
    }

    @Override
    public boolean validateConfiguration(@Nullable Locale configuration) {
        if(configuration == null) return false;

        if(configuration.prefix() == null
                || configuration.configReload() == null
                || configuration.notEnoughItems() == null
                || configuration.insufficientFunds() == null
                || configuration.insufficientItems() == null
                || configuration.buySuccess() == null
                || configuration.sellSuccess() == null
                || configuration.unbuyable() == null
                || configuration.unsellable() == null
                || configuration.buyLimitReached() == null
                || configuration.sellLimitReached() == null
                || configuration.marketRefreshed() == null
                || configuration.marketRefreshTime() == null
                || configuration.invalidMarketId() == null
                || configuration.guiOpenError() == null
                || configuration.itemFormat() == null) {
            logger.error(AdventureUtil.deserialize("Your locale is missing one of the plugin's messages. The default locale will be used."));
            logger.info(AdventureUtil.deserialize("You can regenerate your locale file by deleting it or adding the missing messages to resolve the issue."));
            return false;
        }

        return true;
    }

    private @NonNull String updatePlaceholders(@NonNull String message) {
        message = message.replace("<item>", "<transaction_name>");

        return message;
    }

    /**
     * Convert the milliseconds to a String to use in time placeholders.
     * @param milliseconds The milliseconds.
     * @return The time formatted as a String.
     */
    public @NonNull String getTimeText(long milliseconds) {
        StringBuilder stringBuilder = new StringBuilder();
        Time time = TimeUtil.millisToTime(milliseconds);

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

        if(stringBuilder.isEmpty()) {
            stringBuilder.append("0 seconds");
        }

        return stringBuilder.toString();
    }
}