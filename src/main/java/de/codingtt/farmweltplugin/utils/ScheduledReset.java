package de.codingtt.farmweltplugin.utils;

import de.codingtt.farmweltplugin.Main;
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
        
        // Prüfe ob ein Reset nötig ist
        if (shouldReset(now)) {
            // Kündige Reset 5 Minuten vorher an
            Bukkit.broadcastMessage(plugin.getColoredString("prefix") + "§cDie Farmwelt wird in 5 Minuten zurückgesetzt!");
            
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (worldUtils.worldExists(plugin.getWorldName())) {
                    // Teleportiere alle Spieler aus der Farmwelt
                    Bukkit.getWorld(plugin.getWorldName()).getPlayers().forEach(player -> 
                        player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation())
                    );
                    
                    worldUtils.resetWorld(plugin.getWorldName());
                    Bukkit.broadcastMessage(plugin.getColoredString("prefix") + "§aDie Farmwelt wurde zurückgesetzt!");
                    lastReset = now;
                }
            }, 6000L); // 5 Minuten = 6000 Ticks
        }
    }

    private boolean shouldReset(LocalDateTime now) {
        // Verhindere mehrfache Resets am selben Tag
        if (now.toLocalDate().equals(lastReset.toLocalDate())) {
            return false;
        }

        // Prüfe die Reset-Bedingungen
        if (plugin.getConfig().getBoolean("reset-schedule.daily")) {
            return now.getHour() == 0 && now.getMinute() == 0;
        } else if (plugin.getConfig().getBoolean("reset-schedule.weekly") 
                && now.getDayOfWeek().getValue() == plugin.getConfig().getInt("reset-schedule.day-of-week")) {
            return now.getHour() == 0 && now.getMinute() == 0;
        } else if (plugin.getConfig().getBoolean("reset-schedule.monthly") 
                && now.getDayOfMonth() == plugin.getConfig().getInt("reset-schedule.day-of-month")) {
            return now.getHour() == 0 && now.getMinute() == 0;
        }
        
        return false;
    }
}