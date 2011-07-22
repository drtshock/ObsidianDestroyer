package com.pandemoneus.obsidianDestroyer.config;

import java.io.File;
import java.io.IOException;

import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;

import com.pandemoneus.obsidianDestroyer.ObsidianDestroyer;
import com.pandemoneus.obsidianDestroyer.logger.Log;

/**
 * The configuration file for the ObsidianDestroyer plugin, uses YML.
 * 
 * @author Pandemoneus
 * 
 */
public final class ODConfig {

	private ObsidianDestroyer plugin;

	/**
	 * File handling
	 */
	private static String directory = "plugins" + File.separator + ObsidianDestroyer.getPluginName()
			+ File.separator;
	private File configFile = new File(directory + File.separator
			+ "config.yml");
	private Configuration bukkitConfig = new Configuration(configFile);

	/**
	 * Default settings
	 */
	private static int explosionRadius = 3;
	private static boolean tntEnabled = true;
	private static boolean creepersEnabled = false;
	private static boolean ghastsEnabled = false;
	private static boolean durabilityEnabled = false;
	private static int durability = 1;

	/**
	 * Associates this object with a plugin
	 * 
	 * @param plugin
	 *            the plugin
	 */
	public ODConfig(ObsidianDestroyer plugin) {
		this.plugin = plugin;
	}

	/**
	 * Loads the configuration used by this plugin.
	 * 
	 * @return true if config loaded without errors
	 */
	public boolean loadConfig() {
		boolean isErrorFree = true;

		new File(directory).mkdir();

		if (configFile.exists()) {
			bukkitConfig.load();
			if (bukkitConfig.getString("Version").equals(ObsidianDestroyer.getVersion())) {
				// config file exists and is up to date
				Log.info("ObsidianDestroyer config file found, loading config...");
				loadData();
			} else {
				// config file exists but is outdated
				Log.info("ObsidianDestroyer config file outdated, adding old data and creating new values. "
						+ "Make sure you change those!");
				loadData();
				writeDefault();
			}
		} else {
			// config file does not exist
			try {
				Log.info("ObsidianDestroyer config file not found, creating new config file...");
				configFile.createNewFile();
				writeDefault();
			} catch (IOException ioe) {
				Log.severe("Could not create the config file for ObsidianDestroyer!");
				ioe.printStackTrace();
				isErrorFree = false;
			}
		}

		return isErrorFree;
	}

	private void loadData() {
		bukkitConfig.load();
		explosionRadius = bukkitConfig.getInt("Radius", 3);

		tntEnabled = bukkitConfig.getBoolean("EnabledFor.TNT", true);
		creepersEnabled = bukkitConfig.getBoolean("EnabledFor.Creepers", false);
		ghastsEnabled = bukkitConfig.getBoolean("EnabledFor.Ghasts", false);
		
		durabilityEnabled = bukkitConfig.getBoolean("Durability.Enabled", false);
		durability = bukkitConfig.getInt("Durability.Amount", 1);
	}

	private void writeDefault() {
		write("Version", ObsidianDestroyer.getVersion());
		write("Radius", explosionRadius);

		write("EnabledFor.TNT", tntEnabled);
		write("EnabledFor.Creepers", creepersEnabled);
		write("EnabledFor.Ghasts", ghastsEnabled);
		
		write("Durability.Enabled", durabilityEnabled);
		write("Durability.Amount", durability);

		loadData();
	}

	private void write(String key, Object o) {
		bukkitConfig.load();
		bukkitConfig.setProperty(key, o);
		bukkitConfig.save();
	}

	/**
	 * Returns the explosion radius.
	 * 
	 * @return the explosion radius
	 */
	public static int getRadius() {
		return explosionRadius;
	}

	/**
	 * Returns whether TNT is allowed to destroy Obsidian.
	 * 
	 * @return whether TNT is allowed to destroy Obsidian
	 */
	public static boolean getTntEnabled() {
		return tntEnabled;
	}

	/**
	 * Returns whether Creepers are allowed to destroy Obsidian.
	 * 
	 * @return whether Creepers are allowed to destroy Obsidian
	 */
	public static boolean getCreepersEnabled() {
		return creepersEnabled;
	}

	/**
	 * Returns whether Ghasts are allowed to destroy Obsidian.
	 * 
	 * @return whether Ghasts are allowed to destroy Obsidian
	 */
	public static boolean getGhastsEnabled() {
		return ghastsEnabled;
	}
	
	/**
	 * Returns whether durability for Obsidian is enabled.
	 * 
	 * @return whether durability for Obsidian is enabled
	 */
	public static boolean getDurabilityEnabled() {
		return durabilityEnabled;
	}
	
	/**
	 * Returns the max durability.
	 * 
	 * @return the max durability
	 */
	public static int getDurability() {
		return durability;
	}

	/**
	 * Returns a list containing all loaded keys.
	 * 
	 * @return a list containing all loaded keys
	 */
	public String[] printLoadedConfig() {
		bukkitConfig.load();

		String[] tmp = bukkitConfig.getAll().toString().split(",");
		int n = tmp.length;

		tmp[0] = tmp[0].substring(1);
		tmp[n - 1] = tmp[n - 1].substring(0, tmp[n - 1].length() - 1);

		for (String s : tmp) {
			s = s.trim();
		}

		return tmp;
	}

	/**
	 * Returns the config file.
	 * 
	 * @return the config file
	 */
	public File getConfigFile() {
		return configFile;
	}
	
	/**
	 * Returns the associated plugin.
	 * 
	 * @return the associated plugin
	 */
	public Plugin getPlugin() {
		return plugin;
	}
}
