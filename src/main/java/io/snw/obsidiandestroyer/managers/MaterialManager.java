package io.snw.obsidiandestroyer.managers;

import io.snw.obsidiandestroyer.datatypes.DurabilityMaterial;

import java.util.HashMap;
import java.util.Map;

public class MaterialManager {
    private static MaterialManager instance;
    private Map<String, DurabilityMaterial> durabilityMaterial = new HashMap<String, DurabilityMaterial>();

    public MaterialManager() {
        instance = this;
        durabilityMaterial = ConfigManager.getInstance().getDurabilityBlocks();
    }

    public void setDuraBlocks(Map<String, DurabilityMaterial> rBocks) {
        this.durabilityMaterial = rBocks;
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
     * @param item to compare against
     * @return true if item equals managed block
     */
    public boolean contains(String material) {
        if (durabilityMaterial.containsKey(material)) {
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
        if (durabilityMaterial.containsKey(material)) {
            return durabilityMaterial.get(material).getEnabled();
        }
        return false;
    }

    /**
     * Returns the max durability.
     * 
     * @return the max durability
     */
    public int getDurability(String material) {
        if (durabilityMaterial.containsKey(material)) {
            return durabilityMaterial.get(material).getDurability();
        }
        return 0;
    }

    /**
     * Returns whether durability timer for block is enabled.
     * 
     * @return whether durability timer for block is enabled
     */
    public boolean getDurabilityResetTimerEnabled(String material) {
        if (durabilityMaterial.containsKey(material)) {
            return durabilityMaterial.get(material).getResetEnabled();
        }
        return false;
    }

    /**
     * Returns the time in milliseconds after which the durability gets reset.
     * 
     * @return the time in milliseconds after which the durability gets reset
     */
    public long getDurabilityResetTime(String material) {
        if (durabilityMaterial.containsKey(material)) {
            return durabilityMaterial.get(material).getResetTime();
        }
        return 100000L;
    }

    /**
     * Returns the chance to drop an item from a blown up block.
     * 
     * @return the chance to drop an item from a blown up block
     */
    public double getChanceToDropBlock(String material) {
        if (durabilityMaterial.containsKey(material)) {
            return durabilityMaterial.get(material).getChanceTopDrop();
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
        if (durabilityMaterial.containsKey(material)) {
            return durabilityMaterial.get(material).getGhastsEnabled();
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
        if (durabilityMaterial.containsKey(material)) {
            return durabilityMaterial.get(material).getCreepersEnabled();
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
        if (durabilityMaterial.containsKey(material)) {
            return durabilityMaterial.get(material).getCannonsEnabled();
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
        if (durabilityMaterial.containsKey(material)) {
            return durabilityMaterial.get(material).getTntEnabled();
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
        if (durabilityMaterial.containsKey(material)) {
            return durabilityMaterial.get(material).getTntMinecartsEnabled();
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
        if (durabilityMaterial.containsKey(material)) {
            return durabilityMaterial.get(material).getWithersEnabled();
        }
        return false;
    }
}
