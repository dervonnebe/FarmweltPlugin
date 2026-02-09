package de.codingtt.farmweltplugin.services;

import de.codingtt.farmweltplugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.options.DeleteWorldOptions;
import org.mvplugins.multiverse.core.world.options.UnloadWorldOptions;
import org.mvplugins.multiverse.external.vavr.control.Option;

import java.io.File;

public class WorldManagementService {
    private final Main plugin;
    private final MultiverseCoreApi core;
    private final boolean useMultiverse;

    public WorldManagementService(Main plugin, MultiverseCoreApi core, boolean useMultiverse) {
        this.plugin = plugin;
        this.core = core;
        this.useMultiverse = useMultiverse;
    }

    public void loadWorld(String worldName) {
        if (useMultiverse) {
            loadWorldWithMultiverse(worldName);
        } else {
            loadWorldWithBukkit(worldName);
        }
    }

    public void unloadWorld(String worldName) {
        if (!worldExists(worldName)) {
            return;
        }

        try {
            if (useMultiverse) {
                unloadWorldWithMultiverse(worldName);
            } else {
                unloadWorldWithBukkit(worldName);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error unloading world: " + e.getMessage());
        }
    }

    public void deleteWorld(String worldName) {
        if (worldExists(worldName)) {
            unloadWorld(worldName);
            
            if (useMultiverse) {
                deleteWorldWithMultiverse(worldName);
            } else {
                deleteWorldWithBukkit(worldName);
            }
        } else {
            plugin.getLogger().warning("World does not exist: " + worldName);
        }
    }

    public boolean worldExists(String worldName) {
        if (Bukkit.getWorld(worldName) != null) {
            return true;
        }
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        return worldFolder.exists() && worldFolder.isDirectory();
    }

    private void loadWorldWithMultiverse(String worldName) {
        Option<MultiverseWorld> world = core.getWorldManager().getWorld(worldName);
        if (world.isDefined() && !world.get().isLoaded()) {
            core.getWorldManager().loadWorld(worldName)
                    .onFailure(reason -> plugin.getLogger().warning("Failed to load world: " + worldName + " Reason: " + reason))
                    .onSuccess(unused -> plugin.getLogger().info("World loaded: " + worldName));
        }
    }

    private void loadWorldWithBukkit(String worldName) {
        if (Bukkit.getWorld(worldName) == null) {
            World.Environment environment = determineEnvironment(worldName);
            String generator = getGeneratorForWorld(worldName);

            WorldCreator wc = new WorldCreator(worldName).environment(environment);
            if (generator != null && !generator.trim().isEmpty()) {
                wc.generator(generator);
            }
            wc.createWorld();
        }
    }

    private void unloadWorldWithMultiverse(String worldName) {
        Option<MultiverseWorld> mvWorldOpt = core.getWorldManager().getWorld(worldName);
        if (mvWorldOpt.isDefined() && mvWorldOpt.get() instanceof org.mvplugins.multiverse.core.world.LoadedMultiverseWorld) {
            org.mvplugins.multiverse.core.world.LoadedMultiverseWorld loadedWorld = 
                (org.mvplugins.multiverse.core.world.LoadedMultiverseWorld) mvWorldOpt.get();
            core.getWorldManager().unloadWorld(UnloadWorldOptions.world(loadedWorld))
                .onFailure(reason -> plugin.getLogger().warning("Failed to unload world: " + worldName + " Reason: " + reason))
                .onSuccess(unused -> plugin.getLogger().info("World unloaded: " + worldName));
        }
    }

    private void unloadWorldWithBukkit(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            Bukkit.unloadWorld(world, true);
            plugin.getLogger().info("World unloaded: " + worldName);
        }
    }

    private void deleteWorldWithMultiverse(String worldName) {
        Option<MultiverseWorld> mvWorldOpt = core.getWorldManager().getWorld(worldName);
        if (mvWorldOpt.isDefined()) {
            core.getWorldManager().deleteWorld(DeleteWorldOptions.world(mvWorldOpt.get()))
                .onFailure(reason -> plugin.getLogger().warning("Failed to delete world: " + worldName + " Reason: " + reason))
                .onSuccess(unused -> plugin.getLogger().info("World deleted: " + worldName));
        }
    }

    private void deleteWorldWithBukkit(String worldName) {
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

    private World.Environment determineEnvironment(String worldName) {
        if (worldName.equals(plugin.getNetherWorldName())) {
            return World.Environment.NETHER;
        } else if (worldName.equals(plugin.getEndWorldName())) {
            return World.Environment.THE_END;
        }
        return World.Environment.NORMAL;
    }

    private String getGeneratorForWorld(String worldName) {
        if (worldName.equals(plugin.getNetherWorldName())) {
            return plugin.getConfig().getString("nether-world-settings.generator");
        } else if (worldName.equals(plugin.getEndWorldName())) {
            return plugin.getConfig().getString("end-world-settings.generator");
        }
        return plugin.getConfig().getString("farmwelt-settings.generator");
    }
}
