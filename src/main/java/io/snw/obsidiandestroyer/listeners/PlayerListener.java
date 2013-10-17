package io.snw.obsidiandestroyer.listeners;

import io.snw.obsidiandestroyer.ObsidianDestroyer;
import io.snw.obsidiandestroyer.managers.BlockManager;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
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

    private ObsidianDestroyer plugin;

    public PlayerListener(ObsidianDestroyer p) {
        this.plugin = p;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (plugin.getNeedsUpdate()) {
            event.getPlayer().sendMessage("An update is available!");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getItemInHand().getAmount() > 0 && event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Material itemInHand = player.getItemInHand().getType();
            Block block = event.getClickedBlock();
            if (itemInHand == Material.RED_ROSE) {
                BlockManager bm = BlockManager.getInstance();
                Location loc = block.getLocation();
                if (bm.getDurabilityEnabled(block.getType().name())) {
                    if (player.getGameMode() == GameMode.CREATIVE) {
                        event.setCancelled(true);
                    }
                    int amount = 0;
                    int max = bm.getDurability(block.getType().name());
                    Integer representation = Integer.valueOf(loc.getWorld().hashCode() + loc.getBlockX() * 2389 + loc.getBlockY() * 4027 + loc.getBlockZ() * 2053);
                    if (bm.getMaterialDurability().containsKey(representation)) {
                        amount = ((Integer) bm.getMaterialDurability(representation).intValue());
                        player.sendMessage(ChatColor.DARK_PURPLE + "Durability of this block is: "
                                + ChatColor.WHITE + (max - amount) + "/" + max);
                    } else {
                        player.sendMessage(ChatColor.DARK_PURPLE + "Durability of this block is: "
                                + ChatColor.WHITE + max + "/" + max);
                    }
                }
            }
        }
    }
}
