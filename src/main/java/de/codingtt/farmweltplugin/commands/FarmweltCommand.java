package de.codingtt.farmweltplugin.commands;

import de.codingtt.farmweltplugin.Main;
import de.codingtt.farmweltplugin.utils.WorldUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FarmweltCommand implements CommandExecutor, TabCompleter {
    Main plugin = Main.getInstance();
    WorldUtils worldUtils = new WorldUtils();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        // NO PLAYER

        if (!(sender instanceof Player)) {
            String noPlayerMessage = plugin.getColoredString("no-player");
            assert noPlayerMessage != null;
            sender.sendMessage(noPlayerMessage);
            return false;
        }
        Player player = (Player) sender;

        // NO PERMISSION

        String permissionString = plugin.getColoredString("default-permission");
        assert permissionString != null;
        if (!player.hasPermission(permissionString)) {
            String noPermissionMessage = plugin.getColoredString("no-permission");
            assert noPermissionMessage != null;
            player.sendMessage(plugin.getColoredString("prefix") + noPermissionMessage);
            return true;
        }

        // TELEPORT
        if (args.length == 0) {
            if (player.getLocation().getWorld() == worldUtils.core.getMVWorldManager().getMVWorld(plugin.getWorldName()).getCBWorld()) {
                String alreadyInWorldMessage = plugin.getColoredString("already-in-world");
                assert alreadyInWorldMessage != null;
                player.sendMessage(plugin.getColoredString("prefix") + alreadyInWorldMessage);
                return true;
            }

            if (worldUtils.wolrdExists(plugin.getWorldName())) {
                worldUtils.teleportToWorld(player, plugin.getWorldName());
                String teleportMessage = plugin.getColoredString("world-teleport");
                assert teleportMessage != null;
                player.sendMessage(plugin.getColoredString("prefix") + teleportMessage);
            }
        }

        // ADMIN

        if (args.length == 1) {
            String adminPermissionString = plugin.getColoredString("admin-permission");
            assert adminPermissionString != null;
            if (!player.hasPermission(adminPermissionString)) {
                String adminNoPermissionMessage = plugin.getColoredString("no-permission");
                assert adminNoPermissionMessage != null;
                player.sendMessage(plugin.getColoredString("prefix") + adminNoPermissionMessage);
                return false;
            }

            switch (args[0]) {
                case "reset":
                    worldUtils.resetWorld(plugin.getWorldName());
                    String resetMessage = plugin.getColoredString("admin-reset");
                    player.sendMessage(plugin.getColoredString("prefix") + resetMessage);
                    break;
                case "reload":
                    plugin.reloadConfig();
                    String reloadMessage = plugin.getColoredString("admin-relaod");
                    player.sendMessage(plugin.getColoredString("prefix") + reloadMessage);
                    break;
                default:
                    String usageMessage = plugin.getColoredString("admin-usage");
                    player.sendMessage(plugin.getColoredString("prefix") + usageMessage);
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String adminPermissionString = plugin.getColoredString("admin-permission");
            assert adminPermissionString != null;
            if (player.hasPermission(adminPermissionString) && args.length == 1) {
                completions.add("reset");
                completions.add("reload");
            }
        }
        return completions;
    }
}