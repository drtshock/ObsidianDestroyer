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
        if (detonator == null || detonator.hasMetadata("ObbyEntity")) {
            return;
        }
        if (event.getLocation().getBlock().hasMetadata("ObbyEntity")) {
            return;
        }
        if (event.getYield() <= 0) {
            return;
        }

        ChunkManager.getInstance().handleExplosion(event);
    }
}
