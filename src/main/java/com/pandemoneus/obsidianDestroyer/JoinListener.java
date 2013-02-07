package com.pandemoneus.obsidianDestroyer;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener
{

	ObsidianDestroyer plugin;

	public JoinListener(ObsidianDestroyer plugin)
	{
		this.plugin = plugin;
	}

	/**
	 * Send a message to OP if there is an update available.
	 * Removes the automatic download because that gets annoying
	 * and we have NMS calls now that could break if we let it auto update.
	 * @param Send OP update message.
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event)
	{
		if(event.getPlayer().isOp() && ObsidianDestroyer.update)
		{
			event.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "There is a new update for ObsidianDestroyer!");
			event.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Version: " + ChatColor.GRAY + ObsidianDestroyer.name + 
					ChatColor.DARK_PURPLE + " is the latest version!");
			event.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Visit" + ChatColor.GRAY + "http://dev.bukkit.org/obsidiandestroyer" + 
					ChatColor.DARK_PURPLE + " to download.");
		}
		return;
	}
}
