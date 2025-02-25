package ru.xopek.universalevents.util;

import org.bukkit.*;
import org.bukkit.block.Block;

public class MathUtils {
    public static int randomMinMax (int min , int max) {
        return (int) (min + Math.floor(Math.random() * (max - min)));
    }
    public static Location getRandomSafeLocation (int minx , int maxx , int minz , int maxz) {
        int x = MathUtils.randomMinMax(minx,maxx);
        int z = MathUtils.randomMinMax(minz,maxz);

        int attempts = 0;
        World world = Bukkit.getWorld("world");

        int highestY = world.getHighestBlockYAt(x,z) + 1;

        Location teleportLocation = new Location(world,x,highestY,z);
        Block toTeleport = teleportLocation.getBlock();

        while ((toTeleport.getLocation().clone().subtract(0,1,0).getBlock().getType() == Material.WATER || toTeleport.getLocation().clone().subtract(0,1,0).getBlock().getType() == Material.LAVA) && attempts < 35) {
            attempts ++;

            x = MathUtils.randomMinMax(minx,maxx);
            z = MathUtils.randomMinMax(minz,maxz);

            highestY = world.getHighestBlockYAt(x,z) + 1;
            teleportLocation = new Location(world,x,highestY,z);
            toTeleport = teleportLocation.getBlock();

        }
        Chunk c = world.getChunkAt(toTeleport);

        if(!c.isLoaded())
            c.load();

        return toTeleport.getLocation();
    }
}
