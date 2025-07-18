package de.codingtt.farmweltplugin.utils;

import de.codingtt.farmweltplugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.mvplugins.multiverse.core.MultiverseCore;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.world.options.CreateWorldOptions;
import org.mvplugins.multiverse.core.world.options.DeleteWorldOptions;
import org.mvplugins.multiverse.core.world.options.UnloadWorldOptions;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.core.world.MultiverseWorld;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class WorldUtils {
    private final Main plugin;
    private final MultiverseCoreApi core;
    private final Random random = new Random();
    private final boolean useMultiverse;

    public WorldUtils(Main plugin) {
        this.plugin = plugin;
        Plugin mvPlugin = Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
        this.useMultiverse = mvPlugin != null;
        
        if (useMultiverse) {
            this.core = MultiverseCoreApi.get();
            plugin.getLogger().info("Multiverse-Core found - Activate Multiverse-Integration");
        } else {
            this.core = null;
            plugin.getLogger().info("Multiverse-Core not found - Use Bukkit World Management");
        }
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
            if (seed == null) {
                seed = random.nextLong();
            }

            boolean success = false;

            if (useMultiverse) {
                core.getWorldManager()
                        .createWorld(CreateWorldOptions.worldName(worldName)
                                .environment(environment)
                                .seed(seed)
                                .generateStructures(true))
                        .onFailure(reason -> plugin.getLogger().warning("Failed to create world: " + worldName + " Reason: " + reason))
                        .onSuccess(newWorld -> {
                            plugin.getLogger().info("World created successfully: " + worldName + " (" + environment + ")");
                        });
                success = true; // Erfolg wird asynchron gemeldet
            } else {
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
                    plugin.getLogger().info("World created successfully: " + worldName + " (" + environment + ")");
                    loadWorld(worldName);
                } else {
                    plugin.getLogger().warning("Failed to create world: " + worldName);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error creating world: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteWorld(String worldName) {
        if (worldExists(worldName)) {
            unloadWorld(worldName);
            
            if (useMultiverse) {
                Option<MultiverseWorld> mvWorldOpt = core.getWorldManager().getWorld(worldName);
                if (mvWorldOpt.isDefined()) {
                    core.getWorldManager().deleteWorld(DeleteWorldOptions.world(mvWorldOpt.get()))
                        .onFailure(reason -> plugin.getLogger().warning("Failed to delete world: " + worldName + " Reason: " + reason))
                        .onSuccess(unused -> plugin.getLogger().info("World deleted: " + worldName));
                } else {
                    plugin.getLogger().warning("Failed to delete world: " + worldName + " - MultiverseWorld not found");
                }
            } else {
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    Bukkit.unloadWorld(world, false);
                }
                
                File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
                if (worldFolder.exists() && worldFolder.isDirectory()) {
                    deleteDirectory(worldFolder);
                }
                plugin.getLogger().info("World deleted: " + worldName);
            }
        } else {
            plugin.getLogger().warning("World does not exist: " + worldName);
        }
    }
    
    private boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return directory.delete();
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
            final World.Environment environment;
            
            if (useMultiverse) {
                Option<MultiverseWorld> mvWorldOpt = core.getWorldManager().getWorld(worldName);
                if (mvWorldOpt.isDefined()) {
                    environment = mvWorldOpt.get().getEnvironment();
                } else {
                    environment = World.Environment.NORMAL;
                }
            } else if (world != null) {
                environment = world.getEnvironment();
            } else {
                environment = World.Environment.NORMAL;
            }
            
            unloadWorld(worldName);
            
            if (useMultiverse) {
                Option<MultiverseWorld> mvWorldOptForDelete = core.getWorldManager().getWorld(worldName);
                if (mvWorldOptForDelete.isDefined()) {
                    core.getWorldManager().deleteWorld(DeleteWorldOptions.world(mvWorldOptForDelete.get()))
                        .onFailure(reason -> plugin.getLogger().warning("Failed to delete world: " + worldName + " Reason: " + reason))
                        .onSuccess(unused -> {
                            createWorld(worldName, environment);
                            plugin.getLogger().info("World reset completed: " + worldName);
                            plugin.updateLastResetTime(worldName);
                        });
                } else {
                    plugin.getLogger().warning("Failed to delete world: " + worldName + " - MultiverseWorld not found");
                }
            } else {
                // Bukkit-Alternative
                File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
                if (worldFolder.exists() && worldFolder.isDirectory()) {
                    deleteDirectory(worldFolder);
                }
                createWorld(worldName, environment);
                plugin.getLogger().info("World reset completed: " + worldName);
                plugin.updateLastResetTime(worldName);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error during world reset: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadWorld(String worldName) {
        if (useMultiverse) {
             Option<MultiverseWorld> world = core.getWorldManager().getWorld(worldName);
             if (!world.get().isLoaded()) {
                 core.getWorldManager().loadWorld(worldName)
                         .onFailure(reason -> plugin.getLogger().warning("Failed to load world: " + worldName + " Reason: " + reason))
                         .onSuccess(unused -> plugin.getLogger().info("World loaded: " + worldName));
             }
        } else {
            // Bukkit-Alternative
            if (Bukkit.getWorld(worldName) == null) {
                // Environment aus Config bestimmen
                World.Environment environment = World.Environment.NORMAL;
                if (worldName.equals(plugin.getNetherWorldName())) {
                    environment = World.Environment.NETHER;
                } else if (worldName.equals(plugin.getEndWorldName())) {
                    environment = World.Environment.THE_END;
                }
                new WorldCreator(worldName).environment(environment).createWorld();
            }
        }
    }

    public void unloadWorld(String worldName) {
        if (!worldExists(worldName)) {
            return;
        }

        try {
            if (useMultiverse) {
                Option<MultiverseWorld> mvWorldOpt = core.getWorldManager().getWorld(worldName);
                if (mvWorldOpt.isDefined() && mvWorldOpt.get() instanceof org.mvplugins.multiverse.core.world.LoadedMultiverseWorld) {
                    org.mvplugins.multiverse.core.world.LoadedMultiverseWorld loadedWorld = (org.mvplugins.multiverse.core.world.LoadedMultiverseWorld) mvWorldOpt.get();
                    core.getWorldManager().unloadWorld(UnloadWorldOptions.world(loadedWorld))
                        .onFailure(reason -> plugin.getLogger().warning("Failed to unload world: " + worldName + " Reason: " + reason))
                        .onSuccess(unused -> plugin.getLogger().info("World unloaded: " + worldName));
                } else {
                    plugin.getLogger().warning("Failed to unload world: " + worldName + " - LoadedMultiverseWorld not found");
                }
            } else {
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    Bukkit.unloadWorld(world, true);
                }
                plugin.getLogger().info("World unloaded: " + worldName);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error unloading world: " + e.getMessage());
        }
    }

    public boolean worldExists(String worldName) {
        if (Bukkit.getWorld(worldName) != null) {
            return true;
        }

        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        return worldFolder.exists() && worldFolder.isDirectory();
    }

    public void teleportToWorld(Player player, String worldName) {
        if (worldExists(worldName)) {
            // Back-Feature: Ursprungsposition speichern
            if (plugin.isBackEnabled()) {
                // Nur speichern, wenn Spieler nicht schon in einer Farmwelt ist
                String currentWorld = player.getWorld().getName();
                if (!currentWorld.equals(plugin.getWorldName()) &&
                    !currentWorld.equals(plugin.getNetherWorldName()) &&
                    !currentWorld.equals(plugin.getEndWorldName())) {
                    plugin.setBackLocation(player.getUniqueId(), player.getLocation());
                }
            }

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                loadWorld(worldName);
                world = Bukkit.getWorld(worldName);
            }

            if (world == null) {
                plugin.getLogger().severe("Could not load world: " + worldName);
                return;
            }

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
