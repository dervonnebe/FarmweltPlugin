package de.codingtt.farmweltplugin;

import de.codingtt.farmweltplugin.commands.*;
import de.codingtt.farmweltplugin.utils.ScheduldReset;
import de.codingtt.farmweltplugin.utils.WorldUtils;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main instance;

    WorldUtils worldUtils = new WorldUtils();
    private ScheduldReset scheduldReset;

    public String getWorldName() {
        return getConfig().getString("farmwelt-world");
    }

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();

        registerCommands();
        registerEvents();

        if (worldUtils.wolrdExists(getWorldName())) {
            worldUtils.loadWorld(getWorldName());
        } else {
            worldUtils.createWorld(getWorldName(), World.Environment.NORMAL);
        }

        this.scheduldReset = new ScheduldReset(this, worldUtils);
        this.scheduldReset.runTaskTimer(this, 0L, 20L); // Run every second

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
