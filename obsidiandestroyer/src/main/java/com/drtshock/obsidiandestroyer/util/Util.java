package com.drtshock.obsidiandestroyer.util;

import com.drtshock.obsidiandestroyer.managers.ConfigManager;
import com.drtshock.obsidiandestroyer.managers.HookManager;
import com.drtshock.obsidiandestroyer.managers.MaterialManager;
import com.drtshock.obsidiandestroyer.managers.factions.FactionsIntegration;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Util {

    public static boolean isSolid(Material material) {
        return material.isSolid();
    }

    public static int getMaxDistance(String material, int data, int defaultRadius) {
        int dist = MaterialManager.getInstance().getBlastRadius(material, data);
        if (dist <= 0) {
            dist = defaultRadius;
        }
        return dist;
    }

    @Deprecated
    public static boolean checkIfMax(int value, String id, int data) {
        return value >= MaterialManager.getInstance().getDurability(id, data);
    }

    public static boolean checkIfMax(int value, String id, int data, double multi) {
        return value >= Math.round(MaterialManager.getInstance().getDurability(id, data) * multi);
    }

    public static boolean checkIfOverMax(int value, String id, int data, double multi) {
        final int du = MaterialManager.getInstance().getDurability(id, data);
        return value > Math.round((du * multi) + (du * 0.18));
    }

    public static boolean isNearLiquid(Location location) {
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

    public static boolean isTargetsPathBlocked(Location tLoc, Location dLoc, boolean useOnlyMaterialListing) {

        // check world
        if (!dLoc.getWorld().getName().equalsIgnoreCase(tLoc.getWorld().getName())) {
            return false;
        }

        // if the distance is too close... the path is not blocked ;)
        if (dLoc.distance(tLoc) <= 0.9) {
            return false;
        }

        // try to iterate through blocks between dLoc and tLoc
        try {
            // Create a vector block trace from the detonator location to damaged block's location
            final BlockIterator blocksInPath = new BlockIterator(tLoc.getWorld(), dLoc.toVector(), tLoc.toVector().subtract(dLoc.toVector()).normalize(), 0.5, (int) dLoc.distance(tLoc));

            // iterate through the blocks in the path
            int over = 0; // prevents rare case of infinite loop and server crash
            while (blocksInPath.hasNext() && over < 128) {
                over++;
                // the next block
                final Block block = blocksInPath.next();
                if (block == null) {
                    continue;
                }
                // check if next block is the target block
                if (tLoc.getWorld().getName().equals(block.getWorld().getName()) &&
                        tLoc.getBlockX() == block.getX() &&
                        tLoc.getBlockY() == block.getY() &&
                        tLoc.getBlockZ() == block.getZ()) {
                    // ignore target block
                    continue;
                }

                // check if the block material is being handled
                if (useOnlyMaterialListing) {
                    // only handle for certain case as to not interfere with all explosions
                    if (MaterialManager.getInstance().contains(block.getType().name(), block.getData())) {
                        return true;
                    } else {
                        continue;
                    }
                }
                // check if the block material is a solid
                if (!isNonSolid(block.getType())) {
                    return true;
                }
            }
        } catch (Exception e) {
            // ignore the error and return no targets in path
        }
        return false;
    }

    public static List<Location> getTargetsPathBlocked(Location tLoc, Location dLoc, boolean useOnlyMaterialListing) {
        return getTargetsPathBlocked(tLoc, dLoc, useOnlyMaterialListing, false);
    }

    public static List<Location> getTargetsPathBlocked(Location tLoc, Location dLoc, boolean useOnlyMaterialListing, boolean ignoreFirstZone) {
        final ArrayList<Location> tagetsInPath = new ArrayList<Location>();

        // check world
        if (!dLoc.getWorld().getName().equalsIgnoreCase(tLoc.getWorld().getName())) {
            return tagetsInPath;
        }

        // if the distance is too close... the path is not blocked ;)
        if (dLoc.distance(tLoc) <= 0.9) {
            return tagetsInPath;
        }

        // try to iterate through blocks between dLoc and tLoc
        try {
            // Create a vector block trace from the detonator location to damaged block's location
            final BlockIterator blocksInPath = new BlockIterator(tLoc.getWorld(), dLoc.toVector(), tLoc.toVector().subtract(dLoc.toVector()).normalize(), 0.5, (int) dLoc.distance(tLoc));

            // iterate through the blocks in the path
            int i = ConfigManager.getInstance().getRadius() + 1;
            int over = 0; // prevents rare case of infinite loop and server crash
            while (blocksInPath.hasNext() && over < 128) {
                if (i > 0) {
                    i--;
                } else {
                    break;
                }
                over++;

                // the next block
                final Block block = blocksInPath.next();
                if (block == null) {
                    continue;
                }
                // Ignore first blocks next to explosion
                if (ignoreFirstZone && ((i >= ConfigManager.getInstance().getRadius() - 1) || (dLoc.distance(tLoc) <= 1.5))) {
                    if (i >= ConfigManager.getInstance().getRadius() - 1 && MaterialManager.getInstance().contains(block.getType().name(), block.getData())) {
                        tagetsInPath.add(block.getLocation());
                    }
                    continue;
                }
                // check if next block is the target block
                if (tLoc.getWorld().getName().equals(block.getWorld().getName()) &&
                        tLoc.getBlockX() == block.getX() &&
                        tLoc.getBlockY() == block.getY() &&
                        tLoc.getBlockZ() == block.getZ()) {
                    // ignore target block
                    continue;
                }

                // check if the block material is being handled
                if (useOnlyMaterialListing) {
                    // only handle for certain case as to not interfere with all explosions
                    if (MaterialManager.getInstance().contains(block.getType().name(), block.getData())) {
                        tagetsInPath.add(block.getLocation());
                        break;
                    } else {
                        continue;
                    }
                }
                // check if the block material is a solid
                if (!isNonSolid(block.getType())) {
                    tagetsInPath.add(block.getLocation());
                    break;
                }
            }
        } catch (Exception e) {
            // ignore the error and return no targets in path
        }
        return tagetsInPath;
    }

    public static double getMultiplier(Location location) {
        if (!HookManager.getInstance().isFactionsFound()) {
            return 1D;
        }
        if (!FactionsIntegration.isUsing()) {
            return 1D;
        }

        double value;
        if (FactionsIntegration.get().isExplosionsEnabled(location)) {
            value = 1D;
        } else {
            return 0;
        }
        if (ConfigManager.getInstance().getUseFactionsPowerLevel() && FactionsIntegration.get().isFactionAtPower(location)) {
            if (!MaterialManager.getInstance().getBypassFactionsProtection(location.getBlock().getType().name(), location.getBlock().getData())) {
                return 0;
            }
        }
        if (ConfigManager.getInstance().getHandleOfflineFactions()) {
            if (FactionsIntegration.get().isFactionOffline(location)) {
                if (ConfigManager.getInstance().getProtectOfflineFactions()) {
                    return 0;
                }
                value = ConfigManager.getInstance().getOfflineFactionsDurabilityMultiplier();
            }
        }
        if (ConfigManager.getInstance().getHandleOnlineFactions()) {
            if (!FactionsIntegration.get().isFactionOffline(location)) {
                value = ConfigManager.getInstance().getOnlineFactionsDurabilityMultiplier();
            }
        }
        if (value < 0) {
            value = 1D;
        }

        return value;
    }

    public static String header() {
        return ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "ObsidianDestroyer" + ChatColor.DARK_AQUA + "] " + ChatColor.RESET;
    }

    public static int getRandomNumberFrom(int min, int max) {
        Random foo = new Random();
        return foo.nextInt((max + 1) - min) + min;
    }

    public static boolean isNonSolid(Material type) {
        return !type.isSolid();
    }

    public static boolean matchBlocksToLocations(List<Location> list1, List<Block> list2) {
        for (Location location : list1) {
            if (list2.contains(location.getBlock())) {
                return true;
            }
        }
        for (Block block : list2) {
            if (list1.contains(block.getLocation())) {
                return true;
            }
        }
        return false;
    }

    public static boolean matchLocationsToLocations(List<Location> list1, List<Location> list2) {
        for (Location location : list1) {
            if (list2.contains(location)) {
                return true;
            }
        }
        for (Location location : list2) {
            if (list1.contains(location)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSand(Block block) {
        for (String material : ConfigManager.getInstance().getSandMaterials()) {
            if (block.getType().name().equals(material)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRedstoneMaterial(Block block) {
        for (String material : ConfigManager.getInstance().getRedstoneMaterials()) {
            if (block.getType().name().equals(material)) {
                return true;
            }
        }
        return false;
    }
}
