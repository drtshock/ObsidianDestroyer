package io.snw.obsidiandestroyer.datatypes;

import io.snw.obsidiandestroyer.ObsidianDestroyer;
import io.snw.obsidiandestroyer.managers.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LiquidExplosion {

    private static int RADIUS = 1;
    private static int CANNON_RADIUS = 2;
    private static ArrayList<Material> FLUID_MATERIALS = new ArrayList<Material>();

    /**
     * Handles an explosion if it occurs from within a liquid. Cancels the explosion, removes any nearby liquids found,
     * and creates a new explosion in the cleared location. Hooks and checks explosion settings in: WorldGuard,
     * Factions, and Towny
     *
     * @param event EntityExplodeEvent
     * @param blocklist list of blocks handled
     */
    public static void Handle(EntityExplodeEvent event, LinkedList<Block> blocklist) {
        FLUID_MATERIALS.add(Material.WATER);
        FLUID_MATERIALS.add(Material.STATIONARY_WATER);
        FLUID_MATERIALS.add(Material.LAVA);
        FLUID_MATERIALS.add(Material.STATIONARY_LAVA);

        explosionInLiquid(event, blocklist);
    }

    /**
     * Creates a custom explosion in the liquid. Checks with other plugins to see if it has to cancel the event.
     *
     * @param event EntityExplodeEvent
     * @param blocklist list of blocks handled
     */
    private static void explosionInLiquid(EntityExplodeEvent event, List<Block> blocklist) {
        if (RADIUS <= 0 || event.isCancelled()) {
            return;
        }

        Entity entity = event.getEntity();
        boolean removeLiquids = false;
        int redstoneCount = 0;

        // Protects TNT cannons from exploding themselves
        if (ConfigManager.getInstance().getProtectTNTCannons()) {
            for (int x = -CANNON_RADIUS; x <= CANNON_RADIUS; x++) {
                for (int y = -CANNON_RADIUS; y <= CANNON_RADIUS; y++) {
                    for (int z = -CANNON_RADIUS; z <= CANNON_RADIUS; z++) {
                        Location targetLoc = new Location(entity.getWorld(), entity.getLocation().getX() + x, entity.getLocation().getY() + y, entity.getLocation().getZ() + z);

                        if (targetLoc.getBlock().getType().equals(Material.REDSTONE_WIRE) || targetLoc.getBlock().getType().equals(Material.DIODE_BLOCK_ON) || targetLoc.getBlock().getType().equals(Material.DIODE_BLOCK_OFF)) {
                            if (targetLoc.getBlock().getType().equals(Material.REDSTONE_WIRE) || targetLoc.getBlock().getType().equals(Material.DIODE_BLOCK_ON) || targetLoc.getBlock().getType().equals(Material.DIODE_BLOCK_OFF)) {
                                redstoneCount++;
                            }
                        }
                    }
                }
            }
            if (redstoneCount >= 6) {
                return;
            }
        }

        // Creates air where water used to be and sets up the boom if the explosion is from within a liquid
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = -RADIUS; y <= RADIUS; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    Location targetLoc = new Location(entity.getWorld(), entity.getLocation().getX() + x, entity.getLocation().getY() + y, entity.getLocation().getZ() + z);

                    // Replace any liquid blocks with air.
                    if (FLUID_MATERIALS.contains(targetLoc.getBlock().getType()) && targetLoc.getBlock().isLiquid()) {
                        if (!removeLiquids) {
                            removeLiquids = true;
                        }
                        event.blockList().add(targetLoc.getBlock());
                    } else if (entity.getLocation().getBlock().isLiquid()) {
                        if (!removeLiquids) {
                            removeLiquids = true;
                        }
                        if (!event.blockList().contains(targetLoc)) {
                            event.blockList().add(targetLoc.getBlock());
                            blocklist.add(targetLoc.getBlock());
                        }
                    }
                }
            }
        }

        // Adds to events blocklist and sets metadata flag
        if (removeLiquids) {
            event.blockList().add(event.getLocation().getBlock());
            blocklist.add(event.getLocation().getBlock());
            event.getEntity().setMetadata("LiquidEntity", new FixedMetadataValue(ObsidianDestroyer.getInstance(), new EntityData(event.getEntityType())));
        }
    }
}
