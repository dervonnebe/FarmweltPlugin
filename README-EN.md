# FarmweltPlugin
The perfect solution for automatically resetting farm worlds in Minecraft

[![German Version](https://img.shields.io/badge/German-Version-red.svg)](https://www.spigotmc.org/resources/farmworld-plugin.115930/)

## ⚡ Features
✨ Separate farm worlds (Normal, Nether, and End)  
⏰ Configurable automatic resets (daily/weekly/monthly)  
🎲 Random teleportation in all farm worlds  
🌱 Rotating world seeds for diverse biomes  
🌍 GUI menu system for world selection  
⚠️ 5-minute warning before reset  
🔄 Safe player teleport system  
🌐 Multi-language support (German/English)  
📊 bStats integration (optional)  
💻 Multiverse-Core integration (optional)  

## Commands
• `/farmwelt` - Teleports to normal farm world  
• `/nether` - Teleports to Nether farm world  
• `/end` - Teleports to End farm world  
• `/farmwelt reset` - Resets the farm world (admin)  
• `/farmwelt reload` - Reloads the configuration (admin)  

## Permissions
• `farmwelt.use` - Allows use of the /farmwelt command  
• `farmwelt.admin` - Access to admin commands  

## ⚙️ Configuration
```yaml
prefix: "&8[&6&lFarmwelt&8]&r "
farmwelt-world: "farmwelt"
random-teleport-distance: 1000
language: "en" # de or en

# Rotating seeds for farm worlds
rotating-seeds:
  enabled: false # Set to true to enable rotating seeds
  normal-world-seeds:
    - 1234567890
    - 9876543210
  nether-world-seeds:
    - 1122334455
    - 5544332211
  end-world-seeds:
    - 6677889900
    - 0099887766

reset-schedule:
  timezone: "Europe/Berlin"
  daily: true
  weekly: false
  monthly: false
  day-of-week: 1    # 1 (Monday) to 7 (Sunday)
  day-of-month: 1   # 1 to 31

use-bstats: true # Set to false to disable bStats
```

## Installation
1. Download the latest version from [here](https://github.com/dervonnebe/FarmweltPlugin/releases)
2. Copy the plugin to your plugins folder
3. For enhanced world management, install Multiverse-Core (recommended but not required)
4. For placeholder support, install PlaceholderAPI
5. Restart your server
6. Done!

## ⚠️ Requirements
• Spigot/Paper 1.16.5 or higher  
• Java 8 or higher  
• Optional: Multiverse-Core for enhanced world management  
• Optional: PlaceholderAPI for placeholders  

## Statistics
This plugin uses bStats for anonymous usage statistics.  
https://bstats.org/plugin/bukkit/FarmworldPlugin/24022

## Support
• Discord: dervonnebe
• https://github.com/dervonnebe/FarmweltPlugin/issues

## Changelog

### Version 1.1.3
• Multiverse-Core is now optional and no longer required
• The plugin can now operate without Multiverse-Core
• Improved compatibility with other plugins
• Minor bugfixes and improvements

### Version 1.1.0
• New farm worlds: Added Nether and End
• GUI menu system for world selection
• Multi-language support (German/English)
• Rotating world seeds system
• Improved teleport safety
• bStats can now be disabled
• Enhanced world reset system

### Version 1.0.0
• First stable version
• Automatic reset system
• Player safety system
• bStats integration
• Improved error handling

## ⭐ Ratings
If you like the plugin, please leave a positive rating!

## Updates
Regular updates and improvements planned.

Made with ❤️ by dervonnebe
