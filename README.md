# SkyMarket
## Description
* A shop that rotates it's inventory after a set period of time.

## Features
* Supports buying and selling for items and commands.
* Optional server and player buy and sell limits.
* Optional refresh timers individual market entries.

## Required Dependencies
* Vault

## Commands
- /skymarket open <market_id> - Command to open a market.
- /skymarket reload - Reloads the plugin.
- /skymarket refresh <market_id> - Refreshes the market's inventory.
- /skymarket time <time> - View when the market will refresh next.

## Command Aliases (Configurable)
- /vm - Command to open the villager market.
- /villagers - Command to open the villager market.
- /villagermarket - Command to open the villager market.
- sm - Command to open the skymarket.
- skymarket - Command to open the skymarket.

## Permisisons
- `skymarket.commands.skymarket` - The permission to access the shop.
- `skymarket.commands.skymarket.reload` - The permission to reload the plugin.
- `skymarket.commands.skymarket.refresh` - The permission to refresh the shop.
- `skymarket.commands.skymarket.time` - The permission to view when the shop will refresh next.
- `skymarket.commands.skymarket.open` - The permission to open markets.

## Issues, Bugs, or Suggestions
* Please create a new [Github Issue](https://github.com/lukesky19/SkyMarket/issues) with your issue, bug, or suggestion.
* If an issue or bug, please post any relevant logs containing errors related to SkyMarket and your configuration files.
* I will attempt to solve any issues or implement features to the best of my ability.

## FAQ
Q: What versions does this plugin support?

A: 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1, 26.1.1, 26.1.2, and 26.2.
Note: Java 25 is required even on versions older than 26.1.

Q: I get the following error: "SkyMarket has been compiled by a more recent version of the Java Runtime
(class file version 69.0), this version of the Java Runtime only recognizes class file versions up to 65.0"

A: SkyMarket is compiled using Java 25 as part of it's support of 26.1 and beyond. SkyMarket still works on older version as long as Java 25 is used.

Q: Are there any plans to support any other versions?

A: I will always do my best to support the latest versions of the game. I will sometimes support other versions until I no longer use them.

Q: Does this work on Spigot? Paper? (Insert other server software here)?

A: I only support Paper, but this will likely also work on forks of Paper (untested). There are no plans to support any other server software (i.e., Spigot or Folia).

## Building
* Go to [SkyLib](https://github.com/lukesky19/SkyLib) and follow the "For Developers" instructions.
* Then run:
  ```./gradlew build```

## Why AGPL3?
I wanted a license that will keep my code open source. I believe in open source software and in-case this project goes unmaintained by me, I want it to live on through the work of others. And I want that work to remain open source to prevent a time when a fork can never be continued (i.e., closed-sourced and abandoned).
