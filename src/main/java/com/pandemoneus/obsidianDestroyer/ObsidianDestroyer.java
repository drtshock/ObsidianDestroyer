package com.pandemoneus.obsidianDestroyer;

import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.pandemoneus.obsidianDestroyer.vlisteners.OD1_4_4;
import com.pandemoneus.obsidianDestroyer.vlisteners.OD1_4_5;
import com.pandemoneus.obsidianDestroyer.vlisteners.OD1_4_6;
import com.pandemoneus.obsidianDestroyer.vlisteners.OD1_4_7;
import com.pandemoneus.obsidianDestroyer.vlisteners.OD1_5;
import com.pandemoneus.obsidianDestroyer.vlisteners.OD1_5_1;
import com.pandemoneus.obsidianDestroyer.vlisteners.ODEntityListener;

/**
 * The ObsidianDestroyer plugin.
 * 
 * Allows certain explosions to destroy Obsidian.
 * 
 * @author drtshock
 * 
 */
public final class ObsidianDestroyer extends JavaPlugin {

	/**
	 * Plugin related stuff
	 */
	private final ODCommands cmdExecutor = new ODCommands(this);
	private ODConfig config = new ODConfig(this);
	private final ODEntityListener entityListener = new ODEntityListener(this);
	private final ODJoinListener joinListener = new ODJoinListener(this);
	public static ObsidianDestroyer plugin;
	public static Logger log;


	private static String version;
	private static final String PLUGIN_NAME = "ObsidianDestroyer";
	public static boolean update = false;
	public static String name = "";

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
		loadNMS();
		log = getServer().getLogger();
		PluginDescriptionFile pdfFile = getDescription();
		version = pdfFile.getVersion();

		getCommand("obsidiandestroyer").setExecutor(cmdExecutor);
		getCommand("od").setExecutor(cmdExecutor);

		config.loadConfig();
		entityListener.setObsidianDurability(config.loadDurabilityFromFile());

		// start Metrics
		startMetrics();

		getServer().getPluginManager().registerEvents(joinListener, this);

		// Check for updates.
		if(config.getCheckUpdate()) {
			Updater updater = new Updater(this, "obsidiandestroyer", this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
			update = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE; 
			name = updater.getLatestVersionString();
		}
	}

	public void startMetrics() { 	
		PluginDescriptionFile pdfFile = this.getDescription();
		try {	
			Metrics metrics = new Metrics(this);	
			metrics.start();
		} catch (IOException e) {
			ObsidianDestroyer.log.warning("[" + pdfFile.getName() + "] Failed to submit the stats :-(");
		}
	}

	public void loadNMS() {
		String version = getServer().getBukkitVersion();
		String versionNumber = version.substring(0, 5);

		if (versionNumber.equals("1.4.4"))
			getServer().getPluginManager().registerEvents(new OD1_4_4(this), this);

		else if (versionNumber.equals("1.4.5"))
			getServer().getPluginManager().registerEvents(new OD1_4_5(this), this);

		else if (versionNumber.equalsIgnoreCase("1.4.6")) 
			getServer().getPluginManager().registerEvents(new OD1_4_6(this), this);

		else if (versionNumber.equals("1.4.7"))
			getServer().getPluginManager().registerEvents(new OD1_4_7(this), this);

		else if (versionNumber.equals("1.5-R"))
			getServer().getPluginManager().registerEvents(new OD1_5(this), this);

		else if (versionNumber.equals("1.5.1"))
			getServer().getPluginManager().registerEvents(new OD1_5_1(this), this);

		else {
			ObsidianDestroyer.log.warning("[" + this.getDescription().getName() + "] Couldn't find support for this craftbukkit version.");
			ObsidianDestroyer.log.warning("[" + this.getDescription().getName() + "] Will run without all features.");
			getServer().getPluginManager().registerEvents(new ODEntityListener(this), this);
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
		reloadConfig();
		return true;
	}
}
