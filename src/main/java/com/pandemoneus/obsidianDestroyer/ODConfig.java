package com.pandemoneus.obsidianDestroyer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public final class ODConfig {

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
	private boolean bedrockEnabled = false;
	private int odurability = 1;
	private int edurability = 1;
	private int ecdurability = 1;
	private int adurability = 1;
	private int bdurability = 1;
	private boolean durabilityTimerEnabled = true;
	private long durabilityTime = 600000L;
	private double chanceToDropBlock = 0.7D;
	private boolean waterProtection = true;
	private boolean checkUpdate = true;
	private int checkitemid = 38;
	private boolean ignorecancel = false;
	private boolean checkmemory = false;
	private static String[] values = new String[21];

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
					Log.info(pluginName + " config file outdated, adding old data and creating new values. Make sure you change those!");
					loadData();
					writeDefault();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			Log.info(pluginName + " config file not found, creating new config file :D");
			this.plugin.saveDefaultConfig();
		}

		return isErrorFree;
	}

	private void loadData() {
		try {

			ChatColor y = ChatColor.YELLOW;
			ChatColor g = ChatColor.GRAY;

			this.bukkitConfig.load(this.configFile);

			this.checkUpdate = this.bukkitConfig.getBoolean("checkupdate", true);
			this.explosionRadius = this.bukkitConfig.getInt("Radius", 3);
			this.waterProtection = this.bukkitConfig.getBoolean("FluidsProtect", true);
			this.checkitemid = this.bukkitConfig.getInt("CheckItemId", 38);
			this.ignorecancel = this.bukkitConfig.getBoolean("IgnoreCancel", false);
			this.checkmemory = this.bukkitConfig.getBoolean("check-memory", false);
			this.bedrockEnabled = this.bukkitConfig.getBoolean("Durability.Bedrock.Enabled", false);

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
			this.bdurability = this.bukkitConfig.getInt("Durability.Bedrock.Durability", 1);
			this.durabilityTimerEnabled = this.bukkitConfig.getBoolean("Durability.ResetEnabled", true);

			this.durabilityTime = readLong("Durability.ResetAfter", "600000");
			this.chanceToDropBlock = this.bukkitConfig.getDouble("Blocks.ChanceToDrop", 0.7D);

			values[0] = y + "checkupdate: " + g + this.checkUpdate;
			values[1] = y + "ExplosionRadius: " + g + this.getRadius();
			values[2] = y + "FluidsProtect: " + g + this.getWaterProtection();
			values[3] = y + "CheckItemId: " + g + this.getCheckItemId();
			values[4] = y + "IgnoreCancel: " + g + this.getIgnoreCancel();
			values[5] = y + "CheckMemory: " + g + this.getCheckMemory();
			values[6] = y + "BedrockEnabled: " + g + this.getBedrockEnabled();
			values[7] = y + "TNTEnabled: " + g + this.getTntEnabled();
			values[8] = y + "CannonsEnabled: " + g + this.getCannonsEnabled();
			values[9] = y + "CreepersEnabled: " + g + this.getCreepersEnabled();
			values[10] = y + "GhastsEnabled: " + g + this.getGhastsEnabled();
			values[11] = y + "WithersEnabled: " + g + this.getWithersEnabled();
			values[12] = y + "DurabilityEnabled: " + g + this.getDurabilityEnabled();
			values[13] = y + "ObsidianDurability: " + g + this.getoDurability();
			values[14] = y + "EnchantmentTableDurability: " + g + this.geteDurability();
			values[15] = y + "EnderchestDurability: " + g + this.getecDurability();
			values[16] = y + "AnvilDurability: " + g + this.getaDurability();
			values[17] = y + "BedrockDurability: " + g + this.getbDurability();
			values[18] = y + "ResetEnabled: " + g + this.getDurabilityEnabled();
			values[19] = y + "ResetAfter: " + g + this.getDurabilityResetTime();
			values[20] = y + "ChanceToDrop: " + g + this.getChanceToDropBlock();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeDefault() {
		write("Version", ObsidianDestroyer.getVersion());
		write("checkupdate", Boolean.valueOf(this.checkUpdate));
		write("Radius", Integer.valueOf(this.explosionRadius));
		write("FluidsProtect", Boolean.valueOf(this.waterProtection));
		write("CheckItemId", Integer.valueOf(this.checkitemid));
		write("IgnoreCancel", Boolean.valueOf(this.ignorecancel));
		write("check-memory", Boolean.valueOf(this.checkmemory));

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
		write("Durability.Bedrock.Enabled", Boolean.valueOf(this.bedrockEnabled));
		write("Durability.Bedrock.Durability", Integer.valueOf(this.bdurability));
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

	public int getbDurability() {
		return this.bdurability;
	}

	public boolean getBedrockEnabled() {
		return this.bedrockEnabled;
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

	public File getConfigFile() {
		return this.configFile;
	}

	public String[] getConfigList() {
		return values;
	}

	public boolean getCheckMemory() {
		return this.checkmemory;
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
			Log.severe("Deleting current durability file and creating new one.");
			this.durabilityFile.delete();

			try {
				this.durabilityFile.createNewFile();
			} catch (IOException exception) {
				Log.severe("Couldn't create new durability file.");
			}
			ioe.printStackTrace();
		} catch (ClassNotFoundException cnfe) {
			Log.severe("Obsidian durability file contains an unknown class, was it modified?");
			cnfe.printStackTrace();
		}

		return map;
	}
}