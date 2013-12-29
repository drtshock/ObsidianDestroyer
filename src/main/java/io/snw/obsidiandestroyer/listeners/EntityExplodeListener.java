package io.snw.obsidiandestroyer.listeners;

import io.snw.obsidiandestroyer.managers.ChunkManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import at.pavlov.cannons.event.ProjectilePiercingEvent;

public class EntityExplodeListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event == null || ChunkManager.getInstance().getDisabledWorlds().contains(event.getLocation().getWorld().getName())) {
            return; // do not do anything in case explosions get cancelled
        }

        ChunkManager.getInstance().handleExplosion(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectilePiercing(ProjectilePiercingEvent event) {
        if (event == null || ChunkManager.getInstance().getDisabledWorlds().contains(event.getImpactLocation().getWorld().getName())) {
            return; // do not do anything in case explosions get cancelled
        }

        ChunkManager.getInstance().handleExplosion(event);
    }
}
