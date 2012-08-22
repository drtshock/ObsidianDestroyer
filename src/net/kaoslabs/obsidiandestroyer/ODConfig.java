package net.kaoslabs.obsidiandestroyer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * The configuration file for the ObsidianDestroyer plugin, uses YML.
 * 
 * @author Pandemoneus
 * 
 */
public final class ODConfig {

	private ObsidianDestroyer plugin;
	private static String pluginName;
	private static String pluginVersion;

	/**
	 * File handling
	 */
	private static String directory = "plugins" + File.separator + ObsidianDestroyer.getPluginName() + File.separator;
	private File configFile = new File(directory + "config.yml");
	private File durabilityFile = new File(directory + "durability.dat");
	private YamlConfiguration bukkitConfig = new YamlConfiguration();

	/**
	 * Default settings
	 */
	private int explosionRadius = 3;
	private boolean tntEnabled = true;
	private boolean cannonsEnabled = false;
	private boolean creepersEnabled = false;
	private boolean ghastsEnabled = false;
	private boolean durabilityEnabled = false;
	private int durability = 1;
	private boolean durabilityTimerEnabled = true;
	private long durabilityTime = 600000L; // 10 minutes
	private double chanceToDropBlock = 0.7;

	/**
	 * Associates this object with a plugin
	 * 
	 * @param plugin
	 *            the plugin
	 */
	public ODConfig(ObsidianDestroyer plugin) {
		this.plugin = plugin;
		pluginName = ObsidianDestroyer.getPluginName();
	}

