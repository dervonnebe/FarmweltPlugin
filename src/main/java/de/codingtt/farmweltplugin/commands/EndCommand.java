package de.codingtt.farmweltplugin.commands;

import de.codingtt.farmweltplugin.Main;
import de.codingtt.farmweltplugin.utils.WorldUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EndCommand implements CommandExecutor {
    private final Main plugin;
    private final WorldUtils worldUtils;

    public EndCommand(Main plugin, WorldUtils worldUtils) {
        this.plugin = plugin;
        this.worldUtils = worldUtils;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("command-player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission(plugin.getConfig().getString("default-permission", "farmwelt.use"))) {
            player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("no-permission"));
            return true;
        }
        
        if (!plugin.getConfig().getBoolean("menu.end-world.enabled", false)) {
            player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("end-not-enabled"));
            return true;
        }

        if (worldUtils.worldExists(plugin.getEndWorldName())) {
            worldUtils.teleportToWorld(player, plugin.getEndWorldName());
            player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("end-teleport"));
        } else {
            player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("no-end-world"));
        }
        
        return true;
    }
} 