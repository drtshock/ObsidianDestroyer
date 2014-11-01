package com.drtshock.obsidiandestroyer.listeners;

import com.drtshock.obsidiandestroyer.ObsidianDestroyer;
import com.drtshock.obsidiandestroyer.managers.ChunkManager;
import com.drtshock.obsidiandestroyer.managers.MaterialManager;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class BlockListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block == null) {
            return;
        }
        if (ChunkManager.getInstance().contains(block)) {
            ChunkManager.getInstance().removeBlock(block);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        ChunkManager.getInstance().loadChunk(event.getChunk());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {
        ChunkManager.getInstance().unloadChunk(event.getChunk());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (ChunkManager.getInstance().contains(event.getBlock()) && !MaterialManager.getInstance().contains(event.getBlock().getType().name())) {
            ChunkManager.getInstance().removeBlock(event.getBlock());
            ObsidianDestroyer.vdebug("BlockPhysics update removed ghosted block!");
        }
    }
}
