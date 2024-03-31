package de.codingtt.farmweltplugin.utils;

import de.codingtt.farmweltplugin.Main;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Calendar;
import java.util.TimeZone;

public class ScheduldReset extends BukkitRunnable {
    private final Main plugin;
    private final WorldUtils worldUtils;

    public ScheduldReset(Main plugin, WorldUtils worldUtils) {
        this.plugin = plugin;
        this.worldUtils = worldUtils;
    }

    @Override
    public void run() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(plugin.getConfig().getString("reset-schedule.timezone")));
        boolean shouldReset = true;

        if (plugin.getConfig().getBoolean("reset-schedule.daily")) {
            shouldReset = true;
        } else if (plugin.getConfig().getBoolean("reset-schedule.weekly") && calendar.get(Calendar.DAY_OF_WEEK) == plugin.getConfig().getInt("reset-schedule.day-of-week")) {
            shouldReset = true;
        } else if (plugin.getConfig().getBoolean("reset-schedule.monthly") && calendar.get(Calendar.DAY_OF_MONTH) == plugin.getConfig().getInt("reset-schedule.day-of-month")) {
            shouldReset = true;
        }

        if (shouldReset && worldUtils.wolrdExists(plugin.getWorldName())) {
            worldUtils.resetWorld(plugin.getWorldName());
            plugin.getLogger().info("Farmworld World reset done!");
        }
    }
}