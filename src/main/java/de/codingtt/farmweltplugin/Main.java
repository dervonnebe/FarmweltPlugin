package de.codingtt.farmweltplugin;

import de.codingtt.farmweltplugin.commands.*;
import de.codingtt.farmweltplugin.utils.ScheduledReset;
import de.codingtt.farmweltplugin.utils.WorldUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main instance;
    private static final int BSTATS_PLUGIN_ID = 24022;

    WorldUtils worldUtils = new WorldUtils();
    private ScheduledReset scheduldReset;

    public String getWorldName() {
        return getConfig().getString("farmwelt-world");
    }

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();

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

    @Override
    public void onDisable() {
        worldUtils.unloadWorld(getWorldName());
    }


    private void registerCommands() {
        FarmweltCommand farmweltCommand = new FarmweltCommand();
        getCommand("farmwelt").setExecutor(farmweltCommand);
        getCommand("farmwelt").setTabCompleter(farmweltCommand);
    }

    private void registerEvents() {
    }


    public static Main getInstance() {
        return instance;
    }

    public String getColoredString(String path) {
        String configString = getConfig().getString(path);
        if (configString != null) {
            return ChatColor.translateAlternateColorCodes('&', configString);
        }
        return null;
    }


    }
