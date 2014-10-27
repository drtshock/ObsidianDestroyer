package com.drtshock.obsidiandestroyer.events;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class xEntityExplodeEvent extends EntityExplodeEvent {

    private List<Block> bypassBlockList;

    public xEntityExplodeEvent(Entity entity, Location location, List<Block> blockList, List<Block> bypassBlockList, float yield) {
        super(entity, location, blockList, yield);
        this.bypassBlockList = bypassBlockList;
    }

    public List<Block> bypassBlockList() {
        return bypassBlockList;
    }

    public void setBypassBlockList(List<Block> bypassBlockList) {
        this.bypassBlockList = bypassBlockList;
    }

    public List<Block> totalBlockList() {
        blockList().addAll(bypassBlockList);

        if (bypassBlockList.size() > 0) {
            bypassBlockList.clear();
        }
        return blockList();
    }
}
