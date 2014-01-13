package io.snw.obsidiandestroyer.datatypes;

public class DamageResult {

    private final boolean damage;
    private final boolean destroy;

    public DamageResult(boolean damage, boolean destroy) {
        this.damage = damage;
        this.destroy = destroy;
    }

    public boolean isDamage() {
        return damage;
    }

    public boolean isDestroyed() {
        return destroy;
    }
}
