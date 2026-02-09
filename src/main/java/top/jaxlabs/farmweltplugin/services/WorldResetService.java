package top.jaxlabs.farmweltplugin.services;

import top.jaxlabs.farmweltplugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.options.DeleteWorldOptions;
import org.mvplugins.multiverse.external.vavr.control.Option;

public class WorldResetService {
    private final Main plugin;
    private final MultiverseCoreApi core;
    private final boolean useMultiverse;
    private final WorldManagementService worldManagement;
    private final WorldCreationService worldCreation;

    public WorldResetService(Main plugin, MultiverseCoreApi core, boolean useMultiverse,
                           WorldManagementService worldManagement, WorldCreationService worldCreation) {
        this.plugin = plugin;
        this.core = core;
        this.useMultiverse = useMultiverse;
        this.worldManagement = worldManagement;
        this.worldCreation = worldCreation;
    }

    public void resetWorld(String worldName, WorldBorderService borderService, 
                          ChunkyIntegrationService chunkyService) {
        if (!worldManagement.worldExists(worldName)) {
            plugin.getLogger().warning("Cannot reset - World does not exist: " + worldName);
            return;
        }

        plugin.getLogger().info("Starting world reset: " + worldName);
        
        teleportPlayersToSpawn(worldName);
        World.Environment environment = getWorldEnvironment(worldName);
        
        try {
            worldManagement.unloadWorld(worldName);
            deleteAndRecreate(worldName, environment, borderService, chunkyService);
        } catch (Exception e) {
            plugin.getLogger().severe("Error during world reset: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void teleportPlayersToSpawn(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            World defaultWorld = Bukkit.getWorlds().get(0);
            world.getPlayers().forEach(player -> {
                player.teleport(defaultWorld.getSpawnLocation());
                player.sendMessage(plugin.getLanguageString("prefix") + 
                                 plugin.getLanguageString("reset-warning"));
            });
        }
    }

    private World.Environment getWorldEnvironment(String worldName) {
        if (useMultiverse) {
            Option<MultiverseWorld> mvWorldOpt = core.getWorldManager().getWorld(worldName);
            if (mvWorldOpt.isDefined()) {
                return mvWorldOpt.get().getEnvironment();
            }
        } else {
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                return world.getEnvironment();
            }
        }
        return World.Environment.NORMAL;
    }

    private void deleteAndRecreate(String worldName, World.Environment environment,
                                   WorldBorderService borderService, 
                                   ChunkyIntegrationService chunkyService) {
        if (useMultiverse) {
            deleteAndRecreateWithMultiverse(worldName, environment, borderService, chunkyService);
        } else {
            worldManagement.deleteWorld(worldName);
            worldCreation.createWorld(worldName, environment, borderService, chunkyService);
            plugin.getLogger().info("World reset completed: " + worldName);
            plugin.updateLastResetTime(worldName);
        }
    }

    private void deleteAndRecreateWithMultiverse(String worldName, World.Environment environment,
                                                WorldBorderService borderService, 
                                                ChunkyIntegrationService chunkyService) {
        Option<MultiverseWorld> mvWorldOpt = core.getWorldManager().getWorld(worldName);
        if (mvWorldOpt.isDefined()) {
            core.getWorldManager().deleteWorld(DeleteWorldOptions.world(mvWorldOpt.get()))
                .onFailure(reason -> plugin.getLogger().warning("Failed to delete world: " + worldName + " Reason: " + reason))
                .onSuccess(unused -> {
                    worldCreation.createWorld(worldName, environment, borderService, chunkyService);
                    plugin.getLogger().info("World reset completed: " + worldName);
                    plugin.updateLastResetTime(worldName);
                });
        }
    }
}
