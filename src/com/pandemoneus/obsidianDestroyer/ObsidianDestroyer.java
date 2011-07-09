package com.pandemoneus.obsidianDestroyer;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.pandemoneus.obsidianDestroyer.logger.Log;
import com.pandemoneus.obsidianDestroyer.commands.ODCommands;
import com.pandemoneus.obsidianDestroyer.config.ODConfig;
import com.pandemoneus.obsidianDestroyer.listeners.ODEntityListener;

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
	private final ODEntityListener entityListener = new ODEntityListener();
	private ODConfig config = new ODConfig(this);
	private PermissionHandler permissionsHandler;
	private boolean permissionsFound = false;

	private static final String VERSION = "1.02";
	private static final String PLUGIN_NAME = "ObsidianDestroyer";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDisable() {
		Log.info(PLUGIN_NAME + " disabled");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onEnable() {
		Log.info(PLUGIN_NAME + " v" + VERSION + " enabled");

		getCommand("obsidiandestroyer").setExecutor(cmdExecutor);
		getCommand("od").setExecutor(cmdExecutor);
		setupPermissions();

		config.loadConfig();

		PluginManager pm = getServer().getPluginManager();

		pm.registerEvent(Event.Type.ENTITY_EXPLODE, entityListener,
				Priority.Highest, this);
	}

	/**
	 * Returns the version of the plugin.
	 * 
	 * @return the version of the plugin
	 */
	public static String getVersion() {
		return VERSION;
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
	public ODConfig getConfig() {
		return config;
	}

	/**
	 * Returns whether the permissions plugin could be found.
	 * 
	 * @return true if permissions plugin could be found, otherwise false
	 */
	public boolean getPermissionsFound() {
		return permissionsFound;
	}

	/**
	 * Returns the permissionsHandler of this plugin if it exists.
	 * 
	 * @return the permissionsHandler of this plugin if it exists, otherwise
	 *         null
	 */
	public PermissionHandler getPermissionsHandler() {
		PermissionHandler ph = null;

		if (getPermissionsFound()) {
			ph = permissionsHandler;
		}

		return ph;
	}

	private void setupPermissions() {
		if (permissionsHandler != null) {
			return;
		}

		Plugin permissionsPlugin = getServer().getPluginManager().getPlugin(
				"Permissions");

		if (permissionsPlugin == null) {
			Log.warning("Permissions not detected, using normal command structure.");
			return;
		}

		permissionsFound = true;
		permissionsHandler = ((Permissions) permissionsPlugin).getHandler();
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
