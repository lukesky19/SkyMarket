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
package com.github.lukesky19.skymarket.data.config;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

/**
 * This record contains the plugin's messages.
 * @param configVersion The config version of the file.
 * @param prefix The plugin's prefix.
 * @param configReload The message sent when the plugin is reloaded.
 * @param notEnoughItems The message sent when the player doesn't have enough items to sell.
 * @param insufficientFunds The message sent when the player lacks the funds to purchase an item.
 * @param insufficientItems The message sent when the player lacks the items to purchase an item.
 * @param buySuccess The message sent when a purchase succeeds.
 * @param sellSuccess The message sent when a sale succeeds.
 * @param unbuyable The message sent when something cannot be purchased.
 * @param unsellable The message sent when something cannot be sold.
 * @param buyLimitReached The message sent when something cannot be purchased due to a limit.
 * @param sellLimitReached The message sent when something cannot be sold due to a limit.
 * @param marketRefreshed The message sent when a market is refreshed.
 * @param marketRefreshTime The message sent to display when a market will refresh.
 * @param invalidMarketId The message sent when a market doesn't exist for a specific market id.
 * @param guiOpenError The message sent to the player when a gui fails to open.
 * @param itemFormat The format used to display an item with.
 */
@ConfigSerializable
public record Locale(
        String configVersion,
        String prefix,
        String configReload,
        String notEnoughItems,
        String insufficientFunds,
        String insufficientItems,
        String buySuccess,
        String sellSuccess,
        String unbuyable,
        String unsellable,
        String buyLimitReached,
        String sellLimitReached,
        String marketRefreshed,
        String marketRefreshTime,
        String invalidMarketId,
        String guiOpenError,
        String itemFormat) {}
