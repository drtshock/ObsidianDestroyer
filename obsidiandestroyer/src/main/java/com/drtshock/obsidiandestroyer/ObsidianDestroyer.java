package com.drtshock.obsidiandestroyer;

import com.drtshock.obsidiandestroyer.commands.ODCommand;
import com.drtshock.obsidiandestroyer.enumerations.DamageResult;
import com.drtshock.obsidiandestroyer.listeners.*;
import com.drtshock.obsidiandestroyer.managers.ChunkManager;
import com.drtshock.obsidiandestroyer.managers.ConfigManager;
import com.drtshock.obsidiandestroyer.managers.HookManager;
import com.drtshock.obsidiandestroyer.managers.MaterialManager;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ObsidianDestroyer extends JavaPlugin {

    public static Logger LOG;
    private static ObsidianDestroyer instance;

    public static ObsidianDestroyer getInstance() {
        return instance;
    }

    /**
     * Prints a debug message to the log
     *
     * @param debug message
     */
    public static void debug(String debug) {
        if (ConfigManager.getInstance() == null || ConfigManager.getInstance().getDebug()) {
            LOG.info(debug);
        }
    }

    /**
     * Prints a verbose debug message to the log
     *
     * @param debug message
     */
    public static void vdebug(String debug) {
        if (ConfigManager.getInstance() == null || (ConfigManager.getInstance().getDebug() && ConfigManager.getInstance().getVerbose())) {
            LOG.info(debug);
        }
    }

    /**
     * Gets the durability of a material a location
     *
     * @param location the location to check
     * @return the durability amount
     */
    public static int getDurability(Location location) {
        if (instance == null || ChunkManager.getInstance() == null) {
            return 0;
        }
        if (!instance.isEnabled()) {
            return 0;
        }

        return ChunkManager.getInstance().getMaterialDurability(location);
    }

    /**
     * Raw damage to a block at a location.  Can specify amount.
     *
     * @param location     the location to attempt to apply damage
     * @param damageAmount the amount of damage to apply to the durability
     * @return the result of the attempted damage to location
     */
    public static DamageResult damageBlock(Location location, int damageAmount) {
        if (instance == null || ChunkManager.getInstance() == null) {
            return DamageResult.ERROR;
        }
        if (!instance.isEnabled()) {
            return DamageResult.ERROR;
        }
        if (damageAmount <= 0) {
            return DamageResult.NONE;
        }

        return ChunkManager.getInstance().damageBlock(location, damageAmount);
    }

    /**
     * Damage a block at a location by an entity type
     *
     * @param location   the location to attempt to apply damage
     * @param entityType the entity type that is supplying the damage
     * @return the result of the attempted damage to location
     */
    public static DamageResult damageBlock(Location location, EntityType entityType) {
        if (instance == null || ChunkManager.getInstance() == null) {
            return DamageResult.ERROR;
        }
        if (!instance.isEnabled()) {
            return DamageResult.ERROR;
        }

        return ChunkManager.getInstance().damageBlock(location, entityType);
    }

    /**
     * Damage block at location by an entity
     *
     * @param location the location to attempt to apply damage
     * @param entity   the entity that is supplying the damage
     * @return the result of the attempted damage to location
     */
    public static DamageResult damageBlock(Location location, Entity entity) {
        if (instance == null || ChunkManager.getInstance() == null) {
            return DamageResult.ERROR;
        }
        if (!instance.isEnabled()) {
            return DamageResult.ERROR;
        }

        if (entity == null) {
            return DamageResult.ERROR;
        }

        return ChunkManager.getInstance().damageBlock(location, entity);
    }

    @Override
    public void onEnable() {
        instance = this;
        LOG = getLogger();

        // Initialize managers
        new ConfigManager(false);
        new HookManager();
        new MaterialManager();
        new ChunkManager();

        // Set command executor
        getCommand("od").setExecutor(new ODCommand());

        // Register Event listeners
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new EntityExplodeListener(), this);
        if (HookManager.getInstance().isHookedCannons()) {
            pm.registerEvents(new EntityImpactListener(), this);
        }
        pm.registerEvents(new PlayerListener(), this);
        pm.registerEvents(new BlockListener(), this);
        pm.registerEvents(new ObsidianDestroyerListener(), this);
        try {
            Class.forName("org.bukkit.event.block.BlockExplodeEvent");
            pm.registerEvents(new SpigotListener(), this);
            LOG.log(Level.INFO, "Because of Spigot's laughable design decisions, we need to provide separate code");
            LOG.log(Level.INFO, "to hook into another one of their breaking changes they like to make. Despite Spigot's grotesqueness,");
            LOG.log(Level.INFO, "we provide compatibility because so many of our users run Spigot; so you're good to go.");
        } catch (ClassNotFoundException e) {
            // YAY
        }
    }

    @Override
    public void onDisable() {
        // Save persistent data
        if (ChunkManager.getInstance() != null) {
            ChunkManager.getInstance().save();
        }
    }
}