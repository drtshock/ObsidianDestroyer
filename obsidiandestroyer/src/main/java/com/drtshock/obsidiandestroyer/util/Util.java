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
        switch (material) {
            case AIR:
            case ARROW:
            case APPLE:
            case BAKED_POTATO:
            case BED:
            case BLAZE_ROD:
            case BLAZE_POWDER:
            case BOAT:
            case BONE:
            case BOOK:
            case BOW:
            case BOWL:
            case BREAD:
            case BUCKET:
            case CAKE:
            case CARROT:
            case CARROT_ITEM:
            case CAULDRON_ITEM:
            case CHAINMAIL_BOOTS:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_LEGGINGS:
            case CLAY_BALL:
            case CLAY_BRICK:
            case COAL:
            case COCOA:
            case COMPASS:
            case COOKED_BEEF:
            case COOKED_CHICKEN:
            case COOKED_FISH:
            case COOKIE:
            case DIAMOND:
            case DIAMOND_AXE:
            case DIAMOND_BARDING:
            case DIAMOND_BOOTS:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_HELMET:
            case DIAMOND_HOE:
            case DIAMOND_LEGGINGS:
            case DIAMOND_PICKAXE:
            case DIAMOND_SPADE:
            case DIAMOND_SWORD:
            case EGG:
            case EMERALD:
            case EMPTY_MAP:
            case ENCHANTED_BOOK:
            case ENDER_PEARL:
            case EXP_BOTTLE:
            case EYE_OF_ENDER:
            case FEATHER:
            case FERMENTED_SPIDER_EYE:
            case FIREBALL:
            case FIREWORK:
            case FIREWORK_CHARGE:
            case FLINT:
            case FLINT_AND_STEEL:
            case GHAST_TEAR:
            case GLASS_BOTTLE:
            case GLOWSTONE_DUST:
            case GOLDEN_APPLE:
            case GOLDEN_CARROT:
            case GOLD_AXE:
            case GOLD_BARDING:
            case GOLD_BOOTS:
            case GOLD_CHESTPLATE:
            case GOLD_HELMET:
            case GOLD_HOE:
            case GOLD_INGOT:
            case GOLD_LEGGINGS:
            case GOLD_NUGGET:
            case GOLD_PICKAXE:
            case GOLD_PLATE:
            case GOLD_RECORD:
            case GOLD_SPADE:
            case GOLD_SWORD:
            case GREEN_RECORD:
            case GRILLED_PORK:
            case HOPPER_MINECART:
            case INK_SACK:
            case IRON_AXE:
            case IRON_BARDING:
            case IRON_BOOTS:
            case IRON_CHESTPLATE:
            case IRON_HELMET:
            case IRON_HOE:
            case IRON_INGOT:
            case IRON_LEGGINGS:
            case IRON_PICKAXE:
            case IRON_PLATE:
            case IRON_SPADE:
            case IRON_SWORD:
            case LAVA_BUCKET:
            case LEASH:
            case LEATHER:
            case LEATHER_BOOTS:
            case LEATHER_CHESTPLATE:
            case LEATHER_HELMET:
            case LEATHER_LEGGINGS:
            case MAGMA_CREAM:
            case MAP:
            case MELON:
            case MELON_SEEDS:
            case MILK_BUCKET:
            case MINECART:
            case MONSTER_EGG:
            case MONSTER_EGGS:
            case MUSHROOM_SOUP:
            case NAME_TAG:
            case NETHER_WARTS:
            case NETHER_BRICK_ITEM:
            case NETHER_STAR:
            case PAINTING:
            case PAPER:
            case POISONOUS_POTATO:
            case PORK:
            case POTATO:
            case POTATO_ITEM:
            case POTION:
            case POWERED_MINECART:
            case PUMPKIN_SEEDS:
            case PUMPKIN_PIE:
            case QUARTZ:
            case RAW_BEEF:
            case RAW_CHICKEN:
            case RAW_FISH:
            case RECORD_10:
            case RECORD_11:
            case RECORD_12:
            case RECORD_3:
            case RECORD_4:
            case RECORD_5:
            case RECORD_6:
            case RECORD_7:
            case RECORD_8:
            case RECORD_9:
            case REDSTONE:
            case ROTTEN_FLESH:
            case SADDLE:
            case SEEDS:
            case SHEARS:
            case SKULL_ITEM:
            case SLIME_BALL:
            case SNOW:
            case SNOW_BALL:
            case SPECKLED_MELON:
            case SPIDER_EYE:
            case STICK:
            case STONE_AXE:
            case STONE_HOE:
            case STONE_PICKAXE:
            case STONE_PLATE:
            case STONE_SPADE:
            case STONE_SWORD:
            case STORAGE_MINECART:
            case STRING:
            case SUGAR:
            case SUGAR_CANE:
            case SULPHUR:
            case WATCH:
            case WATER_BUCKET:
            case WHEAT:
            case WOOD_HOE:
            case WOOD_PICKAXE:
            case WOOD_PLATE:
            case WOOD_SPADE:
            case WOOD_SWORD:
            case WRITTEN_BOOK:
                return false;
            default:
                break;
        }
        return true;
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
            BlockIterator blocksInPath = new BlockIterator(tLoc.getWorld(), dLoc.toVector(), tLoc.toVector().subtract(dLoc.toVector()).normalize(), 0, (int) dLoc.distance(tLoc));

            // iterate through the blocks in the path
            while (blocksInPath.hasNext()) {
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
        ArrayList<Location> tagetsInPath = new ArrayList<Location>();

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
            BlockIterator blocksInPath = new BlockIterator(tLoc.getWorld(), dLoc.toVector(), tLoc.toVector().subtract(dLoc.toVector()).normalize(), 0, (int) dLoc.distance(tLoc));

            // iterate through the blocks in the path
            int i = ConfigManager.getInstance().getRadius() + 1;
            while (blocksInPath.hasNext()) {
                if (i > 0) {
                    i--;
                } else {
                    break;
                }
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
        switch (type) {
            case AIR:
            case LONG_GRASS:
            case DEAD_BUSH:
            case THIN_GLASS:
            case YELLOW_FLOWER:
            case RED_ROSE:
            case BROWN_MUSHROOM:
            case RED_MUSHROOM:
            case SNOW:
            case WEB:
            case STRING:
            case VINE:
            case DOUBLE_PLANT:
            case FIRE:
            case TORCH:
            case REDSTONE_TORCH_ON:
            case REDSTONE_TORCH_OFF:
            case REDSTONE:
                return true;
            default:
                return false;
        }
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
}
