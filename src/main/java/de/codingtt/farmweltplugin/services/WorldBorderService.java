package de.codingtt.farmweltplugin.services;

import de.codingtt.farmweltplugin.Main;
import org.bukkit.World;
import org.bukkit.WorldBorder;

public class WorldBorderService {
    private final Main plugin;

    public WorldBorderService(Main plugin) {
        this.plugin = plugin;
    }

    public void setupWorldBorder(World world) {
        String worldName = world.getName();
        double size = getBorderSize(worldName);

        WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(size);
        
        plugin.getLogger().info("WorldBorder for " + worldName + " set to " + size);
    }

    private double getBorderSize(String worldName) {
        if (worldName.equals(plugin.getNetherWorldName())) {
            return plugin.getConfig().getDouble("nether-world-settings.world-border.size", 5000);
        } else if (worldName.equals(plugin.getEndWorldName())) {
            return plugin.getConfig().getDouble("end-world-settings.world-border.size", 5000);
        }
        return plugin.getConfig().getDouble("farmwelt-settings.world-border.size", 5000);
    }
}
