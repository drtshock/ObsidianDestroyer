package com.drtshock.obsidiandestroyer.listeners;

import com.drtshock.obsidiandestroyer.managers.ChunkManager;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class SpigotListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(BlockExplodeEvent event) {
        if (event == null || ChunkManager.getInstance().getDisabledWorlds().contains(event.getBlock().getLocation().getWorld().getName())) {
            return; // do not do anything in case explosions get cancelled
        }

        final Block detonator = event.getBlock();

        if (detonator == null) {
            // some other plugins create new explosions passing 'null' as
            // Entity, so we need this here to fix it
            return;
        }

        if (detonator.hasMetadata("ObbyEntity")) {
            return;
        }

        // Construct a new event but don't call it.
        EntityExplodeEvent entityExplodeEvent = new EntityExplodeEvent(null, event.getBlock().getLocation(), event.blockList(), 0.3F);
        ChunkManager.getInstance().handleExplosion(entityExplodeEvent, event.getBlock().getLocation());
    }
}