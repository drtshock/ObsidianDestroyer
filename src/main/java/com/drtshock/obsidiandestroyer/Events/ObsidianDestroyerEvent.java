package com.drtshock.obsidiandestroyer.Events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ObsidianDestroyerEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
