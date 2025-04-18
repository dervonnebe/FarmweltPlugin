package de.codingtt.farmweltplugin;

import de.codingtt.farmweltplugin.commands.FarmweltCommand;
import de.codingtt.farmweltplugin.utils.FarmweltMenu;
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
        
        // Initialisiere LastResetTimes Map
        this.lastResetTimes = new HashMap<>();
        
        // Initialisiere WorldUtils mit Plugin-Instanz
        this.worldUtils = new WorldUtils(this);
        
        // Initialisiere das FarmweltMenu
        this.farmweltMenu = new FarmweltMenu(this, worldUtils);

        // bStats Metrics
        new Metrics(this, BSTATS_PLUGIN_ID);

        registerCommands();
        registerEvents();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            // Normal Farmwelt erstellen/laden
            if (worldUtils.worldExists(getWorldName())) {
                worldUtils.loadWorld(getWorldName());
            } else {
                worldUtils.createWorld(getWorldName(), World.Environment.NORMAL);
                updateLastResetTime(getWorldName());
            }
            
            // Nether Farmwelt erstellen/laden, falls aktiviert
            if (getConfig().getBoolean("menu.nether-world.enabled", false)) {
                if (worldUtils.worldExists(getNetherWorldName())) {
                    worldUtils.loadWorld(getNetherWorldName());
                } else {
                    worldUtils.createWorld(getNetherWorldName(), World.Environment.NETHER);
                    updateLastResetTime(getNetherWorldName());
                }
            }
            
            // End Farmwelt erstellen/laden, falls aktiviert
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
    }

    private void registerEvents() {
        // Menu Listener registrieren
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
        // Entlade alle Farmwelten
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
