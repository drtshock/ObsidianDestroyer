package com.drtshock.obsidiandestroyer.events;

import com.drtshock.obsidiandestroyer.enumerations.DamageResult;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class DurabilityDamageEvent extends ObsidianDestroyerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean canceled = false;

    private boolean isDisposed = false;

    private String blockType;
    private EntityType entityType;
    private DamageResult damageResult = DamageResult.NONE;

    public DurabilityDamageEvent(String blockType, EntityType entityType) {
        this.blockType = blockType;
        this.entityType = entityType;
    }

    public DurabilityDamageEvent(String blockType) {
        this.blockType = blockType;
        this.entityType = null;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public String getBlockType() {
        return blockType;
    }

    public boolean isDisposed() {
        return isDisposed;
    }

    public DamageResult getDamageResult() {
        return damageResult;
    }

    public void setDamageResult(DamageResult damageResult) {
        this.damageResult = damageResult;
        isDisposed = true;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return canceled;
    }

    @Override
    public void setCancelled(boolean value) {
        canceled = value;
    }
}
