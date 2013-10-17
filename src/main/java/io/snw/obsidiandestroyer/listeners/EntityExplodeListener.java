package io.snw.obsidiandestroyer.listeners;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.snw.obsidiandestroyer.LiquidExplosion;
import io.snw.obsidiandestroyer.ObsidianDestroyer;
import io.snw.obsidiandestroyer.managers.BlockManager;
import io.snw.obsidiandestroyer.managers.ConfigManager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class EntityExplodeListener implements Listener {

    @SuppressWarnings("unused")
    private ObsidianDestroyer plugin;

    public EntityExplodeListener(ObsidianDestroyer p) {
        this.plugin = p;
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        // do not do anything in case explosions get canceled
        if (event == null || event.isCancelled()) {
            return;
        }

        if (ConfigManager.getInstance().getDisabledWorlds().contains(event.getLocation().getWorld().getName())) {
            return;
        }

        final int radius = ConfigManager.getInstance().getRadius();

        // cancel if radius is < 0
        if (radius < 0) {
            ObsidianDestroyer.LOG.warning("Explosion radius is less than zero. Current value: " + radius);
            return;
        }

        final Entity detonator = event.getEntity();

        if (detonator == null) {
            // some other plugins create new explosions passing 'null' as
            // Entity, so we need this here to fix it
            return;
        }

        final Location detonatorLoc = detonator.getLocation();
        final String eventTypeRep = event.getEntity().toString();
        //ObsidianDestroyer.LOG.info("EventTypeRep: " + eventTypeRep);

        // List of blocks that will be removed from the blocklist
        List<Block> blocksToBeRemoved = new ArrayList<Block>();

        // Hook into cannons... (somehow)
        // TODO: Hook into cannons again.
        if (eventTypeRep.equals("CraftSnowball")) {
            List<Location> hitLocs = new ArrayList<Location>();
            Iterator<Block> iter = event.blockList().iterator();
            while (iter.hasNext()) {
                Block block = iter.next();
                hitLocs.add(block.getLocation());
                if (hitLocs.contains(block.getLocation())) {
                    continue;
                }
                if (BlockManager.getInstance().blowBlockUp(block, event) != null) {
                    blocksToBeRemoved.add(block);
                }
            }
        }

        // Liquid override
        if (ConfigManager.getInstance().getExplodeInLiquids()) {
            LiquidExplosion.Handle(event);
        }

        // Check explosion blocks
        for (Block block : event.blockList()) {
            if ((detonatorLoc.getBlock().isLiquid()) && (ConfigManager.getInstance().getWaterProtection())) {
                return;
            }
            if (BlockManager.getInstance().blowBlockUp(block, event) != null) {
                blocksToBeRemoved.add(block);
            }
        }

        // For materials that are not normally destructible.
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location targetLoc = new Location(detonator.getWorld(), detonatorLoc.getX() + x, detonatorLoc.getY() + y, detonatorLoc.getZ() + z);
                    if (detonatorLoc.distance(targetLoc) <= radius) {
                        if (blocksToBeRemoved.contains(targetLoc.getBlock())) {
                            continue;
                        }
                        if (!BlockManager.getInstance().contains(targetLoc.getBlock().getType().name()) || targetLoc.getBlock().getType() == Material.AIR) {
                            continue;
                        }
                        if ((detonatorLoc.getBlock().isLiquid()) && (ConfigManager.getInstance().getWaterProtection())) {
                            return;
                        }
                        if (BlockManager.getInstance().blowBlockUp(targetLoc.getBlock(), event) != null) {
                            blocksToBeRemoved.add(targetLoc.getBlock());
                        }
                    }
                }
            }
        }

        // Remove managed blocks
        for (Block block : blocksToBeRemoved) {
            event.blockList().remove(block);
        }
    }
}
