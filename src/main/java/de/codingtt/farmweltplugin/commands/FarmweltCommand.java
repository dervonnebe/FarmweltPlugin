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
            sender.sendMessage(plugin.getColoredString("prefix") + "§cDieser Befehl kann nur von Spielern ausgeführt werden!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            if (!player.hasPermission("farmwelt.use")) {
                player.sendMessage(plugin.getColoredString("prefix") + "§cDu hast keine Berechtigung dazu!");
                return true;
            }

            if (worldUtils.worldExists(plugin.getWorldName())) {
                worldUtils.teleportToWorld(player, plugin.getWorldName());
                player.sendMessage(plugin.getColoredString("prefix") + "§aDu wurdest zur Farmwelt teleportiert!");
            } else {
                player.sendMessage(plugin.getColoredString("prefix") + "§cDie Farmwelt existiert nicht!");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            if (!player.hasPermission("farmwelt.admin")) {
                player.sendMessage(plugin.getColoredString("prefix") + "§cDu hast keine Berechtigung dazu!");
                return true;
            }

            if (worldUtils.worldExists(plugin.getWorldName())) {
                worldUtils.resetWorld(plugin.getWorldName());
                player.sendMessage(plugin.getColoredString("prefix") + "§aDie Farmwelt wurde zurückgesetzt!");
            } else {
                player.sendMessage(plugin.getColoredString("prefix") + "§cDie Farmwelt existiert nicht!");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("farmwelt.admin")) {
                player.sendMessage(plugin.getColoredString("prefix") + "§cDu hast keine Berechtigung dazu!");
                return true;
            }

            plugin.reloadConfig();
            player.sendMessage(plugin.getColoredString("prefix") + "§aKonfiguration neu geladen!");
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            if (sender.hasPermission("farmwelt.admin")) {
                completions.add("reset");
                completions.add("reload");
            }
        }
        
        return completions;
    }
}