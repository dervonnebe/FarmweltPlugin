package top.jaxlabs.farmweltplugin.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker implements Listener {

    private final JavaPlugin plugin;
    private final String repoUrl = "https://api.github.com/repos/dervonnebe/FarmweltPlugin/releases/latest";
    private String latestVersion;
    private String updateTitle;
    private boolean updateAvailable = false;

    public UpdateChecker(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL(repoUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "FarmweltPlugin");

                if (connection.getResponseCode() == 200) {
                    InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                    JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
                    
                    latestVersion = json.get("tag_name").getAsString();
                    updateTitle = json.get("name").getAsString();
                    
                    String currentVersion = plugin.getDescription().getVersion();
                    
                    if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                        updateAvailable = true;
                        plugin.getLogger().info("--------------------------------------------------");
                        plugin.getLogger().info("A new version of FarmweltPlugin is available!");
                        plugin.getLogger().info("Current version: " + currentVersion);
                        plugin.getLogger().info("New version: " + latestVersion);
                        plugin.getLogger().info("Title: " + updateTitle);
                        plugin.getLogger().info("Download: https://github.com/dervonnebe/FarmweltPlugin/releases/latest");
                        plugin.getLogger().info("--------------------------------------------------");
                    } else {
                         plugin.getLogger().info("FarmweltPlugin is up to date.");
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
            }
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!updateAvailable) return;
        if (!plugin.getConfig().getBoolean("update-notification", true)) return;

        Player player = event.getPlayer();
        if (player.hasPermission("farmwelt.admin") || player.hasPermission("farmwelt.update")) {
             player.sendMessage(ChatColor.GRAY + "--------------------------------------------------");
             player.sendMessage(ChatColor.GOLD + "FarmweltPlugin Update Available!");
             player.sendMessage(ChatColor.AQUA + "Version: " + ChatColor.WHITE + latestVersion);
             player.sendMessage(ChatColor.AQUA + "Title: " + ChatColor.WHITE + updateTitle);
             player.sendMessage(ChatColor.GREEN + "Download: " + ChatColor.UNDERLINE + "https://github.com/dervonnebe/FarmweltPlugin/releases/latest");
             player.sendMessage(ChatColor.GRAY + "--------------------------------------------------");
        }
    }
}
