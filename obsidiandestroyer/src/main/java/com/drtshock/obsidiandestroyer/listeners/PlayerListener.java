package com.drtshock.obsidiandestroyer.listeners;

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

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        try {
            if (player.getItemInHand().getAmount() > 0 && event.getAction() == Action.LEFT_CLICK_BLOCK && event.hasBlock()) {
                if (!player.hasPermission("obsidiandestroyer.check")) {
                    return;
                }
                Material itemInHand = player.getItemInHand().getType();
                Block block = event.getClickedBlock();
                if (itemInHand != null && ConfigManager.getInstance().getDurabilityCheckItem().equals(itemInHand)) {
                    MaterialManager mm = MaterialManager.getInstance();
                    if (block != null && mm.getDurabilityEnabled(block.getType().name(), block.getData())) {
                        if (player.getGameMode() == GameMode.CREATIVE) {
                            event.setCancelled(true);
                        }
                        int amount = ChunkManager.getInstance().getMaterialDurability(block);
                        double mult = Util.getMultiplier(block.getLocation());
                        if (mult == 0) {
                            player.sendMessage(ChatColor.DARK_PURPLE + "Durability of this block is: " + ChatColor.WHITE + "∞");
                        } else {
                            int max = (int) Math.round(mm.getDurability(block.getType().name(), block.getData()) * Util.getMultiplier(block.getLocation()));
                            player.sendMessage(ChatColor.DARK_PURPLE + "Durability of this block is: " + ChatColor.WHITE + (!mm.isDestructible(block.getType().name(), block.getData()) ? "∞" : (max - amount) + "/" + max));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
