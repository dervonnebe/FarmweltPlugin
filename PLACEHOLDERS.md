# FarmweltPlugin - Placeholders

Das FarmweltPlugin bietet verschiedene Placeholders, die mit PlaceholderAPI verwendet werden können, um Informationen über die Farmwelten in Scoreboards, Chat und anderen Plugins anzuzeigen.

## Voraussetzungen

- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) muss installiert sein

## Verfügbare Placeholders

| Placeholder | Beschreibung |
|-------------|--------------|
| `%farmwelt_world%` | Zeigt an, in welcher Welt sich der Spieler befindet |
| `%farmwelt_normal_next_reset%` | Zeigt an, wann der nächste Reset der normalen Farmwelt ist |
| `%farmwelt_nether_next_reset%` | Zeigt an, wann der nächste Reset der Nether-Farmwelt ist |
| `%farmwelt_end_next_reset%` | Zeigt an, wann der nächste Reset der End-Farmwelt ist |
| `%farmwelt_normal_timer%` | Zeigt einen Timer an, wenn der Reset in der nächsten Stunde ist |
| `%farmwelt_nether_timer%` | Zeigt einen Timer an, wenn der Reset in der nächsten Stunde ist |
| `%farmwelt_end_timer%` | Zeigt einen Timer an, wenn der Reset in der nächsten Stunde ist |
| `%farmwelt_normal_players%` | Zeigt an, wie viele Spieler in der normalen Farmwelt sind |
| `%farmwelt_nether_players%` | Zeigt an, wie viele Spieler in der Nether-Farmwelt sind |
| `%farmwelt_end_players%` | Zeigt an, wie viele Spieler in der End-Farmwelt sind |

## Beispiele

### Scoreboard-Beispiel (mit [Featherboard](https://www.spigotmc.org/resources/featherboard.2691/))

```yaml
scoreboard:
  title: "&6&lFarmwelt"
  lines:
    - "&7Welt: &e%farmwelt_world%"
    - "&7"
    - "&7Nächster Reset: &e%farmwelt_normal_next_reset%"
    - "&c%farmwelt_normal_timer%"
    - "&7"
    - "&7Spieler in Farmwelt: &e%farmwelt_normal_players%"
```

### Verwendung mit anderen Plugins

Diese Placeholders können mit vielen anderen Plugins verwendet werden, die PlaceholderAPI unterstützen, wie z.B.:

- [Featherboard](https://www.spigotmc.org/resources/featherboard.2691/)
- [TAB](https://www.spigotmc.org/resources/tab-1-5-1-21-5.57806/)
- [DeluxeMenus](https://www.spigotmc.org/resources/deluxemenus.11734/)
- Und viele weitere Plugins die PlaceHolderAPI benutzen

## Anpassungen

Die Anzeigenamen der Welten können in den Sprachdateien (`plugins/FarmweltPlugin/lang/de.yml` oder `en.yml`) angepasst werden:

```yaml
farmwelt-name: "Farmwelt"
nether-farmwelt-name: "Nether-Farmwelt"
end-farmwelt-name: "End-Farmwelt"
reset-not-scheduled: "Nicht geplant"
``` 