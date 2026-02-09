package de.codingtt.farmweltplugin.services;

import de.codingtt.farmweltplugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class ChunkyIntegrationService {
    private final Main plugin;
    private final boolean useChunky;

    public ChunkyIntegrationService(Main plugin, boolean useChunky) {
        this.plugin = plugin;
        this.useChunky = useChunky;
    }

    public void startChunkyGeneration(World world) {
        if (!useChunky) {
            return;
        }

        String worldName = world.getName();
        ChunkyConfig config = getChunkyConfig(worldName);

        if (config.enabled) {
            plugin.getLogger().info("Starting Chunky generation for " + worldName + " with radius " + config.radius);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky world " + worldName);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky center 0 0");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky radius " + (int)config.radius);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky start");
        }
    }

    private ChunkyConfig getChunkyConfig(String worldName) {
        boolean enabled;
        double radius;

        if (worldName.equals(plugin.getNetherWorldName())) {
            enabled = plugin.getConfig().getBoolean("nether-world-settings.chunky.enabled", false);
            radius = plugin.getConfig().getDouble("nether-world-settings.chunky.radius", 2500);
        } else if (worldName.equals(plugin.getEndWorldName())) {
            enabled = plugin.getConfig().getBoolean("end-world-settings.chunky.enabled", false);
            radius = plugin.getConfig().getDouble("end-world-settings.chunky.radius", 2500);
        } else {
            enabled = plugin.getConfig().getBoolean("farmwelt-settings.chunky.enabled", false);
            radius = plugin.getConfig().getDouble("farmwelt-settings.chunky.radius", 2500);
        }

        return new ChunkyConfig(enabled, radius);
    }

    private static class ChunkyConfig {
        final boolean enabled;
        final double radius;

        ChunkyConfig(boolean enabled, double radius) {
            this.enabled = enabled;
            this.radius = radius;
        }
    }
}
