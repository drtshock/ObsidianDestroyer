package com.drtshock.obsidiandestroyer.listeners;

import com.drtshock.obsidiandestroyer.managers.ChunkManager;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class EntityExplodeListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event == null || ChunkManager.getInstance().getDisabledWorlds().contains(event.getLocation().getWorld().getName())) {
            return; // do not do anything in case explosions get cancelled
        }

        final Entity detonator = event.getEntity();

        if (detonator == null) {
            // some other plugins create new explosions passing 'null' as
            // Entity, so we need this here to fix it
            return;
        }

        if (detonator.hasMetadata("ObbyEntity")) {
            return;
        }

        ChunkManager.getInstance().handleExplosion(event);
    }
}
