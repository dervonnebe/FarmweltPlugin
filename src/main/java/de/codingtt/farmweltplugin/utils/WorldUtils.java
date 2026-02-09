package de.codingtt.farmweltplugin.utils;

import de.codingtt.farmweltplugin.Main;
import de.codingtt.farmweltplugin.services.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.mvplugins.multiverse.core.MultiverseCoreApi;

/**
 * Facade for world management operations.
 * Delegates to specialized service classes following Single Responsibility Principle.
 */
public class WorldUtils {
    private final Main plugin;
    private final MultiverseCoreApi core;
    private final boolean useMultiverse;
    
    private final WorldCreationService creationService;
    private final WorldManagementService managementService;
    private final TeleportService teleportService;
    private final WorldResetService resetService;
    private final WorldBorderService borderService;
    private final ChunkyIntegrationService chunkyService;

    public WorldUtils(Main plugin) {
        this.plugin = plugin;
        Plugin mvPlugin = Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
        this.useMultiverse = mvPlugin != null;
        
        Plugin chunkyPlugin = Bukkit.getServer().getPluginManager().getPlugin("Chunky");
        boolean useChunky = chunkyPlugin != null;

        if (useMultiverse) {
            this.core = MultiverseCoreApi.get();
            plugin.getLogger().info("Multiverse-Core found - Activate Multiverse-Integration");
        } else {
            this.core = null;
            plugin.getLogger().info("Multiverse-Core not found - Use Bukkit World Management");
        }

        if (useChunky) {
            plugin.getLogger().info("Chunky found - Activate Chunky-Integration");
        } else {
            plugin.getLogger().info("Chunky not found - Chunky-Integration disabled");
        }
        
        this.borderService = new WorldBorderService(plugin);
        this.chunkyService = new ChunkyIntegrationService(plugin, useChunky);
        this.managementService = new WorldManagementService(plugin, core, useMultiverse);
        this.creationService = new WorldCreationService(plugin, core, useMultiverse);
        this.teleportService = new TeleportService(plugin, managementService);
        this.resetService = new WorldResetService(plugin, core, useMultiverse, managementService, creationService);
    }

    public void createWorld(String worldName, World.Environment environment) {
        creationService.createWorld(worldName, environment, borderService, chunkyService);
    }

    public void deleteWorld(String worldName) {
        managementService.deleteWorld(worldName);
    }

    public void resetWorld(String worldName) {
        resetService.resetWorld(worldName, borderService, chunkyService);
    }

    public void loadWorld(String worldName) {
        managementService.loadWorld(worldName);
    }

    public void unloadWorld(String worldName) {
        managementService.unloadWorld(worldName);
    }

    public boolean worldExists(String worldName) {
        return managementService.worldExists(worldName);
    }

    public void teleportToWorld(Player player, String worldName) {
        teleportService.teleportToWorld(player, worldName);
    }
}
