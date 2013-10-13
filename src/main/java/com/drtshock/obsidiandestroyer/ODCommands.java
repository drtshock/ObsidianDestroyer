/* 
 * Copyright (C) 2013 drtshock
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.drtshock.obsidiandestroyer;

import java.util.HashMap;
import java.util.Set;
import java.util.Timer;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author drtshock
 */
public final class ODCommands implements CommandExecutor {

    private ObsidianDestroyer plugin;

    public ODCommands(ObsidianDestroyer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        if (args.length == 0) {

            if (sender.hasPermission("obsidiandestroyer.help")) {
                showHelp(sender);
            } else {
                sender.sendMessage(ChatColor.RED + "You are not authorized to use this command.");
            }
        } else if (args.length == 1) {
            // commands with 0 arguments
            String command = args[0];

            if (command.equalsIgnoreCase("reload")) {

                if (sender.hasPermission("obsidiandestroyer.config.reload")) {
                    reloadPlugin(sender);
                } else {
                    sender.sendMessage(ChatColor.RED + "You are not authorized to use this command.");
                }
            } else if (command.equalsIgnoreCase("info")) {

                if (sender.hasPermission("obsidiandestroyer.config.info")) {
                    getConfigInfo(sender);
                } else {
                    sender.sendMessage(ChatColor.RED + "You are not authorized to use this command.");
                }
            } else if (command.equalsIgnoreCase("reset")) {

                if (sender.hasPermission("obsidiandestroyer.durability.reset")) {
                    resetDurability(sender);
                } else {
                    sender.sendMessage(ChatColor.RED + "You are not authorized to use this command.");
                }
            } else if (command.equalsIgnoreCase("version")) {
                if (sender.hasPermission("obsidiandestroyer.help")) {
                    sender.sendMessage(ChatColor.DARK_PURPLE + "ObsidianDestroyer version: " + ChatColor.GRAY + this.plugin.getDescription().getVersion());
                }
            }
        }

        return true;
    }

    // Removed all isOp checks as permissions default to op now.
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_PURPLE + "ObsidianDestroyer " + ChatColor.LIGHT_PURPLE + "v" + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.DARK_PURPLE + "Available commands:");
        sender.sendMessage(ChatColor.DARK_PURPLE + "/od version - gives version and shows commands.");
        sender.sendMessage(ChatColor.DARK_PURPLE + "/od reload - " + ChatColor.LIGHT_PURPLE + "reloads the plugin's config file.");
        sender.sendMessage(ChatColor.DARK_PURPLE + "/od info - " + ChatColor.LIGHT_PURPLE + " shows the currently loaded config.");
        sender.sendMessage(ChatColor.DARK_PURPLE + "/od reset - " + ChatColor.LIGHT_PURPLE + " reset all durability timers.");

    }

    private void reloadPlugin(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "Reloading ObsidianDestroyer config!");
        this.plugin.getODConfig().reloadConfig();
    }

    private void getConfigInfo(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_PURPLE + "Currently loaded config of ObsidianDestroyer:");
        sender.sendMessage(ChatColor.DARK_PURPLE + "---------------------------------------------");

        if (this.plugin.getODConfig().getConfigFile().exists()) {
            for (String s : this.plugin.getODConfig().getConfigList()) {
                sender.sendMessage(s);
            }
        } else {
            sender.sendMessage(ChatColor.RED + "None - Config file deleted - please reload");
        }
    }

    private void resetDurability(CommandSender sender) {
        ODEntityListener listener = this.plugin.getListener();

        listener.setObsidianDurability(new HashMap<Integer, Integer>());

        Set<Integer> set = listener.getObsidianTimer().keySet();

        for (Integer i : set) {
            Timer t = listener.getObsidianTimer().get(i);

            if (t != null) {
                t.cancel();
            }
        }

        listener.setObsidianTimer(new HashMap<Integer, Timer>());

        sender.sendMessage(ChatColor.GREEN + "Reset all Obsidian durabilities!");
    }
}
