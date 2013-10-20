package io.snw.obsidiandestroyer.datatypes;

import org.bukkit.Material;

public class DurabilityMaterial {
    private Material type;
    private int dura;
    private boolean enabled;
    private double chanceToDrop;
    private boolean resetEnabled;
    private long resetTime;
    private boolean tntEnabled = true;
    private boolean cannonsEnabled = false;
    private boolean creepersEnabled = false;
    private boolean ghastsEnabled = false;
    private boolean withersEnabled = false;
    private boolean tntMinecartsEnabled;

    public DurabilityMaterial(Material type, int dura, boolean enabled, double chanceToDrop, boolean resetEnabled, long resetTime, boolean tnt, boolean cannons, boolean creepers, boolean ghasts, boolean withers, boolean minecarts) {
        this.type = type;
        this.dura = dura;
        this.enabled = enabled;
        this.chanceToDrop = chanceToDrop;
        this.resetEnabled = resetEnabled;
        this.resetTime = resetTime;
        this.tntEnabled = tnt;
        this.cannonsEnabled = cannons;
        this.creepersEnabled = creepers;
        this.ghastsEnabled = ghasts;
        this.withersEnabled = withers;
        this.tntMinecartsEnabled = minecarts;
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
