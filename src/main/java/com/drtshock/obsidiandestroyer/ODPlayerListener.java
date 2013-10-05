package com.drtshock.obsidiandestroyer;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @author drtshock
 */
public class ODPlayerListener implements Listener {

    private ODEntityListener odlistener;
    public ODConfig config;

    public ODPlayerListener(ObsidianDestroyer plugin) {
        this.config = plugin.getODConfig();
        this.odlistener = plugin.getListener();
    }

    /**
     * Send a message to OP if there is an update available. Removes the
     * automatic download because that gets annoying and we have NMS calls now
     * that could break if we let it auto update.
     *
     * @param Send OP update message.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (event.getPlayer().hasPermission("obsidiandestroyer.update") && ObsidianDestroyer.UPDATE) {
            event.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "There is a new update for ObsidianDestroyer \\o/");
            event.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Version: " + ChatColor.GRAY + ObsidianDestroyer.NAME
                    + ChatColor.DARK_PURPLE + " is the latest version!");
            event.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Visit " + ChatColor.GRAY + "http://dev.bukkit.org/potato/obsidiandestroyer"
                    + ChatColor.DARK_PURPLE + " to download.");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        if (config.getDurabilityEnabled() && event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            if (player.getItemInHand().getAmount() > 0) {
                if (player.getItemInHand().getType() == Material.RED_ROSE) {

                    Material block = event.getClickedBlock().getType();
                    Location loc = event.getClickedBlock().getLocation();
                    if (block == Material.OBSIDIAN
                            || block == Material.ENDER_CHEST
                            || block == Material.ANVIL
                            || block == Material.ENCHANTMENT_TABLE
                            || block == Material.ENDER_PORTAL
                            || block == Material.ENDER_PORTAL_FRAME) {

                        if (player.getGameMode() == GameMode.CREATIVE) {
                            event.setCancelled(true);
                        }

                        Integer representation = Integer.valueOf(loc.getWorld().hashCode() + loc.getBlockX() * 2389 + loc.getBlockY() * 4027 + loc.getBlockZ() * 2053);
                        int currentDurability = 0;

                        if (odlistener.obsidianDurability.containsKey(representation)) {
                            currentDurability = ((Integer) odlistener.obsidianDurability.get(representation)).intValue();
                        }

                        if (block == Material.OBSIDIAN) {
                            player.sendMessage(ChatColor.DARK_PURPLE + "Durability of this obsidian block is: "
                                    + ChatColor.WHITE + (config.getoDurability() - currentDurability) + "/" + config.getoDurability());
                        }

                        else if (block == Material.ENDER_CHEST) {
                            player.sendMessage(ChatColor.DARK_PURPLE + "Durability of this ender chest is: "
                                    + ChatColor.WHITE + (config.getecDurability() - currentDurability) + "/" + config.getecDurability());
                        }

                        else if (block == Material.ANVIL) {
                            player.sendMessage(ChatColor.DARK_PURPLE + "Durability of this anvil is: "
                                    + ChatColor.WHITE + (config.getaDurability() - currentDurability) + "/" + config.getaDurability());
                        }

                        else if (block == Material.ENCHANTMENT_TABLE) {
                            player.sendMessage(ChatColor.DARK_PURPLE + "Durability of this enchantment table is: "
                                    + ChatColor.WHITE + (config.geteDurability() - currentDurability) + "/" + config.geteDurability());
                        }

                        else if (block == Material.ENDER_PORTAL) {
                            player.sendMessage(ChatColor.DARK_PURPLE + "Durability of this ender portal is: "
                                    + ChatColor.WHITE + (config.getfDurability() - currentDurability) + "/" + config.getfDurability());
                        }

                        else if (block == Material.ENDER_PORTAL_FRAME) {
                            player.sendMessage(ChatColor.DARK_PURPLE + "Durability of this ender portal frame is: "
                                    + ChatColor.WHITE + (config.getepDurability() - currentDurability) + "/" + config.getepDurability());
                        }
                    }
                }
            }
        }
    }
}
