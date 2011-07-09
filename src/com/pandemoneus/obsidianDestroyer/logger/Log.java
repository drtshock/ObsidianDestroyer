package com.pandemoneus.obsidianDestroyer.logger;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.pandemoneus.obsidianDestroyer.ObsidianDestroyer;

/**
 * Server logger class
 * 
 * @author Pandemoneus
 * 
 */
public final class Log {
	private static String pre = "[" + ObsidianDestroyer.getPluginName() + "] ";
	private static final Logger LOG = Logger.getLogger("Minecraft");

	private Log() {

	}

	/**
	 * Displays a info message in the bukkit console.
	 * 
	 * @param message
	 *            the message to display
	 */
	public static void info(String message) {
		LOG.log(Level.INFO, pre + message);
	}

	/**
	 * Displays a warning message in the bukkit console.
	 * 
	 * @param message
	 *            the message to display
	 */
	public static void warning(String message) {
		LOG.log(Level.WARNING, pre + message);
	}

	/**
	 * Displays a message with severe level in the bukkit console.
	 * 
	 * @param message
	 *            the message to display
	 */
	public static void severe(String message) {
		LOG.log(Level.SEVERE, pre + message);
	}
}
