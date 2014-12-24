package com.drtshock.obsidiandestroyer.managers;

import com.drtshock.obsidiandestroyer.datatypes.DurabilityMaterial;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class MaterialManager {

    private static MaterialManager instance;
    private Map<String, DurabilityMaterial> durabilityMaterials = new HashMap<String, DurabilityMaterial>();

    /**
     * Stores materials that have durability enabled to track
     */
    public MaterialManager() {
        instance = this;
        load();
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
     * Loads the durability materials to track
     */
    public void load() {
        durabilityMaterials.clear();
        durabilityMaterials = ConfigManager.getInstance().getDurabilityMaterials();
    }

    /**
     * Checks if the managed blocks contains an item
     *
     * @param material to compare against
     * @return true if item equals managed block
     */
    public boolean contains(String material) {
        return durabilityMaterials.containsKey(material);
    }

    /**
     * Returns whether durability for block is enabled.
     *
     * @return whether durability for block is enabled
     */
    public boolean getDurabilityEnabled(String material) {
        return durabilityMaterials.containsKey(material) && durabilityMaterials.get(material).getEnabled();
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
        return durabilityMaterials.containsKey(material) && durabilityMaterials.get(material).getResetEnabled();
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
        return durabilityMaterials.containsKey(material) && durabilityMaterials.get(material).getGhastsEnabled();
    }

    /**
     * Returns if Creeper damage is enabled for block
     *
     * @param material key
     * @return Creeper damage is enabled for block
     */
    public boolean getCreepersEnabled(String material) {
        return durabilityMaterials.containsKey(material) && durabilityMaterials.get(material).getCreepersEnabled();
    }

    /**
     * Returns if Cannon damage is enabled for block
     *
     * @param material key
     * @return Cannon damage is enabled for block
     */
    public boolean getCannonsEnabled(String material) {
        return durabilityMaterials.containsKey(material) && durabilityMaterials.get(material).getCannonsEnabled();
    }

    /**
     * Returns if TNT damage is enabled for block
     *
     * @param material key
     * @return TNT damage is enabled for block
     */
    public boolean getTntEnabled(String material) {
        return durabilityMaterials.containsKey(material) && durabilityMaterials.get(material).getTntEnabled();
    }

    /**
     * Returns if TNT minecart damage is enabled for block
     *
     * @param material key
     * @return TNT minecart damage is enabled for block
     */
    public boolean getTntMinecartsEnabled(String material) {
        return durabilityMaterials.containsKey(material) && durabilityMaterials.get(material).getTntMinecartsEnabled();
    }

    /**
     * Returns if Wither damage is enabled for block
     *
     * @param material key
     * @return Wither damage is enabled for block
     */
    public boolean getWithersEnabled(String material) {
        return durabilityMaterials.containsKey(material) && durabilityMaterials.get(material).getWithersEnabled();
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
     * Returns the amount of damage done to the material by an entity
     *
     * @param entity   the entity that is involved in damaging
     * @param material the name of the material to lookup
     * @return amount of damage done
     */
    public int getDamageTypeAmount(Entity entity, String material) {
        if (durabilityMaterials.containsKey(material)) {
            EntityType eventTypeRep = entity.getType();
            switch (eventTypeRep) {
                case PRIMED_TNT:
                    return durabilityMaterials.get(material).getTntDamage();
                case SNOWBALL:
                    return 0;
                case CREEPER:
                    Creeper creeper = (Creeper) entity;
                    return creeper.isPowered() ? durabilityMaterials.get(material).getChargedCreeperDamage() : durabilityMaterials.get(material).getCreepersDamage();
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

    /**
     * Returns the amount of damage done to the material by a cannon projectile
     *
     * @param material the name of the material to lookup
     * @return amount of damage done
     */
    public int getDamageTypeCannonsAmount(String material) {
        if (durabilityMaterials.containsKey(material)) {
            return durabilityMaterials.get(material).getCannonsDamage();
        }
        return 1;
    }

    public double getFluidDamperAmount(String material) {
        if (durabilityMaterials.containsKey(material)) {
            return durabilityMaterials.get(material).getFluidDamper();
        }
        return 0;
    }

    public boolean isDestructible(String material) {
        if (durabilityMaterials.containsKey(material)) {
            return durabilityMaterials.get(material).isDestructible();
        }
        return true;
    }

}