	/**
	 * Loads the configuration used by this plugin.
	 * 
	 * @return true if config loaded without errors
	 */
	public boolean loadConfig() {
		boolean isErrorFree = true;
		pluginVersion = ObsidianDestroyer.getVersion();

		new File(directory).mkdir();

		if (configFile.exists()) {
			try {
				bukkitConfig.load(configFile);

				if (bukkitConfig.getString("Version", "").equals(pluginVersion)) {
					// config file exists and is up to date
					Log.info(pluginName + " config file found, loading config...");
					loadData();
				} else {
					// config file exists but is outdated
					Log.info(pluginName + " config file outdated, adding old data and creating new values. " + "Make sure you change those!");
					loadData();
					writeDefault();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// config file does not exist
			try {
				Log.info(pluginName + " config file not found, creating new config file...");
				configFile.createNewFile();
				writeDefault();
			} catch (IOException ioe) {
				Log.severe("Could not create the config file for " + pluginName + "!");
				ioe.printStackTrace();
				isErrorFree = false;
			}
		}

		return isErrorFree;
	}

	private void loadData() {
		try {
			bukkitConfig.load(configFile);

			explosionRadius = bukkitConfig.getInt("Radius", 3);

			tntEnabled = bukkitConfig.getBoolean("EnabledFor.TNT", true);
			cannonsEnabled = bukkitConfig.getBoolean("EnabledFor.Cannons", false);
			creepersEnabled = bukkitConfig.getBoolean("EnabledFor.Creepers", false);
			ghastsEnabled = bukkitConfig.getBoolean("EnabledFor.Ghasts", false);

			durabilityEnabled = bukkitConfig.getBoolean("Durability.Enabled", false);
			durability = bukkitConfig.getInt("Durability.Amount", 1);
			durabilityTimerEnabled = bukkitConfig.getBoolean("Durability.ResetEnabled", true);
			durabilityTime = readLong("Durability.ResetAfter", "600000");

			chanceToDropBlock = bukkitConfig.getDouble("Blocks.ChanceToDrop", 0.7);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeDefault() {
		write("Version", ObsidianDestroyer.getVersion());
		write("Radius", explosionRadius);

		write("EnabledFor.TNT", tntEnabled);
		write("EnabledFor.Cannons", cannonsEnabled);
		write("EnabledFor.Creepers", creepersEnabled);
		write("EnabledFor.Ghasts", ghastsEnabled);

		write("Durability.Enabled", durabilityEnabled);
		write("Durability.Amount", durability);
		write("Durability.ResetEnabled", durabilityTimerEnabled);
		write("Durability.ResetAfter", "" + durabilityTime);

		write("Blocks.ChanceToDrop", chanceToDropBlock);

		loadData();
	}

	private void write(String key, Object o) {
		try {
			bukkitConfig.load(configFile);
			bukkitConfig.set(key, o);
			bukkitConfig.save(configFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Reads a string representing a long from the config file.
	 * 
	 * Returns '0' when an exception occurs.
	 * 
	 * @param key
	 *            the key
	 * @param def
	 *            default value
	 * @return the long specified in 'key' from the config file, '0' on errors
	 */
	private long readLong(String key, String def) {
		try {
			bukkitConfig.load(configFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Bukkit Config has no getLong(..)-method, so we are using Strings
		String value = bukkitConfig.getString(key, def);

		long tmp = 0;

		try {
			tmp = Long.parseLong(value);
		} catch (NumberFormatException nfe) {
			Log.warning("Error parsing a long from the config file. Key=" + key);
			nfe.printStackTrace();
		}

		return tmp;
	}

	/**
	 * Returns the explosion radius.
	 * 
	 * @return the explosion radius
	 */
	public int getRadius() {
		return explosionRadius;
	}

	/**
	 * Returns whether TNT is allowed to destroy Obsidian.
	 * 
	 * @return whether TNT is allowed to destroy Obsidian
	 */
	public boolean getTntEnabled() {
		return tntEnabled;
	}
	
	/**
	 * Returns whether Cannons are allowed to destroy Obsidian.
	 * 
	 * @return whether Cannons are allowed to destroy Obsidian
	 */
	public boolean getCannonsEnabled() {
		return cannonsEnabled;
	}


	/**
	 * Returns whether Creepers are allowed to destroy Obsidian.
	 * 
	 * @return whether Creepers are allowed to destroy Obsidian
	 */
	public boolean getCreepersEnabled() {
		return creepersEnabled;
	}

	/**
	 * Returns whether Ghasts are allowed to destroy Obsidian.
	 * 
	 * @return whether Ghasts are allowed to destroy Obsidian
	 */
	public boolean getGhastsEnabled() {
		return ghastsEnabled;
	}

	/**
	 * Returns whether durability for Obsidian is enabled.
	 * 
	 * @return whether durability for Obsidian is enabled
	 */
	public boolean getDurabilityEnabled() {
		return durabilityEnabled;
	}

	/**
	 * Returns the max durability.
	 * 
	 * @return the max durability
	 */
	public int getDurability() {
		return durability;
	}

	/**
	 * Returns whether durability timer for Obsidian is enabled.
	 * 
	 * @return whether durability timer for Obsidian is enabled
	 */
	public boolean getDurabilityResetTimerEnabled() {
		return durabilityTimerEnabled;
	}

	/**
	 * Returns the time in milliseconds after which the durability gets reset.
	 * 
	 * @return the time in milliseconds after which the durability gets reset
	 */
	public long getDurabilityResetTime() {
		return durabilityTime;
	}

	/**
	 * Returns the chance to drop an item from a blown up block.
	 * 
	 * @return the chance to drop an item from a blown up block
	 */
	public double getChanceToDropBlock() {
		return chanceToDropBlock;
	}

	/**
	 * Returns a list containing all loaded keys.
	 * 
	 * @return a list containing all loaded keys
	 */
	public String[] printLoadedConfig() {
		return new String[] { "this doesn't work." };
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

	/**
	 * Saves the durability hash map to a file.
	 */
	public void saveDurabilityToFile() {
		if (plugin.getListener() == null || plugin.getListener().getObsidianDurability() == null) {
			return;
		}

		HashMap<Integer, Integer> map = plugin.getListener().getObsidianDurability();

		new File(directory).mkdir();

		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(durabilityFile));
			oos.writeObject(map);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			Log.severe("Failed writing obsidian durability for " + ObsidianDestroyer.getPluginName());
			e.printStackTrace();
		}
	}

	/**
	 * Loads the durability hash map from a file.
	 * 
	 * @return the durability hash map from a file
	 */
	@SuppressWarnings("unchecked")
	public HashMap<Integer, Integer> loadDurabilityFromFile() {
		if (!durabilityFile.exists() || plugin.getListener() == null || plugin.getListener().getObsidianDurability() == null) {
			return null;
		}

		new File(directory).mkdir();

		HashMap<Integer, Integer> map = null;
		Object result = null;

		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(durabilityFile));
			result = ois.readObject();
			map = (HashMap<Integer, Integer>) result;
			ois.close();
		} catch (IOException ioe) {
			Log.severe("Failed reading obsidian durability for " + ObsidianDestroyer.getPluginName());
			ioe.printStackTrace();
		} catch (ClassNotFoundException cnfe) {
			Log.severe("Obsidian durability file contains an unknown class, was it modified?");
			cnfe.printStackTrace();
		}

		return map;
	}
}
