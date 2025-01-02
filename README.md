# SkyMarket
## Description
* A shop that rotates it's inventory after a set period of time.
## Features
* Supports buying and selling for items and commands.
## Required Dependencies
* Vault
## Commands
- /skymarket - Command to open the shop.
  - Aliases
    - /market
    - /bm
    - /blackmarket
- /skymarket reload - Reloads the plugin.
- /skymarket refresh - Refreshes the market's inventory.
- /skymarket time - View when the shop will refresh next.
## Permisisons
- `skymarket.commands.skymarket` - The permission to access the shop.
- `skymarket.commands.skymarket.reload` - The permission to reload the plugin.
- `skymarket.commands.skymarket.refresh` - The permission to refresh the shop.
- `skymarket.commands.skymarket.time` - The permission to view when the shop will refresh next.
## Issues, Bugs, or Suggestions
* Please create a new [Github Issue](https://github.com/lukesky19/SkyMarket/issues) with your issue, bug, or suggestion.
* If an issue or bug, please post any relevant logs containing errors related to SkyShop and your configuration files.
* I will attempt to solve any issues or implement features to the best of my ability.
## FAQ
Q: What versions does this plugin support?

A: 1.21.0, 1.21.1, 1.21.2, 1.21.3, and 1.21.4.

Q: Are there any plans to support any other versions?

A: The plugin will always support the latest version of the game at the time.

Q: Does this work on Spigot and Paper?

A: This plugin only works with Paper, it makes use of many newer API features that don't exist in the Spigot API. There are no plans to support Spigot.

Q: Is Folia supported?

A: There is no Folia support at this time. I may look into it in the future though.

## Building
```./gradlew build```

## Why AGPL3?
I wanted a license that will keep my code open source. I believe in open source software and in-case this project goes unmaintained by me, I want it to live on through the work of others. And I want that work to remain open source to prevent a time when a fork can never be continued (i.e., closed-sourced and abandoned).
