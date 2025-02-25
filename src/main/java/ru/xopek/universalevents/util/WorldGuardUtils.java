package ru.xopek.universalevents.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class WorldGuardUtils {
    public static String createPSID (int x , int y , int z) {
        return "unievts" + x + "x" + y + "y" + z + "z";
    }
    public static BlockVector3 getMinVector(double bx, double by, double bz, long xRadius, long yRadius, long zRadius) {
        if (yRadius == -1L) {
            return BlockVector3.at(bx - (double) xRadius, 0.0, bz - (double) zRadius);
        }
        return BlockVector3.at(bx - (double) xRadius, by - (double) yRadius, bz - (double) zRadius);
    }
    public static RegionManager getRegionManagerWithPlayer(Player p) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(p.getWorld()));
    }
    public static ApplicableRegionSet getApplicableRegions(Location loc) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager;
        if (container == null || (manager = container.get(BukkitAdapter.adapt(loc.getWorld()))) == null) {
            return null;
        }
        return manager.getApplicableRegions(
                BukkitAdapter.asBlockVector(loc));
    }
    public static RegionManager getRegionManagerWithWorld(World w) {
        if (w == null) {
            return null;
        }
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(w));
    }

    public static HashMap<World, RegionManager> getAllRegionManagers() {
        HashMap<World, RegionManager> m = new HashMap<World, RegionManager>();
        for (World w : Bukkit.getWorlds()) {
            RegionManager rgm = WorldGuardUtils.getRegionManagerWithWorld(w);
            if (rgm == null) continue;
            m.put(w, rgm);
        }
        return m;
    }

    public static BlockVector3 getMaxVector(double bx, double by, double bz, long xRadius, long yRadius, long zRadius) {
        if (yRadius == -1L) {
            return BlockVector3.at(bx + (double)xRadius, 256.0, bz + (double)zRadius);
        }
        return BlockVector3.at(bx + (double)xRadius, by + (double)yRadius, bz + (double)zRadius);
    }


    public static boolean hasRegionWithName (Player player , String name) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

        ApplicableRegionSet rs = query.getApplicableRegions(localPlayer.getLocation());

        for(ProtectedRegion reg : rs) {
            if(reg.getId().equals(name)) return true;
        }
        return false;
    }
    public static ApplicableRegionSet getApplicableRegions (Player player) {

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

        ApplicableRegionSet rs = query.getApplicableRegions(localPlayer.getLocation());

        return rs;
    }
    public static boolean isPvPEnabled (Player player) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

        if(!query.testState(localPlayer.getLocation(), localPlayer, Flags.PVP))
            return false;

        return true;
    }
}
