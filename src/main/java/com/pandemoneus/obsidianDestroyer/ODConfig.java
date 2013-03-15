package com.pandemoneus.obsidianDestroyer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public final class ODConfig
{
	private ObsidianDestroyer plugin;
	private static String pluginName;
	private static String pluginVersion;
	private static String directory = "plugins" + File.separator + ObsidianDestroyer.getPluginName() + File.separator;
	private File configFile = new File(directory + "config.yml");
	private File durabilityFile = new File(directory + "durability.dat");
	private YamlConfiguration bukkitConfig = new YamlConfiguration();

	private int explosionRadius = 3;
	private boolean tntEnabled = true;
	private boolean cannonsEnabled = false;
	private boolean creepersEnabled = false;
	private boolean ghastsEnabled = false;
	private boolean withersEnabled = false;
	private boolean durabilityEnabled = false;
	private int odurability = 1;
	private int edurability = 1;
	private int ecdurability = 1;
	private int adurability = 1;
	private int mdurability = 1;
	private boolean durabilityTimerEnabled = true;
	private long durabilityTime = 600000L;
	private double chanceToDropBlock = 0.7D;
	private boolean waterProtection = true;
	private boolean checkUpdate = true;
	private int checkitemid = 38;
	private boolean ignorecancel = false;

	public ODConfig(ObsidianDestroyer plugin) {
		this.plugin = plugin;
		pluginName = ObsidianDestroyer.getPluginName();
	}

	public boolean loadConfig() {
		boolean isErrorFree = true;
		pluginVersion = ObsidianDestroyer.getVersion();

		new File(directory).mkdir();

		if (this.configFile.exists()) {
			try {
				this.bukkitConfig.load(this.configFile);

				if (this.bukkitConfig.getString("Version", "").equals(pluginVersion))
					loadData();

				else {
					Log.info(pluginName + " config file outdated, adding old data and creating new values. " + "Make sure you change those!");
					loadData();
					writeDefault();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			Log.info(pluginName + " config file not found, creating new config file...");
			this.plugin.saveDefaultConfig();
		}

		return isErrorFree;
	}

	private void loadData() {
		try {
			this.bukkitConfig.load(this.configFile);

			this.checkUpdate = this.bukkitConfig.getBoolean("checkupdate", true);

			this.explosionRadius = this.bukkitConfig.getInt("Radius", 3);
			this.waterProtection = this.bukkitConfig.getBoolean("FluidsProtect", true);
			this.checkitemid = this.bukkitConfig.getInt("CheckItemId", 38);
			this.ignorecancel = this.bukkitConfig.getBoolean("IgnoreCancel", false);

			this.tntEnabled = this.bukkitConfig.getBoolean("EnabledFor.TNT", true);
			this.cannonsEnabled = this.bukkitConfig.getBoolean("EnabledFor.Cannons", false);
			this.creepersEnabled = this.bukkitConfig.getBoolean("EnabledFor.Creepers", false);
			this.ghastsEnabled = this.bukkitConfig.getBoolean("EnabledFor.Ghasts", false);
			this.withersEnabled = this.bukkitConfig.getBoolean("EnabledFor.Withers", false);

			this.durabilityEnabled = this.bukkitConfig.getBoolean("Durability.Enabled", false);
			this.odurability = this.bukkitConfig.getInt("Durability.Obsidian", 1);
			this.edurability = this.bukkitConfig.getInt("Durability.EnchantmentTable", 1);
			this.ecdurability = this.bukkitConfig.getInt("Durability.EnderChest", 1);
			this.adurability = this.bukkitConfig.getInt("Durability.Anvil", 1);
			this.mdurability = this.bukkitConfig.getInt("Durability.Spawner", 1);
			this.durabilityTimerEnabled = this.bukkitConfig.getBoolean("Durability.ResetEnabled", true);
			this.durabilityTime = readLong("Durability.ResetAfter", "600000");

			this.chanceToDropBlock = this.bukkitConfig.getDouble("Blocks.ChanceToDrop", 0.7D);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeDefault() {
		write("Version", ObsidianDestroyer.getVersion());
		write("autoupdate", Boolean.valueOf(this.checkUpdate));
		write("Radius", Integer.valueOf(this.explosionRadius));
		write("FluidsProtect", Boolean.valueOf(this.waterProtection));
		write("CheckItemId", Integer.valueOf(this.checkitemid));
		write("IgnoreCancel", Boolean.valueOf(this.ignorecancel));

		write("EnabledFor.TNT", Boolean.valueOf(this.tntEnabled));
		write("EnabledFor.Cannons", Boolean.valueOf(this.cannonsEnabled));
		write("EnabledFor.Creepers", Boolean.valueOf(this.creepersEnabled));
		write("EnabledFor.Ghasts", Boolean.valueOf(this.ghastsEnabled));
		write("EnabledFor.Withers", Boolean.valueOf(this.withersEnabled));


		write("Durability.Enabled", Boolean.valueOf(this.durabilityEnabled));
		write("Durability.Obsidian", Integer.valueOf(this.odurability));
		write("Durability.EnchantmentTable", Integer.valueOf(this.edurability));
		write("Durability.EnderChest", Integer.valueOf(this.ecdurability));
		write("Durability.Anvil", Integer.valueOf(this.adurability));
		write("Durability.Spawners", Integer.valueOf(this.mdurability));
		write("Durability.ResetEnabled", Boolean.valueOf(this.durabilityTimerEnabled));
		write("Durability.ResetAfter", this.durabilityTime);

		write("Blocks.ChanceToDrop", Double.valueOf(this.chanceToDropBlock));

		loadData();
	}

	private void write(String key, Object o) {
		try {
			this.bukkitConfig.load(this.configFile);
			this.bukkitConfig.set(key, o);
			this.bukkitConfig.save(this.configFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private long readLong(String key, String def) {
		try {
			this.bukkitConfig.load(this.configFile);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String value = this.bukkitConfig.getString(key, def);

		long tmp = 0L;
		try {
			tmp = Long.parseLong(value);
		} catch (NumberFormatException nfe) {
			Log.warning("Error parsing a long from the config file. Key=" + key);
			nfe.printStackTrace();
		}

		return tmp;
	}

	public int getRadius() {
		return this.explosionRadius;
	}

	public boolean getCheckUpdate() {
		return this.checkUpdate;
	}

	public boolean getTntEnabled() {
		return this.tntEnabled;
	}

	public boolean getCannonsEnabled() {
		return this.cannonsEnabled;
	}

	public boolean getCreepersEnabled() {
		return this.creepersEnabled;
	}

	public boolean getGhastsEnabled() {
		return this.ghastsEnabled;
	}

	public boolean getWithersEnabled() {
		return this.withersEnabled;
	}

	public boolean getDurabilityEnabled() {
		return this.durabilityEnabled;
	}

	public int getoDurability() {
		return this.odurability;
	}

	public int geteDurability() {
		return this.edurability;
	}

	public int getecDurability() {
		return this.ecdurability;
	}

	public int getaDurability() {
		return this.adurability;
	}

	public int getmDurability() {
		return this.mdurability;
	}

	public boolean getDurabilityResetTimerEnabled() {
		return this.durabilityTimerEnabled;
	}

	public long getDurabilityResetTime() {
		return this.durabilityTime;
	}

	public double getChanceToDropBlock() {
		return this.chanceToDropBlock;
	}

	public boolean getWaterProtection() {
		return this.waterProtection;
	}

	public int getCheckItemId() {
		return this.checkitemid;
	}

	public boolean getIgnoreCancel() {
		return this.ignorecancel;
	}

	public String[] printLoadedConfig() {
		return new String[] { "this doesn't work." };
	}

	public File getConfigFile() {
		return this.configFile;
	}

	public Plugin getPlugin() {
		return this.plugin;
	}

	public void saveDurabilityToFile() {
		if ((this.plugin.getListener() == null) || (this.plugin.getListener().getObsidianDurability() == null))
			return;

		HashMap<Integer, Integer> map = this.plugin.getListener().getObsidianDurability();

		new File(directory).mkdir();

		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(this.durabilityFile));
			oos.writeObject(map);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			Log.severe("Failed writing obsidian durability for " + ObsidianDestroyer.getPluginName());
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public HashMap<Integer, Integer> loadDurabilityFromFile() {
		if ((!this.durabilityFile.exists()) || (this.plugin.getListener() == null) || (this.plugin.getListener().getObsidianDurability() == null))
			return null;

		new File(directory).mkdir();

		HashMap<Integer, Integer> map = null;
		Object result = null;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(this.durabilityFile));
			result = ois.readObject();
			map = (HashMap<Integer, Integer>)result;
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