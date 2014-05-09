package com.drtshock.obsidiandestroyer.datatypes;

import org.bukkit.entity.EntityType;

public class EntityData {

    private final EntityType type;

    public EntityData(EntityType entityType) {
        this.type = entityType;
    }

    public EntityType getEntityType() {
        return type;
    }
}
