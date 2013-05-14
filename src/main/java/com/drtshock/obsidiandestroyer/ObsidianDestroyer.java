package com.drtshock.obsidiandestroyer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.drtshock.obsidiandestroyer.Metrics.Graph;

/**
 * The ObsidianDestroyer plugin.
 * 
 * Allows certain explosions to destroy Obsidian.
 * 
 * @author drtshock
 * 
 */
public final class ObsidianDestroyer extends JavaPlugin {

    private final ODCommands cmdExecutor = new ODCommands(this);
    private ODConfig config = new ODConfig(this);
    private final ODEntityListener entityListener = new ODEntityListener(this);
    private final ODJoinListener joinListener = new ODJoinListener(this);
    public static Logger LOG;
    private static PluginManager pm;

    public static boolean UPDATE = false;
    public static String NAME = "";
    
    private static boolean hookedFactions = false;
    private static boolean hookedTowny = false;

    
    @Override
    public void onDisable() {
        config.saveDurabilityToFile();
        saveConfig();
    }

    
    @Override
    public void onEnable() {
        pm = getServer().getPluginManager();
        LOG = getLogger();
        getCommand("obsidiandestroyer").setExecutor(cmdExecutor);
        getCommand("od").setExecutor(cmdExecutor);

        config.loadConfig();
        entityListener.setObsidianDurability(config.loadDurabilityFromFile());
        checkFactionsHook();
        checkTownyHook();

        pm.registerEvents(entityListener, this);
        pm.registerEvents(joinListener, this);
        
        startMetrics();

        if(config.getCheckUpdate()) {
            Updater updater = new Updater(this, "obsidiandestroyer", this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
            UPDATE = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE; 
            NAME = updater.getLatestVersionString();
        }
    }

    public void startMetrics() { 	
        try {	
            Metrics metrics = new Metrics(this);
            
            Graph graph = metrics.createGraph("Durability");
            
            graph.addPlotter(new Metrics.Plotter("Obsidian Durability Per Server") {

                @Override
                public String getColumnName() {
                    return String.valueOf(config.getoDurability());
                }
            	
                @Override
                public int getValue() {
                    return 1;
                }
            });

            metrics.start();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to submit the stats :-("); // Failed to submit the stats :-(
        }
    }

    /**
     * Returns the config of this plugin.
     * 
     * @return the config of this plugin
     */
    public ODConfig getODConfig() {
        return config;
    }

    /**
     * Returns the entity listener of this plugin.
     * 
     * @return the entity listener of this plugin
     */
    public ODEntityListener getListener() {
        return entityListener;
    }
    
    /**
     * Checks to see if the Factions plugin is active.
     */
    private void checkFactionsHook() {
        Plugin plug = pm.getPlugin("Factions");
		
        if (plug != null) {
            LOG.info("Factions Found! Enabling hook..");
            hookedFactions = true;
        }
    }
        
    /**
     * Gets the state of the Factions hook.
     * 
     * @return Factions hook state
     */
    public static boolean hookedFactions() {
        return hookedFactions;
    }
    
    /**
     * Checks to see if the Towny plugin is active.
     */
    private void checkTownyHook() {
        Plugin plug = pm.getPlugin("Towny");
		
        if (plug != null) {
            LOG.info("Towny Found! Enabling hook..");
            hookedTowny = true;
        }
    }
    
    /**
     * Gets the state of the Towny hook.
     * 
     * @return Towny hook state
     */
    public static boolean hookedTowny() {
        return hookedTowny;
    }
}
