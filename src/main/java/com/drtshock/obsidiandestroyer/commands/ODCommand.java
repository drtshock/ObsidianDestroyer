package com.drtshock.obsidiandestroyer.commands;

import com.drtshock.obsidiandestroyer.ObsidianDestroyer;
import com.drtshock.obsidiandestroyer.managers.ChunkManager;
import com.drtshock.obsidiandestroyer.managers.ConfigManager;
import com.drtshock.obsidiandestroyer.managers.MaterialManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

public class ODCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);

        } else if (args.length == 1) {
            // commands with 0 arguments
            String command = args[0];

            if (command.equalsIgnoreCase("reload")) {
                reloadPlugin(sender);

            } else if (command.equalsIgnoreCase("reloadDB") || command.equalsIgnoreCase("reloadDataBase")) {
                reloadDurabilites(sender);

            } else if (command.equalsIgnoreCase("reset")) {
                resetDurability(sender);

            } else if (command.equalsIgnoreCase("version")) {
                sender.sendMessage(ChatColor.DARK_PURPLE + "ObsidianDestroyer version: " + ChatColor.GRAY + ObsidianDestroyer.getInstance().getDescription().getVersion());

            } else {
                return false;
            }
        }

        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "" + ChatColor.STRIKETHROUGH + "                                                             ");
        sender.sendMessage(ChatColor.DARK_PURPLE + "ObsidianDestroyer " + ChatColor.LIGHT_PURPLE + "v" + ObsidianDestroyer.getInstance().getDescription().getVersion());
        sender.sendMessage(ChatColor.DARK_PURPLE + "Available commands:");
        sender.sendMessage(ChatColor.DARK_PURPLE + "/od version - " + ChatColor.LIGHT_PURPLE + "gives version and shows commands.");
        sender.sendMessage(ChatColor.DARK_PURPLE + "/od reload - " + ChatColor.LIGHT_PURPLE + "reloads the plugin's config file.");
        sender.sendMessage(ChatColor.DARK_PURPLE + "/od reloadDB - " + ChatColor.LIGHT_PURPLE + "reloads the durability database.");
        sender.sendMessage(ChatColor.DARK_PURPLE + "/od reset - " + ChatColor.LIGHT_PURPLE + " reset all durability damage and timers.");
        sender.sendMessage(ChatColor.GREEN + "" + ChatColor.STRIKETHROUGH + "                                                             ");
    }

    private void reloadDurabilites(CommandSender sender) {
        long time = System.currentTimeMillis();
        ChunkManager.getInstance().save();
        ChunkManager.getInstance().load();
        sender.sendMessage(ChatColor.GREEN + "Reloading ObsidianDestroyer database completed in " + (System.currentTimeMillis() - time) + " ms!");
    }

    private void reloadPlugin(CommandSender sender) {
        long time = System.currentTimeMillis();
        try {
            ConfigManager.getInstance().backup(false);
            ConfigManager.getInstance().reload();
            if (ConfigManager.getInstance().isLoaded()) {
                MaterialManager.getInstance().load();
                ChunkManager.getInstance().loadDisabledWorlds();
                sender.sendMessage(ChatColor.GREEN + "Reloading ObsidianDestroyer config completed in " + (System.currentTimeMillis() - time) + " ms!");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        new ConfigManager(true).backup(true);
        MaterialManager.getInstance().load();
        ChunkManager.getInstance().loadDisabledWorlds();
        ObsidianDestroyer.LOG.log(Level.SEVERE, "The config has encountered an error on load. Recovered a backup from memory...");
        sender.sendMessage(ChatColor.RED + "Reloading ObsidianDestroyer config failed, restored from memory. See log file.  Completed in " + (System.currentTimeMillis() - time) + " ms!");
    }

    private void resetDurability(CommandSender sender) {
        long time = ChunkManager.getInstance().resetAllDurabilities();
        sender.sendMessage(ChatColor.GREEN + "Reset all Material durabilities in " + (System.currentTimeMillis() - time) + " ms.");
    }
}
