package top.jaxlabs.farmweltplugin.services;

import top.jaxlabs.farmweltplugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Random;

public class TeleportService {
    private final Main plugin;
    private final WorldManagementService worldManagement;
    private final Random random = new Random();

    public TeleportService(Main plugin, WorldManagementService worldManagement) {
        this.plugin = plugin;
        this.worldManagement = worldManagement;
    }

    public void teleportToWorld(Player player, String worldName) {
        if (!worldManagement.worldExists(worldName)) {
            return;
        }

        saveBackLocation(player, worldName);

        World world = loadWorldIfNeeded(worldName);
        if (world == null) {
            plugin.getLogger().severe("Could not load world: " + worldName);
            return;
        }

        Location targetLocation = determineTargetLocation(world, worldName);
        player.teleport(targetLocation);
        plugin.getLogger().info("Teleported player " + player.getName() + " to " + worldName);
    }

    private void saveBackLocation(Player player, String worldName) {
        if (plugin.isBackEnabled()) {
            String currentWorld = player.getWorld().getName();
            if (!currentWorld.equals(plugin.getWorldName()) &&
                !currentWorld.equals(plugin.getNetherWorldName()) &&
                !currentWorld.equals(plugin.getEndWorldName())) {
                plugin.setBackLocation(player.getUniqueId(), player.getLocation());
            }
        }
    }

    private World loadWorldIfNeeded(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            worldManagement.loadWorld(worldName);
            world = Bukkit.getWorld(worldName);
        }
        return world;
    }

    private Location determineTargetLocation(World world, String worldName) {
        if (!shouldUseRandomTeleport(worldName)) {
            return world.getSpawnLocation();
        }

        int maxDistance = getTeleportDistance(worldName);
        Location safeLocation = findSafeRandomLocation(world, maxDistance);
        
        return safeLocation != null ? safeLocation : world.getSpawnLocation();
    }

    private boolean shouldUseRandomTeleport(String worldName) {
        if (worldName.equals(plugin.getWorldName())) {
            return plugin.getConfig().getBoolean("farmwelt-settings.use-random-teleport", true);
        } else if (worldName.equals(plugin.getNetherWorldName())) {
            return plugin.getConfig().getBoolean("nether-world-settings.use-random-teleport", true);
        } else if (worldName.equals(plugin.getEndWorldName())) {
            return plugin.getConfig().getBoolean("end-world-settings.use-random-teleport", true);
        }
        return true;
    }

    private int getTeleportDistance(String worldName) {
        if (worldName.equals(plugin.getWorldName())) {
            return plugin.getConfig().getInt("farmwelt-settings.teleport-distance", 
                    plugin.getConfig().getInt("random-teleport-distance", 1000));
        } else if (worldName.equals(plugin.getNetherWorldName())) {
            return plugin.getConfig().getInt("nether-world-settings.teleport-distance", 
                    plugin.getConfig().getInt("random-teleport-distance", 500));
        } else if (worldName.equals(plugin.getEndWorldName())) {
            return plugin.getConfig().getInt("end-world-settings.teleport-distance", 
                    plugin.getConfig().getInt("random-teleport-distance", 1000));
        }
        return plugin.getConfig().getInt("random-teleport-distance", 1000);
    }

    private Location findSafeRandomLocation(World world, int maxDistance) {
        final int MAX_ATTEMPTS = 10;
        
        for (int attempts = 0; attempts < MAX_ATTEMPTS; attempts++) {
            int x = random.nextInt(maxDistance * 2) - maxDistance;
            int z = random.nextInt(maxDistance * 2) - maxDistance;
            int y = getSafeY(world, x, z);
            
            Location randomLocation = new Location(world, x, y + 1, z);
            Location safeLocation = findSafeLocation(randomLocation);
            
            if (safeLocation != null && isDryLocation(safeLocation)) {
                return safeLocation;
            }
        }
        
        plugin.getLogger().warning("Could not find safe location after " + MAX_ATTEMPTS + " attempts");
        return null;
    }

    private int getSafeY(World world, int x, int z) {
        World.Environment environment = world.getEnvironment();
        
        if (environment == World.Environment.NETHER) {
            return getSafeYInNether(world, x, z);
        } else if (environment == World.Environment.THE_END) {
            return getSafeYInEnd(world, x, z);
        }
        
        return world.getHighestBlockYAt(x, z);
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
        return 60 + random.nextInt(10);
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

    private Location findSafeLocation(Location initialLocation) {
        World world = initialLocation.getWorld();
        int x = initialLocation.getBlockX();
        int y = initialLocation.getBlockY();
        int z = initialLocation.getBlockZ();
        
        if (isSafeLocation(world, x, y, z)) {
            return initialLocation;
        }
        
        int radius = 20;
        for (int offsetX = -radius; offsetX <= radius; offsetX++) {
            for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                if (Math.abs(offsetX) + Math.abs(offsetZ) > radius) continue;
                
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
        return world.getBlockAt(x, y, z).isEmpty() && 
               world.getBlockAt(x, y + 1, z).isEmpty() &&
               !world.getBlockAt(x, y - 1, z).isEmpty() &&
               world.getBlockAt(x, y - 1, z).getType().isSolid() &&
               !world.getBlockAt(x, y - 1, z).isLiquid() &&
               !world.getBlockAt(x, y, z).isLiquid() && 
               !world.getBlockAt(x, y + 1, z).isLiquid();
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
        return !biomeName.contains("ocean") && !biomeName.contains("river") && 
               !biomeName.contains("beach") && !biomeName.contains("water");
    }
}
