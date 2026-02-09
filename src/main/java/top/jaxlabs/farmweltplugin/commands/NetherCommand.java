package top.jaxlabs.farmweltplugin.commands;

import top.jaxlabs.farmweltplugin.Main;
import top.jaxlabs.farmweltplugin.utils.WorldUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NetherCommand implements CommandExecutor {
    private final Main plugin;
    private final WorldUtils worldUtils;

    public NetherCommand(Main plugin, WorldUtils worldUtils) {
        this.plugin = plugin;
        this.worldUtils = worldUtils;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("command-player-only"));
            return true;
        }

        if (!player.hasPermission(plugin.getConfig().getString("default-permission", "farmwelt.use"))) {
            player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("no-permission"));
            return true;
        }
        
        // Prüfe, ob die Nether-Welt aktiviert ist
        if (!plugin.getConfig().getBoolean("menu.nether-world.enabled", false)) {
            player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("nether-not-enabled"));
            return true;
        }

        if (worldUtils.worldExists(plugin.getNetherWorldName())) {
            worldUtils.teleportToWorld(player, plugin.getNetherWorldName());
            player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("nether-teleport"));
        } else {
            player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("no-nether-world"));
        }
        
        return true;
    }
} 