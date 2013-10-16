package io.snw.obsidiandestroyer.listeners;

import io.snw.obsidiandestroyer.ObsidianDestroyer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    private ObsidianDestroyer plugin;

    public PlayerListener(ObsidianDestroyer p) {
        this.plugin = p;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (plugin.getNeedsUpdate()) {
            event.getPlayer().sendMessage("An update is available!");
        }
    }
}
