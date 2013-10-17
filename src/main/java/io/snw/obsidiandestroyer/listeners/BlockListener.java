package io.snw.obsidiandestroyer.listeners;

import io.snw.obsidiandestroyer.ObsidianDestroyer;
import io.snw.obsidiandestroyer.managers.BlockManager;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockListener implements Listener {

    public BlockListener(ObsidianDestroyer obsidianDestroyer) {
        // TODO Auto-generated constructor stub
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Integer representation = block.getWorld().hashCode() + block.getX() * 2389 + block.getY() * 4027 + block.getZ() * 2053;
        if (BlockManager.getInstance().getMaterialDurability().containsKey(representation)) {
            BlockManager.getInstance().removeMaterial(representation);
        }
    }
}