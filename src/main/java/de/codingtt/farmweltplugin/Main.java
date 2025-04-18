package de.codingtt.farmweltplugin;

import de.codingtt.farmweltplugin.commands.FarmweltCommand;
import de.codingtt.farmweltplugin.commands.NetherCommand;
import de.codingtt.farmweltplugin.commands.EndCommand;
import de.codingtt.farmweltplugin.utils.FarmweltMenu;
import de.codingtt.farmweltplugin.utils.FarmweltPlaceholders;
import de.codingtt.farmweltplugin.utils.MenuListener;
import de.codingtt.farmweltplugin.utils.ScheduledReset;
import de.codingtt.farmweltplugin.utils.WorldUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public final class Main extends JavaPlugin {
    private static Main instance;
    private static final int BSTATS_PLUGIN_ID = 24022;
    private WorldUtils worldUtils;
    private ScheduledReset scheduldReset;
    private FileConfiguration languageConfig;
    private File languageFile;
    private String language;
    private FarmweltMenu farmweltMenu;
    private Map<String, LocalDateTime> lastResetTimes;

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        this.language = getConfig().getString("language", "de");
        loadLanguageConfig();
        
        this.lastResetTimes = new HashMap<>();
        
        this.worldUtils = new WorldUtils(this);
        
        this.farmweltMenu = new FarmweltMenu(this, worldUtils);

        if (getConfig().getBoolean("use-bstats", true)) {
            new Metrics(this, BSTATS_PLUGIN_ID);
            getLogger().info("bStats metrics collection enabled");
        } else {
            getLogger().info("bStats metrics collection disabled in config");
        }

        registerCommands();
        registerEvents();
        
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new FarmweltPlaceholders(this).register();
            getLogger().info("PlaceholderAPI found - Registering placeholders");
        } else {
            getLogger().info("PlaceholderAPI not found - Placeholders will not be available");
        }

        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (worldUtils.worldExists(getWorldName())) {
                worldUtils.loadWorld(getWorldName());
            } else {
                worldUtils.createWorld(getWorldName(), World.Environment.NORMAL);
                updateLastResetTime(getWorldName());
            }
            
            if (getConfig().getBoolean("menu.nether-world.enabled", false)) {
                if (worldUtils.worldExists(getNetherWorldName())) {
                    worldUtils.loadWorld(getNetherWorldName());
                } else {
                    worldUtils.createWorld(getNetherWorldName(), World.Environment.NETHER);
                    updateLastResetTime(getNetherWorldName());
                }
            }
            
            if (getConfig().getBoolean("menu.end-world.enabled", false)) {
                if (worldUtils.worldExists(getEndWorldName())) {
                    worldUtils.loadWorld(getEndWorldName());
                } else {
                    worldUtils.createWorld(getEndWorldName(), World.Environment.THE_END);
                    updateLastResetTime(getEndWorldName());
                }
            }
        }, 40L);

        this.scheduldReset = new ScheduledReset(this, worldUtils);
        this.scheduldReset.runTaskTimer(this, 20L, 20L);
    }

    private void loadLanguageConfig() {
        File langFolder = new File(getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
            saveResource("lang/de.yml", false);
            saveResource("lang/en.yml", false);
        }
        
        languageFile = new File(langFolder, language + ".yml");
        
        if (!languageFile.exists()) {
            getLogger().warning("Language file " + language + ".yml not found, defaulting to de.yml");
            languageFile = new File(langFolder, "de.yml");
            language = "de";
            
            if (!languageFile.exists()) {
                try {
                    languageFile.createNewFile();
                    InputStream inputStream = getResource("lang/de.yml");
                    if (inputStream != null) {
                        Files.copy(inputStream, languageFile.toPath());
                    }
                } catch (IOException e) {
                    getLogger().severe("Could not create default language file: " + e.getMessage());
                }
            }
        }
        
        languageConfig = YamlConfiguration.loadConfiguration(languageFile);
    }

    private void registerCommands() {
        FarmweltCommand farmweltCommand = new FarmweltCommand(this, worldUtils, farmweltMenu);
        getCommand("farmwelt").setExecutor(farmweltCommand);
        
        NetherCommand netherCommand = new NetherCommand(this, worldUtils);
        EndCommand endCommand = new EndCommand(this, worldUtils);
        getCommand("nether").setExecutor(netherCommand);
        getCommand("end").setExecutor(endCommand);
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new MenuListener(this, worldUtils), this);
    }

    public static Main getInstance() {
        return instance;
    }

    public String getWorldName() {
        return getConfig().getString("farmwelt-world", "farmwelt");
    }
    
    public String getNetherWorldName() {
        return getConfig().getString("nether-farmwelt-world", "farmwelt_nether");
    }
    
    public String getEndWorldName() {
        return getConfig().getString("end-farmwelt-world", "farmwelt_end");
    }
    
    public FarmweltMenu getFarmweltMenu() {
        return farmweltMenu;
    }
    
    public void updateLastResetTime(String worldName) {
        lastResetTimes.put(worldName, LocalDateTime.now(ZoneId.of(getConfig().getString("reset-schedule.timezone", "Europe/Berlin"))));
    }
    
    public LocalDateTime getLastResetTime(String worldName) {
        return lastResetTimes.get(worldName);
    }

    public String getLanguageString(String path) {
        String text = languageConfig.getString(path, "");
        if (text.isEmpty()) {
            getLogger().warning("Missing language string: " + path);
            return "§cMissing text for: " + path;
        }
        return text.replace("&", "§");
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        String newLanguage = getConfig().getString("language", "de");
        if (!newLanguage.equals(language)) {
            language = newLanguage;
            loadLanguageConfig();
        }
    }

    @Override
    public void onDisable() {
        if (worldUtils.worldExists(getWorldName())) {
            worldUtils.unloadWorld(getWorldName());
        }
        
        if (worldUtils.worldExists(getNetherWorldName())) {
            worldUtils.unloadWorld(getNetherWorldName());
        }
        
        if (worldUtils.worldExists(getEndWorldName())) {
            worldUtils.unloadWorld(getEndWorldName());
        }
    }
}
