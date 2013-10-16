package io.snw.obsidiandestroyer.listeners;

import io.snw.obsidiandestroyer.ObsidianDestroyer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class EntityExplodeListener implements Listener {

    private ObsidianDestroyer plugin;

    public EntityExplodeListener(ObsidianDestroyer p) {
        this.plugin = p;
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {

    }
}
