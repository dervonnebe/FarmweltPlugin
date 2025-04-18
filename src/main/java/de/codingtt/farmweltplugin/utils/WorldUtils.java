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
            boolean useRotatingSeeds = plugin.getConfig().getBoolean("rotating-seeds.enabled", false);
            Long seed = null;
            
            if (useRotatingSeeds) {
                List<Long> seedList;
                if (environment == World.Environment.NORMAL) {
                    seedList = plugin.getConfig().getLongList("rotating-seeds.normal-world-seeds");
                } else if (environment == World.Environment.NETHER) {
                    seedList = plugin.getConfig().getLongList("rotating-seeds.nether-world-seeds");
                } else { // THE_END
                    seedList = plugin.getConfig().getLongList("rotating-seeds.end-world-seeds");
                }
                
                if (seedList != null && !seedList.isEmpty()) {
                    seed = seedList.get(random.nextInt(seedList.size()));
                    plugin.getLogger().info("Using rotating seed: " + seed + " for world: " + worldName);
                }
            }

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
            
            boolean useRandomTp = true;
            if (worldName.equals(plugin.getWorldName())) {
                useRandomTp = plugin.getConfig().getBoolean("farmwelt-settings.use-random-teleport", true);
            } else if (worldName.equals(plugin.getNetherWorldName())) {
                useRandomTp = plugin.getConfig().getBoolean("nether-world-settings.use-random-teleport", true);
            } else if (worldName.equals(plugin.getEndWorldName())) {
                useRandomTp = plugin.getConfig().getBoolean("end-world-settings.use-random-teleport", true);
            }
            
            if (!useRandomTp) {
                player.teleport(world.getSpawnLocation());
                plugin.getLogger().info("Teleported player " + player.getName() + " to " + worldName + " spawn (random TP disabled)");
                return;
            }
            
            Random random = new Random();
            
            // Individuelle Teleport-Distanz je nach Welt
            int maxDistance;
            if (worldName.equals(plugin.getWorldName())) {
                maxDistance = plugin.getConfig().getInt("farmwelt-settings.teleport-distance", 
                        plugin.getConfig().getInt("random-teleport-distance", 1000));
            } else if (worldName.equals(plugin.getNetherWorldName())) {
                maxDistance = plugin.getConfig().getInt("nether-world-settings.teleport-distance", 
                        plugin.getConfig().getInt("random-teleport-distance", 500));
            } else if (worldName.equals(plugin.getEndWorldName())) {
                maxDistance = plugin.getConfig().getInt("end-world-settings.teleport-distance", 
                        plugin.getConfig().getInt("random-teleport-distance", 1000));
            } else {
                maxDistance = plugin.getConfig().getInt("random-teleport-distance", 1000);
            }
            
            World.Environment environment = world.getEnvironment();
            if (environment == World.Environment.NETHER && worldName.equals(plugin.getNetherWorldName())) {
                // Für den Nether stellen wir sicher, dass die maximale Distanz nicht zu groß ist,
                // aber nur wenn keine spezifische Einstellung für den Nether gesetzt wurde
                if (!plugin.getConfig().contains("nether-world-settings.teleport-distance")) {
                    maxDistance = Math.min(maxDistance, 500);
                }
            }
            
            Location safeLocation = null;
            int attempts = 0;
            final int MAX_ATTEMPTS = 10;
            
            while (safeLocation == null && attempts < MAX_ATTEMPTS) {
                attempts++;
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
                Location possibleSafeLocation = findSafeLocation(randomLocation);
                
                if (possibleSafeLocation != null && isDryLocation(possibleSafeLocation)) {
                    safeLocation = possibleSafeLocation;
                }
            }
            
            if (safeLocation == null) {
                plugin.getLogger().warning("Could not find safe location after " + MAX_ATTEMPTS + " attempts, using world spawn");
                safeLocation = world.getSpawnLocation();
            }
            
            player.teleport(safeLocation);
            plugin.getLogger().info("Teleported player " + player.getName() + " to " + worldName);
        }
    }
    
    private boolean isDryLocation(Location location) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (world.getBlockAt(x + dx, y - 1, z + dz).isLiquid() ||
                    world.getBlockAt(x + dx, y, z + dz).isLiquid()) {
                    return false;
                }
            }
        }
        
        if (!world.getBlockAt(x, y - 1, z).getType().isSolid()) {
            return false;
        }
        
        String biomeName = world.getBiome(x, y, z).name().toLowerCase();
        if (biomeName.contains("ocean") || biomeName.contains("river") || 
            biomeName.contains("beach") || biomeName.contains("water")) {
            return false;
        }
        
        return true;
    }
    
    private Location findSafeLocation(Location initialLocation) {
        World world = initialLocation.getWorld();
        int x = initialLocation.getBlockX();
        int y = initialLocation.getBlockY();
        int z = initialLocation.getBlockZ();
        
        if (isSafeLocation(world, x, y, z) && isDryLocation(initialLocation)) {
            return initialLocation;
        }
        
        int radius = 20;
        for (int offsetX = -radius; offsetX <= radius; offsetX++) {
            for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                int distance = Math.abs(offsetX) + Math.abs(offsetZ);
                if (distance > radius) continue;
                
                for (int offsetY = -5; offsetY <= 5; offsetY++) {
                    int newX = x + offsetX;
                    int newY = y + offsetY;
                    int newZ = z + offsetZ;
                    
                    if (isSafeLocation(world, newX, newY, newZ)) {
                        Location possibleLocation = new Location(world, newX + 0.5, newY, newZ + 0.5, 
                                                 initialLocation.getYaw(), initialLocation.getPitch());
                        if (isDryLocation(possibleLocation)) {
                            return possibleLocation;
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    private boolean isSafeLocation(World world, int x, int y, int z) {
        if (!world.getBlockAt(x, y, z).isEmpty() || !world.getBlockAt(x, y + 1, z).isEmpty()) {
            return false;
        }
        
        if (world.getBlockAt(x, y - 1, z).isEmpty()) {
            return false;
        }
        
        if (world.getBlockAt(x, y - 1, z).isLiquid() || !world.getBlockAt(x, y - 1, z).getType().isSolid()) {
            return false;
        }
        
        if (world.getBlockAt(x, y, z).isLiquid() || world.getBlockAt(x, y + 1, z).isLiquid()) {
            return false;
        }
        
        return true;
    }
    
    private int getSafeYInNether(World world, int x, int z) {
        for (int y = 31; y < 115; y++) {
            if (world.getBlockAt(x, y, z).isEmpty() && 
                world.getBlockAt(x, y + 1, z).isEmpty() && 
                !world.getBlockAt(x, y - 1, z).isEmpty() &&
                !world.getBlockAt(x, y - 1, z).isLiquid()) {
                return y;
            }
        }
        
        int fallbackY = 60 + random.nextInt(10);
        plugin.getLogger().warning("Could not find safe nether location at X:" + x + " Z:" + z + ", using fallback Y:" + fallbackY);
        return fallbackY;
    }
    
    private int getSafeYInEnd(World world, int x, int z) {
        for (int y = 50; y < 100; y++) {
            if (world.getBlockAt(x, y, z).isEmpty() && 
                world.getBlockAt(x, y + 1, z).isEmpty() && 
                !world.getBlockAt(x, y - 1, z).isEmpty() &&
                !world.getBlockAt(x, y - 1, z).isLiquid()) {
                return y;
            }
        }
        return 64;
    }
}
