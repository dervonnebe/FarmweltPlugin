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

        if (plugin.getConfig().getBoolean("reset-schedule.daily")) {
            return now.getHour() == resetHour && now.getMinute() == resetMinute;
        } else if (plugin.getConfig().getBoolean("reset-schedule.weekly") 
                && now.getDayOfWeek().getValue() == plugin.getConfig().getInt("reset-schedule.day-of-week")) {
            return now.getHour() == resetHour && now.getMinute() == resetMinute;
        } else if (plugin.getConfig().getBoolean("reset-schedule.monthly") 
                && now.getDayOfMonth() == plugin.getConfig().getInt("reset-schedule.day-of-month")) {
            return now.getHour() == resetHour && now.getMinute() == resetMinute;
        }
        return false;
    }
}