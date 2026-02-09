package top.jaxlabs.farmweltplugin.utils;

import top.jaxlabs.farmweltplugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ScheduledReset extends BukkitRunnable {
    private final Main plugin;
    private final WorldUtils worldUtils;
    private LocalDateTime lastReset;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ScheduledReset(Main plugin, WorldUtils worldUtils) {
        this.plugin = plugin;
        this.worldUtils = worldUtils;
        this.lastReset = LocalDateTime.now(ZoneId.of(plugin.getConfig().getString("reset-schedule.timezone", "Europe/Berlin")));
    }

    @Override
    public void run() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of(plugin.getConfig().getString("reset-schedule.timezone", "Europe/Berlin")));
        
        if (shouldReset(now)) {
            // lastReset direkt setzen, damit kein mehrfacher Reset im Loop passiert
            lastReset = now;
            Bukkit.broadcastMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("reset-announcement"));
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                resetAllFarmworlds();
            }, 6000L);
        }
    }
    
    private void resetAllFarmworlds() {
        // Reset normale Farmwelt
        if (worldUtils.worldExists(plugin.getWorldName())) {
            Bukkit.getWorld(plugin.getWorldName()).getPlayers().forEach(player -> 
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation())
            );
            
            worldUtils.resetWorld(plugin.getWorldName());
        }
        
        // Reset Nether Farmwelt wenn aktiviert
        if (plugin.getConfig().getBoolean("menu.nether-world.enabled", false) && 
                worldUtils.worldExists(plugin.getNetherWorldName())) {
            Bukkit.getWorld(plugin.getNetherWorldName()).getPlayers().forEach(player -> 
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation())
            );
            
            worldUtils.resetWorld(plugin.getNetherWorldName());
        }
        
        // Reset End Farmwelt wenn aktiviert
        if (plugin.getConfig().getBoolean("menu.end-world.enabled", false) && 
                worldUtils.worldExists(plugin.getEndWorldName())) {
            Bukkit.getWorld(plugin.getEndWorldName()).getPlayers().forEach(player -> 
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation())
            );
            
            worldUtils.resetWorld(plugin.getEndWorldName());
        }
        
    Bukkit.broadcastMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("reset-complete"));
    // lastReset wird jetzt im run() gesetzt, nicht mehr hier
    }

    private boolean shouldReset(LocalDateTime now) {
        // Prüfe ob schon heute zurückgesetzt wurde
        if (now.toLocalDate().equals(lastReset.toLocalDate())) {
            return false;
        }

        String resetTimeStr = plugin.getConfig().getString("reset-schedule.time", "00:00");
        int resetHour = 0;
        int resetMinute = 0;
        try {
            String[] parts = resetTimeStr.split(":");
            resetHour = Integer.parseInt(parts[0]);
            resetMinute = Integer.parseInt(parts[1]);
        } catch (Exception e) {
            // Fallback auf 00:00 falls Fehler
            resetHour = 0;
            resetMinute = 0;
        }

        // Prüfe ob aktuelle Zeit im richtigen Zeitfenster liegt (±1 Minute Toleranz)
        int currentMinutes = now.getHour() * 60 + now.getMinute();
        int resetMinutes = resetHour * 60 + resetMinute;
        boolean isInTimeWindow = Math.abs(currentMinutes - resetMinutes) <= 1;

        if (plugin.getConfig().getBoolean("reset-schedule.daily")) {
            return isInTimeWindow;
        } else if (plugin.getConfig().getBoolean("reset-schedule.weekly")) {
            // getDayOfWeek().getValue(): 1=Montag, 7=Sonntag
            // Konfiguration: 0=Sonntag, 1=Montag, ..., 5=Freitag, 6=Samstag
            int javaDayOfWeek = now.getDayOfWeek().getValue();
            int configDayOfWeek = plugin.getConfig().getInt("reset-schedule.day-of-week");
            
            // Konvertiere Konfigurationstag (0=Sonntag) zu Java-Tag (7=Sonntag)
            if (configDayOfWeek == 0) {
                configDayOfWeek = 7; // Sonntag
            }
            
            return javaDayOfWeek == configDayOfWeek && isInTimeWindow;
        } else if (plugin.getConfig().getBoolean("reset-schedule.monthly")) {
            return now.getDayOfMonth() == plugin.getConfig().getInt("reset-schedule.day-of-month") && isInTimeWindow;
        }
        return false;
    }
}