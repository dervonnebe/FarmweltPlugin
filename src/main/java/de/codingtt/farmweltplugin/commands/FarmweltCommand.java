package de.codingtt.farmweltplugin.commands;

import de.codingtt.farmweltplugin.Main;
import de.codingtt.farmweltplugin.utils.WorldUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FarmweltCommand implements CommandExecutor, TabCompleter {
    private final Main plugin;
    private final WorldUtils worldUtils;

    public FarmweltCommand(Main plugin, WorldUtils worldUtils) {
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

        if (args.length == 0) {
            if (!player.hasPermission(plugin.getConfig().getString("default-permission", "farmwelt.use"))) {
                player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("no-permission"));
                return true;
            }

            if (worldUtils.worldExists(plugin.getWorldName())) {
                worldUtils.teleportToWorld(player, plugin.getWorldName());
                player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("world-teleport"));
            } else {
                player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("no-world"));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            if (!player.hasPermission(plugin.getConfig().getString("admin-permission", "farmwelt.admin"))) {
                player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("no-permission"));
                return true;
            }

            if (worldUtils.worldExists(plugin.getWorldName())) {
                worldUtils.resetWorld(plugin.getWorldName());
                player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("admin-reset"));
            } else {
                player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("no-world"));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission(plugin.getConfig().getString("admin-permission", "farmwelt.admin"))) {
                player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("no-permission"));
                return true;
            }

            plugin.reloadConfig();
            player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("admin-reload"));
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            if (sender.hasPermission(plugin.getConfig().getString("admin-permission", "farmwelt.admin"))) {
                completions.add("reset");
                completions.add("reload");
            }
        }
        
        return completions;
    }
}