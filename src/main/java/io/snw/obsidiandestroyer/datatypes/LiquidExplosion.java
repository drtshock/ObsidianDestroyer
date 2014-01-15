package io.snw.obsidiandestroyer.datatypes;

import io.snw.obsidiandestroyer.ObsidianDestroyer;
import io.snw.obsidiandestroyer.managers.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;

public class LiquidExplosion {

    private static int RADIUS = (int) Math.round(ConfigManager.getInstance().getRadius() * 0.51);
    private static int CANNON_RADIUS = 2;

    /**
     * Handles an explosion if it occurs from within or around a liquid.
     * Adds liquids to the event blocklist
     * Adds blocks near liquids to the event blocklist
     *
     * @param event EntityExplodeEvent
     * @param blocklist list of blocks handled
     */
    public static void handle(EntityExplodeEvent event, List<Block> blocklist) {
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

        // Adds liquids and blocks near them to the event listings
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = -RADIUS; y <= RADIUS; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    Location targetLoc = new Location(entity.getWorld(), entity.getLocation().getX() + x, entity.getLocation().getY() + y, entity.getLocation().getZ() + z);
                    if (isNearLiquid(targetLoc)) {
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

        // Sets metadata flag
        if (removeLiquids) {
            event.getEntity().setMetadata("LiquidEntity", new FixedMetadataValue(ObsidianDestroyer.getInstance(), new EntityData(event.getEntityType())));
        }
    }

    private static final boolean isNearLiquid(Location location) {
        for (BlockFace face : BlockFace.values()) {
            switch (face) {
                case NORTH:
                case EAST:
                case SOUTH:
                case WEST:
                case UP:
                case DOWN:
                case SELF:
                    if (location.getBlock().getRelative(face) != null && location.getBlock().getRelative(face).isLiquid()) {
                        return true;
                    }
                    break;
                default:
                    break;
            }
        }
        return false;
    }
}
