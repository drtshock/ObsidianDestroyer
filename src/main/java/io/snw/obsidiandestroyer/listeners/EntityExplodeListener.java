package io.snw.obsidiandestroyer.listeners;

import io.snw.obsidiandestroyer.managers.ChunkManager;
import io.snw.obsidiandestroyer.managers.ConfigManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class EntityExplodeListener implements Listener {

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        // do not do anything in case explosions get canceled
        if (event == null || (event.isCancelled() && !ConfigManager.getInstance().getIgnoreCancel())) {
            return;
        }

        if (ChunkManager.getInstance().getDisabledWorlds().contains(event.getLocation().getWorld().getName())) {
            return;
        }

        ChunkManager.getInstance().handleExplosion(event);
    }
}
