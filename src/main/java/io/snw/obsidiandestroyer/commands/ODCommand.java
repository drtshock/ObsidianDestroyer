package io.snw.obsidiandestroyer.commands;

import io.snw.obsidiandestroyer.ObsidianDestroyer;
import io.snw.obsidiandestroyer.managers.ChunkManager;
import io.snw.obsidiandestroyer.managers.ConfigManager;
import io.snw.obsidiandestroyer.managers.MaterialManager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ODCommand implements CommandExecutor {

    private ObsidianDestroyer plugin;

    public ODCommand(ObsidianDestroyer p) {
        this.plugin = p;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
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
            } else if (command.equalsIgnoreCase("reloadDB") || command.equalsIgnoreCase("reloadDataBase")) {

                if (sender.hasPermission("obsidiandestroyer.config.reloadDurability")) {
                    reloadDurabilites(sender);
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

    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "--------------------------------------------------");
        sender.sendMessage(ChatColor.DARK_PURPLE + "ObsidianDestroyer " + ChatColor.LIGHT_PURPLE + "v" + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.DARK_PURPLE + "Available commands:");
        sender.sendMessage(ChatColor.DARK_PURPLE + "/od version - gives version and shows commands.");
        sender.sendMessage(ChatColor.DARK_PURPLE + "/od reload - " + ChatColor.LIGHT_PURPLE + "reloads the plugin's config file.");
        sender.sendMessage(ChatColor.DARK_PURPLE + "/od reloadDB - " + ChatColor.LIGHT_PURPLE + "reloads the durability database.");
        sender.sendMessage(ChatColor.DARK_PURPLE + "/od reset - " + ChatColor.LIGHT_PURPLE + " reset all durability damage and timers.");
    }

    private void reloadDurabilites(CommandSender sender) {
        long time = System.currentTimeMillis();
        ChunkManager.getInstance().save();
        ChunkManager.getInstance().load();
        sender.sendMessage(ChatColor.GREEN + "Reloading ObsidianDestroyer database complete in " + (System.currentTimeMillis() - time) + " ms!");
    }

    private void reloadPlugin(CommandSender sender) {
        long time = System.currentTimeMillis();
        new ConfigManager();
        MaterialManager.getInstance().load();
        ChunkManager.getInstance().loadDisabledWorlds();
        sender.sendMessage(ChatColor.GREEN + "Reloading ObsidianDestroyer config complete in " + (System.currentTimeMillis() - time) + " ms!");
    }

    private void resetDurability(CommandSender sender) {
        long time = ChunkManager.getInstance().resetAllDurabilities();
        sender.sendMessage(ChatColor.GREEN + "Reset all Material durabilities in " + (System.currentTimeMillis() - time) + " ms.");
    }
}
