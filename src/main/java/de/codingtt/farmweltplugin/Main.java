package de.codingtt.farmweltplugin;

import de.codingtt.farmweltplugin.commands.FarmweltCommand;
import de.codingtt.farmweltplugin.utils.ScheduledReset;
import de.codingtt.farmweltplugin.utils.WorldUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private static Main instance;
    private static final int BSTATS_PLUGIN_ID = 24022;
    private WorldUtils worldUtils;
    private ScheduledReset scheduldReset;

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        
        // Initialisiere WorldUtils mit Plugin-Instanz
        this.worldUtils = new WorldUtils(this);

        // bStats Metrics
        new Metrics(this, BSTATS_PLUGIN_ID);

        registerCommands();
        registerEvents();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (worldUtils.worldExists(getWorldName())) {
                worldUtils.loadWorld(getWorldName());
            } else {
                worldUtils.createWorld(getWorldName(), World.Environment.NORMAL);
            }
        }, 40L);

        this.scheduldReset = new ScheduledReset(this, worldUtils);
        this.scheduldReset.runTaskTimer(this, 20L, 20L);
    }

    private void registerCommands() {
        FarmweltCommand farmweltCommand = new FarmweltCommand(this, worldUtils);
        getCommand("farmwelt").setExecutor(farmweltCommand);
    }

    private void registerEvents() {
        // Event registrierung hier falls benötigt
    }

    public static Main getInstance() {
        return instance;
    }

    public String getWorldName() {
        return getConfig().getString("farmwelt-world", "farmwelt");
    }

    public String getColoredString(String path) {
        return getConfig().getString(path, "").replace("&", "§");
    }

    @Override
    public void onDisable() {
        if (worldUtils.worldExists(getWorldName())) {
            worldUtils.unloadWorld(getWorldName());
        }
    }
}
