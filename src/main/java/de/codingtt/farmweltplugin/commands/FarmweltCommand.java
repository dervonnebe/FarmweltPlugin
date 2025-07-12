package de.codingtt.farmweltplugin.commands;

import de.codingtt.farmweltplugin.Main;
import de.codingtt.farmweltplugin.utils.FarmweltMenu;
import de.codingtt.farmweltplugin.utils.WorldUtils;
import org.bukkit.Location;
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
    private final FarmweltMenu farmweltMenu;

    public FarmweltCommand(Main plugin, WorldUtils worldUtils, FarmweltMenu farmweltMenu) {
        this.plugin = plugin;
        this.worldUtils = worldUtils;
        this.farmweltMenu = farmweltMenu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("command-player-only"));
            return true;
        }

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

        if (args[0].equalsIgnoreCase("menu")) {
            if (!player.hasPermission(plugin.getConfig().getString("default-permission", "farmwelt.use"))) {
                player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("no-permission"));
                return true;
            }
            
            farmweltMenu.openMenu(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            if (!player.hasPermission(plugin.getConfig().getString("admin-permission", "farmwelt.admin"))) {
                player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("no-permission"));
                return true;
            }

            if (args.length >= 2) {
                String worldType = args[1].toLowerCase();
                
                if (worldType.equals("normal")) {
                    if (worldUtils.worldExists(plugin.getWorldName())) {
                        worldUtils.resetWorld(plugin.getWorldName());
                        player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("admin-reset-normal"));
                    } else {
                        player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("no-world"));
                    }
                } else if (worldType.equals("nether")) {
                    if (worldUtils.worldExists(plugin.getNetherWorldName())) {
                        worldUtils.resetWorld(plugin.getNetherWorldName());
                        player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("admin-reset-nether"));
                    } else {
                        player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("no-nether-world"));
                    }
                } else if (worldType.equals("end")) {
                    if (worldUtils.worldExists(plugin.getEndWorldName())) {
                        worldUtils.resetWorld(plugin.getEndWorldName());
                        player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("admin-reset-end"));
                    } else {
                        player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("no-end-world"));
                    }
                } else {
                    player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("invalid-world-type"));
                }
                
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

        if (args[0].equalsIgnoreCase("back")) {
            if (!plugin.isBackEnabled()) {
                player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("back-disabled"));
                return true;
            }
            Location backLoc = plugin.getBackLocation(player.getUniqueId());
            if (backLoc != null) {
                player.teleport(backLoc);
                plugin.clearBackLocation(player.getUniqueId());
                player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("back-success"));
            } else {
                player.sendMessage(plugin.getLanguageString("prefix") + plugin.getLanguageString("back-no-location"));
            }
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("menu");
            if (plugin.isBackEnabled()) {
                completions.add("back");
            }
            if (sender.hasPermission(plugin.getConfig().getString("admin-permission", "farmwelt.admin"))) {
                completions.add("reset");
                completions.add("reload");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("reset") && 
                sender.hasPermission(plugin.getConfig().getString("admin-permission", "farmwelt.admin"))) {
            completions.add("normal");
            if (plugin.getConfig().getBoolean("menu.nether-world.enabled", false)) {
                completions.add("nether");
            }
            if (plugin.getConfig().getBoolean("menu.end-world.enabled", false)) {
                completions.add("end");
            }
        }
        
        return completions;
    }
}