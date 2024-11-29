# FarmweltPlugin - Technische Dokumentation

## Systemarchitektur

### Hauptkomponenten
1. **Main.java**
   - Plugin-Einstiegspunkt
   - Initialisiert bStats (ID: 24022)
   - Verwaltet Plugin-Instanz
   - Verzögerter Weltstart (40 Ticks)

2. **WorldUtils.java**
   - Multiverse-Core Integration
   - Welt-Management (Erstellen, Löschen, Reset)
   - Sicheres Teleport-System mit Zufallskoordinaten

3. **ScheduledReset.java**
   - Automatisches Reset-System
   - Zeitzonenbasierte Planung
   - 5-Minuten Warnsystem
   - Spieler-Sicherheitsteleport

### Abhängigkeiten
- Spigot API 1.16.5+
- Multiverse-Core 4.3.12
- bStats 3.0.2

## Konfiguration
```yaml
reset-schedule:
  timezone: "Europe/Berlin"  # Zeitzone für Resets
  daily: true               # Täglicher Reset
  weekly: false            # Wöchentlicher Reset
  monthly: false           # Monatlicher Reset
  day-of-week: 1          # 1-7 (Mo-So)
  day-of-month: 1         # 1-31
```

## Berechtigungssystem
- `farmwelt.use`: Basis-Zugriff
- `farmwelt.admin`: Administrative Funktionen

## Wichtige Methoden
- `WorldUtils.resetWorld()`: Sicherer Welt-Reset
- `WorldUtils.teleportToWorld()`: Sicheres Teleport-System
- `ScheduledReset.shouldReset()`: Reset-Zeitprüfung

## Bekannte Einschränkungen
- Reset nur zur vollen Stunde (00:00)
- Multiverse-Core erforderlich
- Keine gleichzeitigen Resets

## Performance
- Verzögerter Weltstart
- Asynchrone Spieler-Teleports
- Optimierte Reset-Prüfungen 