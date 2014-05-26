package com.drtshock.obsidiandestroyer.listeners;

import com.drtshock.obsidiandestroyer.ObsidianDestroyer;
import com.drtshock.obsidiandestroyer.managers.ChunkManager;
import com.drtshock.obsidiandestroyer.managers.ConfigManager;
import com.drtshock.obsidiandestroyer.managers.MaterialManager;
import com.drtshock.obsidiandestroyer.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (ObsidianDestroyer.getInstance().getNeedsUpdate() && event.getPlayer().hasPermission("obsidiandestroyer.admin")) {
            event.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Version " + ChatColor.GRAY + ObsidianDestroyer.getInstance().getLatestVersion() + ChatColor.DARK_PURPLE + " is available for update.");
            event.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Download it from http://dev.bukkit.org/potato/obsidiandestroyer");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getItemInHand().getAmount() > 0 && event.getAction() == Action.LEFT_CLICK_BLOCK && event.hasBlock()) {
            Material itemInHand = player.getItemInHand().getType();
            Block block = event.getClickedBlock();
            if (ConfigManager.getInstance().getDurabilityCheckItem().equals(itemInHand) && player.hasPermission("obsidiandestroyer.check")) {
                MaterialManager mm = MaterialManager.getInstance();
                if (mm.getDurabilityEnabled(block.getType().name())) {
                    if (player.getGameMode() == GameMode.CREATIVE) {
                        event.setCancelled(true);
                    }
                    int amount = ChunkManager.getInstance().getMaterialDurability(block);
                    int max = (int) Math.round(mm.getDurability(block.getType().name()) * Util.getMultiplier(block.getLocation()));
                    player.sendMessage(ChatColor.DARK_PURPLE + "Durability of this block is: " + ChatColor.WHITE + (max - amount) + "/" + max);
                }
            }
        }
    }
}
