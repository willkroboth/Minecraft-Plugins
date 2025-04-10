package me.willkroboth.platformspawner;

import org.bukkit.Location;
import org.bukkit.World;

public record IntegerLocation(World world, int x, int y, int z) {
    public static IntegerLocation fromLocation(Location location) {
        return new IntegerLocation(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public Location toLocation() {
        return new Location(world, x, y, z);
    }
}
