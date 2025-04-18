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
        lastReset = LocalDateTime.now(ZoneId.of(plugin.getConfig().getString("reset-schedule.timezone", "Europe/Berlin")));
    }

    private boolean shouldReset(LocalDateTime now) {
        if (now.toLocalDate().equals(lastReset.toLocalDate())) {
            return false;
        }

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