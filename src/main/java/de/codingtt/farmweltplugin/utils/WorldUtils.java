package de.codingtt.farmweltplugin.utils;

import com.onarandombox.MultiverseCore.MultiverseCore;
import de.codingtt.farmweltplugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Random;

public class WorldUtils {
    public MultiverseCore core = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
    private final Main plugin = Main.getInstance();

    public void createWorld(String worldName, World.Environment environment) {
        if (worldExists(worldName)) {
            System.out.println("World already exists");
            return;
        }
        core.getMVWorldManager().addWorld(worldName, environment, null, null, null, null);
        loadWorld(worldName);
    }

    public void deleteWorld(String worldName) {
        if (worldExists(worldName)) {
            unloadWorld(worldName);
            core.getMVWorldManager().deleteWorld(worldName);
        } else {
            System.out.println("World does not exist");
        }
    }

    public void resetWorld(String worldName) {
        if (worldExists(worldName)) {
            plugin.getLogger().info("Starting reset of world: " + worldName);
            
            try {
                unloadWorld(worldName);
                core.getMVWorldManager().deleteWorld(worldName);
                createWorld(worldName, World.Environment.NORMAL);
                plugin.getLogger().info("World reset completed successfully: " + worldName);
            } catch (Exception e) {
                plugin.getLogger().severe("Error during world reset: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            plugin.getLogger().warning("Cannot reset world - World does not exist: " + worldName);
        }
    }

    public void loadWorld(String worldName) {
        core.getMVWorldManager().loadWorld(worldName);
    }

    public void unloadWorld(String worldName) {
        core.getMVWorldManager().unloadWorld(worldName);
    }

    public boolean worldExists(String worldName) {
        return core.getMVWorldManager().isMVWorld(worldName);
    }

    public void teleportToWorld(Player player, String worldName) {
        if (worldExists(worldName)) {
            World world = core.getMVWorldManager().getMVWorld(worldName).getSpawnLocation().getWorld();
            Location spawnLocation = world.getSpawnLocation();

            Random random = new Random();
            int maxDistance = Main.getInstance().getConfig().getInt("random-teleport-distance");
            int x = random.nextInt(maxDistance * 2) - maxDistance;
            int z = random.nextInt(maxDistance * 2) - maxDistance;
            int y = world.getHighestBlockYAt(x, z);

            Location randomLocation = new Location(world, x, y, z);
            player.teleport(randomLocation);
        }
    }
}
