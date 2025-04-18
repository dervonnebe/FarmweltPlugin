# FarmweltPlugin - Placeholders

The FarmweltPlugin provides various placeholders that can be used with PlaceholderAPI to display information about farm worlds in scoreboards, chat, and other plugins.

## Requirements

- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) must be installed

## Available Placeholders

| Placeholder | Description |
|-------------|-------------|
| `%farmwelt_world%` | Shows which world the player is in |
| `%farmwelt_normal_next_reset%` | Shows when the next reset of the normal farm world will occur |
| `%farmwelt_nether_next_reset%` | Shows when the next reset of the Nether farm world will occur |
| `%farmwelt_end_next_reset%` | Shows when the next reset of the End farm world will occur |
| `%farmwelt_normal_timer%` | Shows a timer when the reset is within the next hour |
| `%farmwelt_nether_timer%` | Shows a timer when the reset is within the next hour |
| `%farmwelt_end_timer%` | Shows a timer when the reset is within the next hour |
| `%farmwelt_normal_players%` | Shows how many players are in the normal farm world |
| `%farmwelt_nether_players%` | Shows how many players are in the Nether farm world |
| `%farmwelt_end_players%` | Shows how many players are in the End farm world |

## Examples

### Scoreboard Example (with [Featherboard](https://www.spigotmc.org/resources/featherboard.2691/))

```yaml
scoreboard:
  title: "&6&lFarm World"
  lines:
    - "&7World: &e%farmwelt_world%"
    - "&7"
    - "&7Next Reset: &e%farmwelt_normal_next_reset%"
    - "&c%farmwelt_normal_timer%"
    - "&7"
    - "&7Players in Farm World: &e%farmwelt_normal_players%"
```

### Usage with Other Plugins

These placeholders can be used with many other plugins that support PlaceholderAPI, such as:

- [Featherboard](https://www.spigotmc.org/resources/featherboard.2691/)
- [TAB](https://www.spigotmc.org/resources/tab-1-5-1-21-5.57806/)
- [DeluxeMenus](https://www.spigotmc.org/resources/deluxemenus.11734/)
- And many other plugins that use PlaceholderAPI

## Customization

The display names of the worlds can be customized in the language files (`plugins/FarmweltPlugin/lang/de.yml` or `en.yml`):

```yaml
farmwelt-name: "Farm World"
nether-farmwelt-name: "Nether Farm World"
end-farmwelt-name: "End Farm World"
reset-not-scheduled: "Not scheduled"
```