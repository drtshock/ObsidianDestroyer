package io.snw.obsidiandestroyer.io.snw.obsidiandestroyer.commands;

import io.snw.obsidiandestroyer.ObsidianDestroyer;
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
        return true;
    }
}
