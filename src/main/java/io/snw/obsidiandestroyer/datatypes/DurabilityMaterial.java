package io.snw.obsidiandestroyer.datatypes;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public class DurabilityMaterial {
    private Material type;
    private int dura;
    private boolean enabled;
    private double chanceToDrop;
    private boolean resetEnabled;
    private long resetTime;
    private boolean tntEnabled;
    private boolean cannonsEnabled;
    private boolean creepersEnabled;
    private boolean ghastsEnabled;
    private boolean withersEnabled;
    private boolean tntMinecartsEnabled;

    /**
     * Storage for a tracked material from the config
     * 
     * @param type the type of material
     * @param section the configuration section to load
     */
    public DurabilityMaterial(Material type, ConfigurationSection section) {
        this.type = type;
        this.dura = section.getInt("Durability.Amount", 5);
        this.enabled = section.getBoolean("Durability.Enabled", true);
        this.chanceToDrop = section.getDouble("ChanceToDrop", 0.7);
        this.resetEnabled = section.getBoolean("Durability.ResetEnabled", false);
        this.resetTime = section.getLong("Durability.ResetAfter", 10000L);
        this.tntEnabled = section.getBoolean("EnabledFor.TNT", true);
        this.cannonsEnabled = section.getBoolean("EnabledFor.Cannons", false);
        this.creepersEnabled = section.getBoolean("EnabledFor.Creepers", false);
        this.ghastsEnabled = section.getBoolean("EnabledFor.Ghasts", false);
        this.withersEnabled = section.getBoolean("EnabledFor.Minecarts", false);
        this.tntMinecartsEnabled = section.getBoolean("EnabledFor.Withers", false);
    }

    public Material getType() {
        return type;
    }

    public int getDurability() {
        return dura;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public double getChanceTopDrop() {
        return chanceToDrop;
    }

    public boolean getResetEnabled() {
        return resetEnabled;
    }

    public long getResetTime() {
        return resetTime;
    }

    public boolean getTntEnabled() {
        return tntEnabled;
    }

    public boolean getCannonsEnabled() {
        return cannonsEnabled;
    }

    public boolean getCreepersEnabled() {
        return creepersEnabled;
    }

    public boolean getGhastsEnabled() {
        return ghastsEnabled;
    }

    public boolean getWithersEnabled() {
        return this.withersEnabled;
    }

    public boolean getTntMinecartsEnabled() {
        return this.tntMinecartsEnabled;
    }
}
