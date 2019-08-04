package com.drtshock.obsidiandestroyer.listeners;

import com.drtshock.obsidiandestroyer.managers.ChunkManager;
import com.drtshock.obsidiandestroyer.managers.ConfigManager;
import com.drtshock.obsidiandestroyer.managers.MaterialManager;
import com.drtshock.obsidiandestroyer.util.Util;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack inHand = player.getItemInHand();
        // They no longer throw the interact event if the player is in creative, so lets do right click.
        if (inHand == null || event.getAction() != Action.RIGHT_CLICK_BLOCK || !event.hasBlock()
                || !player.hasPermission("obsidiandestroyer.check")) {
            return;
        }

        Material type = inHand.getType();
        Block block = event.getClickedBlock();
        if (!ConfigManager.getInstance().getDurabilityCheckItem().equals(type)) {
            return;
        }

        MaterialManager mm = MaterialManager.getInstance();
        if (block != null && mm.getDurabilityEnabled(block.getType().name())) {
            int amount = ChunkManager.getInstance().getMaterialDurability(block);
            double mult = Util.getMultiplier(block.getLocation());
            if (mult == 0) {
                player.sendMessage(ConfigManager.getInstance().getDurabilityMessage().replace("{DURABILITY}", "∞"));
            } else {
                int max = (int) Math.round(mm.getDurability(block.getType().name(), block.getData()) * Util.getMultiplier(block.getLocation()));
                player.sendMessage(ConfigManager.getInstance().getDurabilityMessage().replace("{DURABILITY}", !mm.isDestructible(block.getType().name(), block.getData()) ? "∞" : (max - amount) + "/" + max));
            }
        }
    }
}
