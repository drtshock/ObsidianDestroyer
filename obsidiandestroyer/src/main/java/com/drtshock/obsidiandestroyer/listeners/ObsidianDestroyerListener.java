package com.drtshock.obsidiandestroyer.listeners;

import com.drtshock.obsidiandestroyer.events.DurabilityDamageEvent;
import com.drtshock.obsidiandestroyer.managers.ConfigManager;
import org.bukkit.Effect;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ObsidianDestroyerListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    void onDurabilityDamageEvent(DurabilityDamageEvent event) {
        if (ConfigManager.getInstance().getEffectsEnabled() && event.getLocation() != null) {
            // display particles effects on damage
            final double random = Math.random();
            if (random <= ConfigManager.getInstance().getEffectsChance()) {
                event.getLocation().getWorld().playEffect(event.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
            }
        }
    }
}
