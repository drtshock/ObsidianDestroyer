package com.drtshock.obsidiandestroyer.listeners;

import com.drtshock.obsidiandestroyer.ObsidianDestroyer;
import com.drtshock.obsidiandestroyer.managers.ChunkManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Bat;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpigotListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(BlockExplodeEvent event) {
        if (event == null || ChunkManager.getInstance().getDisabledWorlds().contains(event.getBlock().getLocation().getWorld().getName())) {
            return; // do not do anything in case explosions get cancelled
        }

        final Block detonatorBlock = event.getBlock();

        if (detonatorBlock == null) {
            return;
        }
        if (detonatorBlock.hasMetadata("ObbyEntity")) {
            return;
        }
        if (event.getYield() <= 0) {
            return;
        }

        // feeling batty?! Spawn a bat to tie onto the EntityExplodeEvent.
        try {
            Bat bat = (Bat) Bukkit.getWorld(detonatorBlock.getWorld().getName()).spawnEntity(detonatorBlock.getLocation(), EntityType.BAT);
            if (bat != null) {
                bat.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 1), true);
            }
            // Construct a new event but don't call it.
            EntityExplodeEvent entityExplodeEvent = new EntityExplodeEvent(bat, event.getBlock().getLocation(), event.blockList(), event.getYield());
            ChunkManager.getInstance().handleExplosion(entityExplodeEvent, event.getBlock().getLocation());
            if (bat != null) {
                bat.remove(); // bye
            }
        } catch (Exception e) {
            ObsidianDestroyer.debug(e.toString());
        }
    }
}