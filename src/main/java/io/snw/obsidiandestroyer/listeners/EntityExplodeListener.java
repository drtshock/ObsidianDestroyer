package io.snw.obsidiandestroyer.listeners;

import io.snw.obsidiandestroyer.managers.ChunkManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class EntityExplodeListener implements Listener {

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event == null || (event.isCancelled()) || ChunkManager.getInstance().getDisabledWorlds().contains(event.getLocation().getWorld().getName())) {
            return; // do not do anything in case explosions get cancelled
        }

        ChunkManager.getInstance().handleExplosion(event);
    }
}
