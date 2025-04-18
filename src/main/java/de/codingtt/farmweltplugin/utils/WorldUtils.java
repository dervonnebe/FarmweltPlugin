package de.codingtt.farmweltplugin.utils;

import com.onarandombox.MultiverseCore.MultiverseCore;
import de.codingtt.farmweltplugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

public class WorldUtils {
    private final Main plugin;
    private final MultiverseCore core;
    private final Random random = new Random();

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
            // Prüfe, ob rotierende Seeds aktiviert sind
            boolean useRotatingSeeds = plugin.getConfig().getBoolean("rotating-seeds.enabled", false);
            Long seed = null;
            
            if (useRotatingSeeds) {
                // Bestimme welche Seed-Liste verwendet werden soll
                List<Long> seedList;
                if (environment == World.Environment.NORMAL) {
                    seedList = plugin.getConfig().getLongList("rotating-seeds.normal-world-seeds");
                } else if (environment == World.Environment.NETHER) {
                    seedList = plugin.getConfig().getLongList("rotating-seeds.nether-world-seeds");
                } else { // THE_END
                    seedList = plugin.getConfig().getLongList("rotating-seeds.end-world-seeds");
                }
                
                // Wenn Seeds vorhanden sind, wähle einen zufälligen aus
                if (seedList != null && !seedList.isEmpty()) {
                    seed = seedList.get(random.nextInt(seedList.size()));
                    plugin.getLogger().info("Using rotating seed: " + seed + " for world: " + worldName);
                }
            }

            // Erstelle die Welt mit dem Seed, falls angegeben
            boolean success;
            if (seed != null) {
                WorldCreator worldCreator = new WorldCreator(worldName)
                    .environment(environment)
                    .type(environment == World.Environment.NORMAL ? WorldType.NORMAL : 
                          environment == World.Environment.NETHER ? WorldType.NORMAL : 
                          WorldType.NORMAL)
                    .generateStructures(true)
                    .seed(seed);
                
                World world = worldCreator.createWorld();
                success = world != null;
                
                if (success) {
                    // Registriere die Welt bei Multiverse
                    core.getMVWorldManager().addWorld(
                        worldName,
                        environment,
                        null,
                        environment == World.Environment.NORMAL ? WorldType.NORMAL : 
                        environment == World.Environment.NETHER ? WorldType.NORMAL : 
                        WorldType.NORMAL,
                        true,
                        null,
                        true
                    );
                }
            } else {
                // Standard Multiverse-Erstellung ohne Seed
                success = core.getMVWorldManager().addWorld(
                    worldName,
                    environment,
                    null, // Generator
                    environment == World.Environment.NORMAL ? WorldType.NORMAL : 
                    environment == World.Environment.NETHER ? WorldType.NORMAL : 
                    WorldType.NORMAL,
                    true, // generateStructures
                    null  // Generator Settings
                );
            }

            if (success) {
                plugin.getLogger().info("World created successfully: " + worldName + " (" + environment + ")");
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
            // Bestimme die Umgebung vor dem Löschen
            World.Environment environment = World.Environment.NORMAL;
            if (core.getMVWorldManager().isMVWorld(worldName)) {
                environment = core.getMVWorldManager().getMVWorld(worldName).getEnvironment();
            }
            
            unloadWorld(worldName);
            core.getMVWorldManager().deleteWorld(worldName, true);
            createWorld(worldName, environment);
            plugin.getLogger().info("World reset completed: " + worldName);
            plugin.updateLastResetTime(worldName);
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
            
            // Für Nether und End weniger maximale Distanz verwenden
            World.Environment environment = world.getEnvironment();
            if (environment == World.Environment.NETHER) {
                maxDistance = Math.min(maxDistance, 500); // Nether ist kleiner
            }
            
            int x = random.nextInt(maxDistance * 2) - maxDistance;
            int z = random.nextInt(maxDistance * 2) - maxDistance;
            int y;
            
            if (environment == World.Environment.NETHER) {
                y = getSafeYInNether(world, x, z);
            } else if (environment == World.Environment.THE_END) {
                y = getSafeYInEnd(world, x, z);
            } else {
                y = world.getHighestBlockYAt(x, z);
            }

            Location randomLocation = new Location(world, x, y + 1, z);
            player.teleport(randomLocation);
            plugin.getLogger().info("Teleported player " + player.getName() + " to " + worldName);
        }
    }
    
    private int getSafeYInNether(World world, int x, int z) {
        // Suche einen sicheren Ort im Nether, nicht in der Lava
        for (int y = 31; y < 100; y++) {
            if (world.getBlockAt(x, y, z).isEmpty() && 
                world.getBlockAt(x, y + 1, z).isEmpty() && 
                !world.getBlockAt(x, y - 1, z).isEmpty()) {
                return y;
            }
        }
        return 64; // Fallback
    }
    
    private int getSafeYInEnd(World world, int x, int z) {
        // Im End ist es einfacher einen sicheren Ort zu finden
        for (int y = 50; y < 100; y++) {
            if (world.getBlockAt(x, y, z).isEmpty() && 
                world.getBlockAt(x, y + 1, z).isEmpty() && 
                !world.getBlockAt(x, y - 1, z).isEmpty()) {
                return y;
            }
        }
        return 64; // Fallback
    }
}
