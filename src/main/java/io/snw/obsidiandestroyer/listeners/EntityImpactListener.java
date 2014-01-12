package io.snw.obsidiandestroyer.listeners;

import io.snw.obsidiandestroyer.managers.ChunkManager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import at.pavlov.cannons.event.ProjectileImpactEvent;
import at.pavlov.cannons.event.ProjectilePiercingEvent;

public class EntityImpactListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onProjectilePiercing(ProjectilePiercingEvent event) {
        if (event == null || ChunkManager.getInstance().getDisabledWorlds().contains(event.getImpactLocation().getWorld().getName())) {
            return; // do not do anything in case explosions get cancelled
        }

        ChunkManager.getInstance().handlePiercing(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileImpact(ProjectileImpactEvent event) {
        if (event == null || ChunkManager.getInstance().getDisabledWorlds().contains(event.getImpactLocation().getWorld().getName())) {
            return; // do not do anything in case explosions get cancelled
        }

        ChunkManager.getInstance().handleImpact(event);
    }
}
