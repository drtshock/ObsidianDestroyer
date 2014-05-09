package com.drtshock.obsidiandestroyer.datatypes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class Key {

    public final String world;
    public final int x, y, z;
    public final int durabilityAmount;
    public final long durabilityTime;

    public Key(Location location, int durabilityAmount, long durabilityTime) {
        this(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), durabilityAmount, durabilityTime);
    }

    public Key(Location location, int durabilityAmount) {
        this(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), durabilityAmount);
    }

    public Key(String world, int x, int y, int z, int durabilityAmount) {
        this(world, x, y, z, durabilityAmount, 0L);
    }

    public Key(String world, int x, int y, int z, int durabilityAmount, long durabilityTime) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.durabilityAmount = durabilityAmount;
        this.durabilityTime = durabilityTime;
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }

    public Location toLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Key) {
            Key other = (Key) object;
            return this.world.equals(other.world) && this.x == other.x && this.y == other.y && this.z == other.z;
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + toLocation().hashCode();
        return result;
    }
}
