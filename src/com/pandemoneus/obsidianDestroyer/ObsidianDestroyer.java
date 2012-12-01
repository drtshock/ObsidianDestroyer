package com.pandemoneus.obsidianDestroyer;

import java.io.IOException;
import java.util.logging.Logger;

import com.pandemoneus.obsidianDestroyer.Metrics;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The ObsidianDestroyer plugin.
 * 
 * Allows certain explosions to destroy Obsidian.
 * 
 * @author Pandemoneus
 * 
 */
public final class ObsidianDestroyer extends JavaPlugin {
	/**
	 * Plugin related stuff
	 */
	private final ODCommands cmdExecutor = new ODCommands(this);
	private ODConfig config = new ODConfig(this);
	private final ODEntityListener entityListener = new ODEntityListener(this);
	public static ObsidianDestroyer plugin;
	public static final Logger log = Logger.getLogger("Minecraft");

	private static String version;
	private static final String PLUGIN_NAME = "ObsidianDestroyer";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDisable() {
		config.saveDurabilityToFile();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onEnable() {
		getCommand("obsidiandestroyer").setExecutor(cmdExecutor);
		getCommand("od").setExecutor(cmdExecutor);

		config.loadConfig();
		entityListener.setObsidianDurability(config.loadDurabilityFromFile());
		
		// start Metrics
		startMetrics();

		getServer().getPluginManager().registerEvents(entityListener, this);
	}
	
	public void startMetrics() { 	
		PluginDescriptionFile pdfFile = this.getDescription();
		try {	
			Metrics metrics = new Metrics(this);	
			metrics.start();
			ObsidianDestroyer.log.info("[" + pdfFile.getName() + "] Metrics connection started.");
		} catch (IOException e) {
			ObsidianDestroyer.log.warning("[" + pdfFile.getName() + "] Failed to submit the stats :-("); // Failed to submit the stats :-(
		}
	}

	/**
	 * Returns the version of the plugin.
	 * 
	 * @return the version of the plugin
	 */
	public static String getVersion() {
		return version;
	}

	/**
	 * Returns the name of the plugin.
	 * 
	 * @return the name of the plugin
	 */
	public static String getPluginName() {
		return PLUGIN_NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getPluginName();
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
	 * Method that handles what gets reloaded
	 * 
	 * @return true if everything loaded properly, otherwise false
	 */
	public boolean reload() {
		return config.loadConfig();
	}
}
