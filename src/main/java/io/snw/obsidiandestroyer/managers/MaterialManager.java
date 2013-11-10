package io.snw.obsidiandestroyer.managers;

import io.snw.obsidiandestroyer.datatypes.DurabilityMaterial;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.EntityType;

public class MaterialManager {
    private static MaterialManager instance;
    private Map<String, DurabilityMaterial> durabilityMaterials = new HashMap<String, DurabilityMaterial>();

    public MaterialManager() {
        instance = this;
        load();
    }

    /**
     * Loads the durability materials to track
     */
    public void load() {
        durabilityMaterials.clear();
        durabilityMaterials = ConfigManager.getInstance().getDurabilityMaterials();
    }

    /**
     * Gets the instance
     *
     * @return instance
     */
    public static MaterialManager getInstance() {
        return instance;
    }

    /**
     * Checks if the managed blocks contains an item
     *
     * @param material to compare against
     * @return true if item equals managed block
     */
    public boolean contains(String material) {
        if (durabilityMaterials.containsKey(material)) {
            return true;
        }
        return false;
    }

    /**
     * Returns whether durability for block is enabled.
     *
     * @return whether durability for block is enabled
     */
    public boolean getDurabilityEnabled(String material) {
        if (durabilityMaterials.containsKey(material)) {
            return durabilityMaterials.get(material).getEnabled();
        }
        return false;
    }

    /**
     * Returns the max durability.
     *
     * @return the max durability
     */
    public int getDurability(String material) {
        if (durabilityMaterials.containsKey(material)) {
            return durabilityMaterials.get(material).getDurability();
        }
        return 0;
    }

    /**
     * Returns whether durability timer for block is enabled.
     *
     * @return whether durability timer for block is enabled
     */
    public boolean getDurabilityResetTimerEnabled(String material) {
        if (durabilityMaterials.containsKey(material)) {
            return durabilityMaterials.get(material).getResetEnabled();
        }
        return false;
    }

    /**
     * Returns the time in milliseconds after which the durability gets reset.
     *
     * @return the time in milliseconds after which the durability gets reset
     */
    public long getDurabilityResetTime(String material) {
        if (durabilityMaterials.containsKey(material)) {
            return durabilityMaterials.get(material).getResetTime();
        }
        return 100000L;
    }

    /**
     * Returns the chance to drop an item from a blown up block.
     *
     * @return the chance to drop an item from a blown up block
     */
    public double getChanceToDropBlock(String material) {
        if (durabilityMaterials.containsKey(material)) {
            return durabilityMaterials.get(material).getChanceTopDrop();
        }
        return 0.6D;
    }

    /**
     * Returns if Fireball damage is enabled for block
     *
     * @param material key
     * @return Fireball damage is enabled for block
     */
    public boolean getGhastsEnabled(String material) {
        if (durabilityMaterials.containsKey(material)) {
            return durabilityMaterials.get(material).getGhastsEnabled();
        }
        return false;
    }

    /**
     * Returns if Creeper damage is enabled for block
     *
     * @param material key
     * @return Creeper damage is enabled for block
     */
    public boolean getCreepersEnabled(String material) {
        if (durabilityMaterials.containsKey(material)) {
            return durabilityMaterials.get(material).getCreepersEnabled();
        }
        return false;
    }

    /**
     * Returns if Cannon damage is enabled for block
     *
     * @param material key
     * @return Cannon damage is enabled for block
     */
    public boolean getCannonsEnabled(String material) {
        if (durabilityMaterials.containsKey(material)) {
            return durabilityMaterials.get(material).getCannonsEnabled();
        }
        return false;
    }

    /**
     * Returns if TNT damage is enabled for block
     *
     * @param material key
     * @return TNT damage is enabled for block
     */
    public boolean getTntEnabled(String material) {
        if (durabilityMaterials.containsKey(material)) {
            return durabilityMaterials.get(material).getTntEnabled();
        }
        return false;
    }

    /**
     * Returns if TNT minecart damage is enabled for block
     *
     * @param material key
     * @return TNT minecart damage is enabled for block
     */
    public boolean getTntMinecartsEnabled(String material) {
        if (durabilityMaterials.containsKey(material)) {
            return durabilityMaterials.get(material).getTntMinecartsEnabled();
        }
        return false;
    }

    /**
     * Returns if Wither damage is enabled for block
     *
     * @param material key
     * @return Wither damage is enabled for block
     */
    public boolean getWithersEnabled(String material) {
        if (durabilityMaterials.containsKey(material)) {
            return durabilityMaterials.get(material).getWithersEnabled();
        }
        return false;
    }

    /**
     * Returns the blast radius for a specific material
     *
     * @param material the name of the material to lookup
     * @return Blast Radius or 0
     */
    public int getBlastRadius(String material) {
        if (durabilityMaterials.containsKey(material)) {
            return durabilityMaterials.get(material).getRadius();
        }
        return 0;
    }

    /**
     * Returns the amount of damage done to the material by a damage type
     * 
     * @param eventTypeRep the event type
     * @param material the name of the material to lookup
     * @return amount of damage done
     */
    public int getDamageTypeAmount(EntityType eventTypeRep, String material) {
        if (durabilityMaterials.containsKey(material)) {
            switch(eventTypeRep) {
                case PRIMED_TNT:
                    return durabilityMaterials.get(material).getTntDamage();
                case SNOWBALL:
                    return durabilityMaterials.get(material).getCannonsDamage();
                case CREEPER:
                    return durabilityMaterials.get(material).getCreepersDamage();
                case WITHER:
                case WITHER_SKULL:
                    return durabilityMaterials.get(material).getWithersDamage();
                case MINECART_TNT:
                    return durabilityMaterials.get(material).getTntMinecartsDamage();
                case FIREBALL:
                case SMALL_FIREBALL:
                case GHAST:
                    return durabilityMaterials.get(material).getGhastsDamage();
                default:
                    break;
            }
        }
        return 1;
    }
}
