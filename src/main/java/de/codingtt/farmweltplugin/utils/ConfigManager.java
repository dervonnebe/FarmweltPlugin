package de.codingtt.farmweltplugin.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;

public class ConfigManager {

    private final JavaPlugin plugin;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void checkConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
            return;
        }

        FileConfiguration currentConfig = YamlConfiguration.loadConfiguration(configFile);
        InputStream defaultStream = plugin.getResource("config.yml");
        if (defaultStream == null) {
            return;
        }

        FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
        Set<String> defaultKeys = defaultConfig.getKeys(true);
        
        boolean missingKeys = false;
        for (String key : defaultKeys) {
            if (!currentConfig.contains(key)) {
                missingKeys = true;
                break;
            }
        }

        if (missingKeys) {
            plugin.getLogger().info("Config is outdated or missing keys. Starting update process...");
            
            // Backup
            try {
                File backupDir = new File(plugin.getDataFolder(), "backups");
                if (!backupDir.exists()) {
                    backupDir.mkdirs();
                }
                String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                File backupFile = new File(backupDir, "config-backup-" + timestamp + ".yml");
                Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().info("Backup created: " + backupFile.getName());
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create config backup!", e);
            }

            // Create new config with values from old config where possible
            for (String key : defaultKeys) {
                if (!defaultConfig.isConfigurationSection(key) && currentConfig.contains(key)) {
                    defaultConfig.set(key, currentConfig.get(key));
                }
            }

            // Save the updated default config as the new config
            try {
                defaultConfig.save(configFile);
                plugin.getLogger().info("Config updated successfully. Unknown keys were removed.");
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not save updated config!", e);
            }
            
            // Reload the plugin config to ensure it's using the new file
            plugin.reloadConfig();
        }
    }
}
