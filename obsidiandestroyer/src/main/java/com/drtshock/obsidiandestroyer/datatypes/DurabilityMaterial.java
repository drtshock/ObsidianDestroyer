package com.drtshock.obsidiandestroyer.datatypes;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public class DurabilityMaterial {

    private Material type;
    private int dura;
    private int blastRadius;
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
    private int tntDamage;
    private int cannonDamage;
    private int creeperDamage;
    private int chargedCreeperDamage;
    private int ghastDamage;
    private int witherDamage;
    private int tntMinecartDamage;
    private double fluidDamper;
    private boolean destructible;

    /**
     * Storage for a tracked material from the config
     *
     * @param type    the type of material
     * @param section the configuration section to load
     */
    public DurabilityMaterial(Material type, ConfigurationSection section) {
        this.type = type;
        this.blastRadius = section.getInt("BlastRadius", 0);
        this.destructible = section.getBoolean("Destructible", true);
        this.dura = section.getInt("Durability.Amount", 5);
        this.fluidDamper = section.getDouble("Durability.FluidDamper", 0);
        this.enabled = section.getBoolean("Durability.Enabled", true);
        this.chanceToDrop = section.getDouble("Durability.ChanceToDrop", 0.7);
        this.resetEnabled = section.getBoolean("Durability.ResetEnabled", false);
        this.resetTime = section.getLong("Durability.ResetAfter", 10000L);
        this.tntEnabled = section.getBoolean("EnabledFor.TNT", true);
        this.cannonsEnabled = section.getBoolean("EnabledFor.Cannons", false);
        this.creepersEnabled = section.getBoolean("EnabledFor.Creepers", false);
        this.ghastsEnabled = section.getBoolean("EnabledFor.Ghasts", false);
        this.withersEnabled = section.getBoolean("EnabledFor.Withers", false);
        this.tntMinecartsEnabled = section.getBoolean("EnabledFor.Minecarts", false);
        this.tntDamage = section.getInt("Damage.TNT", 1);
        this.cannonDamage = section.getInt("Damage.Cannons", 1);
        this.creeperDamage = section.getInt("Damage.Creepers", 1);
        this.chargedCreeperDamage = section.getInt("Damage.ChargedCreepers", 1);
        this.ghastDamage = section.getInt("Damage.Ghasts", 1);
        this.witherDamage = section.getInt("Damage.Withers", 1);
        this.tntMinecartDamage = section.getInt("Damage.Minecarts", 1);
        this.tallyKittens();
    }

    private void tallyKittens() {
        if (blastRadius < 0) {
            blastRadius = 1;
        }
        if (dura <= 0) {
            dura = 1;
        }
        if (fluidDamper < 0.0) {
            fluidDamper = 0;
        } else if (fluidDamper > 1.0) {
            fluidDamper = 1.0;
        }
        if (resetTime <= 0) {
            resetTime = 100L;
        }
        if (chanceToDrop > 1) {
            chanceToDrop = 1.0;
        } else if (chanceToDrop < 0) {
            chanceToDrop = 0;
        }
        if (tntDamage < 1) {
            tntDamage = 1;
        }
        if (cannonDamage < 1) {
            cannonDamage = 1;
        }
        if (creeperDamage < 1) {
            creeperDamage = 1;
        }
        if (chargedCreeperDamage < 1) {
            chargedCreeperDamage = 1;
        }
        if (ghastDamage < 1) {
            ghastDamage = 1;
        }
        if (witherDamage < 1) {
            witherDamage = 1;
        }
        if (tntMinecartDamage < 1) {
            tntMinecartDamage = 1;
        }
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

    public int getRadius() {
        return this.blastRadius;
    }

    public int getTntDamage() {
        return tntDamage;
    }

    public int getCannonsDamage() {
        return cannonDamage;
    }

    public int getCreepersDamage() {
        return creeperDamage;
    }

    public int getChargedCreeperDamage() {
        return chargedCreeperDamage;
    }

    public int getGhastsDamage() {
        return ghastDamage;
    }

    public int getWithersDamage() {
        return witherDamage;
    }

    public int getTntMinecartsDamage() {
        return tntMinecartDamage;
    }

    public double getFluidDamper() {
        return fluidDamper;
    }

    public boolean isDestructible() {
        return destructible;
    }

    @Override
    public String toString() {
        return getType() != null ? getType().name() : "material";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof String) {
            if (((String) obj).equalsIgnoreCase(getType().name())) {
                return true;
            }
        }
        if (obj instanceof DurabilityMaterial) {
            if (((DurabilityMaterial) obj).getType().equals(getType())) {
                return true;
            }
        }
        return false;
    }
}
