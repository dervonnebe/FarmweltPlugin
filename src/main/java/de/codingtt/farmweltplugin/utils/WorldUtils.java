package de.codingtt.farmweltplugin.utils;

import com.onarandombox.MultiverseCore.MultiverseCore;
import de.codingtt.farmweltplugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

import java.util.Random;

public class WorldUtils {
    private final Main plugin;
    private final MultiverseCore core;

    public WorldUtils(Main plugin) {
        this.plugin = plugin;
        this.core = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
    }

    public void createWorld(String worldName, World.Environment environment) {
        if (worldExists(worldName)) {
            plugin.getLogger().info("World already exists");
            return;
        }

        try {
            boolean success = core.getMVWorldManager().addWorld(
                worldName,
                environment,
                null, // Generator
                    WorldType.valueOf(World.Environment.NORMAL.toString()),
                false, // generateStructures
                null  // Generator Settings
            );

            if (success) {
                plugin.getLogger().info("World created successfully: " + worldName);
                loadWorld(worldName);
            } else {
                plugin.getLogger().warning("Failed to create world: " + worldName);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error creating world: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteWorld(String worldName) {
        if (worldExists(worldName)) {
            unloadWorld(worldName);
            core.getMVWorldManager().deleteWorld(worldName);
            plugin.getLogger().info("World deleted: " + worldName);
        } else {
            plugin.getLogger().warning("World does not exist: " + worldName);
        }
    }

    public void resetWorld(String worldName) {
        if (!worldExists(worldName)) {
            plugin.getLogger().warning("Cannot reset - World does not exist: " + worldName);
            return;
        }

        plugin.getLogger().info("Starting world reset: " + worldName);
        
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            World defaultWorld = Bukkit.getWorlds().get(0);
            world.getPlayers().forEach(player -> {
                player.teleport(defaultWorld.getSpawnLocation());
                player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("reset-warning"));
            });
        }

        try {
            unloadWorld(worldName);
            core.getMVWorldManager().deleteWorld(worldName, true);
            createWorld(worldName, World.Environment.NORMAL);
            plugin.getLogger().info("World reset completed: " + worldName);
        } catch (Exception e) {
            plugin.getLogger().severe("Error during world reset: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadWorld(String worldName) {
        core.getMVWorldManager().loadWorld(worldName);
    }

    public void unloadWorld(String worldName) {
        if (!worldExists(worldName)) {
            return;
        }

        try {
            core.getMVWorldManager().unloadWorld(worldName, true);
            plugin.getLogger().info("World unloaded: " + worldName);
        } catch (Exception e) {
            plugin.getLogger().severe("Error unloading world: " + e.getMessage());
        }
    }

    public boolean worldExists(String worldName) {
        return core.getMVWorldManager().isMVWorld(worldName);
    }

    public void teleportToWorld(Player player, String worldName) {
        if (worldExists(worldName)) {
            World world = core.getMVWorldManager().getMVWorld(worldName).getSpawnLocation().getWorld();
            Random random = new Random();
            int maxDistance = plugin.getConfig().getInt("random-teleport-distance", 1000);
            int x = random.nextInt(maxDistance * 2) - maxDistance;
            int z = random.nextInt(maxDistance * 2) - maxDistance;
            int y = world.getHighestBlockYAt(x, z);

            Location randomLocation = new Location(world, x, y + 1, z);
            player.teleport(randomLocation);
            plugin.getLogger().info("Teleported player " + player.getName() + " to " + worldName);
        }
    }
}
