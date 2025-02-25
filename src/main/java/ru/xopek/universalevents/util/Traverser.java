package ru.xopek.universalevents.util;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;

public class Traverser {
    public static ArrayList<Location> destinateCuboid(Location aLoc, Location bLoc, double step, boolean isSelection2D) {
        ArrayList<Location> destinations = new ArrayList<>();
        World world = aLoc.getWorld();

        double[] xArr = {Math.min(aLoc.getX(), bLoc.getX()), Math.max(aLoc.getX(), bLoc.getX())};
        double[] yArr = {Math.min(aLoc.getY(), bLoc.getY()), Math.max(aLoc.getY(), bLoc.getY())};
        double[] zArr = {Math.min(aLoc.getZ(), bLoc.getZ()), Math.max(aLoc.getZ(), bLoc.getZ())};

        for (double x = xArr[0]; x < xArr[1]; x += step) {
            for (double y : yArr) {
                for (double z : zArr) {
                    destinations.add(new Location(world, x , y , z));
                }
            }
        }
        if(!isSelection2D) {
            for (double y = yArr[0]; y < yArr[1]; y += step) {
                for (double x : xArr) {
                    for (double z : zArr) {
                        destinations.add(new Location(world, x, y, z));
                    }
                }
            }
        }
        for (double z = zArr[0]; z < zArr[1]; z += step) {
            for (double y : yArr) {
                for (double x : xArr) {
                    destinations.add(new Location(world, x , y , z));
                }
            }
        }

        return destinations;
    }

    public static ArrayList<Location> destinateSphere (Location location, double radius,double step,  boolean hollow) {
        ArrayList<Location> destinations = new ArrayList<>();
        World world = location.getWorld();
        int X = location.getBlockX();
        int Y = location.getBlockY();
        int Z = location.getBlockZ();
        double radiusSquared = radius * radius;

        if(hollow){
            for (double x = X - radius; x <= X + radius; x+= step) {
                for (double y = Y - radius; y <= Y + radius; y+= step) {
                    for (double z = Z - radius; z <= Z + radius; z+= step) {
                        if ((X - x) * (X - x) + (Y - y) * (Y - y) + (Z - z) * (Z - z) <= radiusSquared) {
                            Location block = new Location(world, x, y, z);
                            destinations.add(block);
                        }
                    }
                }
            }
            return makeHollow(destinations, true);
        } else {
            for (double x = X - radius; x <= X + radius; x+= step) {
                for (double y = Y - radius; y <= Y + radius; y+= step) {
                    for (double z = Z - radius; z <= Z + radius; z+= step) {
                        if ((X - x) * (X - x) + (Y - y) * (Y - y) + (Z - z) * (Z - z) <= radiusSquared) {
                            Location block = new Location(world, x, y, z);
                            destinations.add(block);
                        }
                    }
                }
            }
            return destinations;
        }
    }
    public static ArrayList<Location> makeHollow(ArrayList<Location> blocks, boolean sphere){
        ArrayList<Location> edge = new ArrayList<>();
        if(!sphere){
            for(Location l : blocks){
                World w = l.getWorld();
                int X = l.getBlockX();
                int Y = l.getBlockY();
                int Z = l.getBlockZ();
                Location front = new Location(w, X + 1, Y, Z);
                Location back = new Location(w, X - 1, Y, Z);
                Location left = new Location(w, X, Y, Z + 1);
                Location right = new Location(w, X, Y, Z - 1);
                if(!(blocks.contains(front) && blocks.contains(back) && blocks.contains(left) && blocks.contains(right))){
                    edge.add(l);
                }
            }
            return edge;
        } else {
            for(Location l : blocks){
                World w = l.getWorld();
                int X = l.getBlockX();
                int Y = l.getBlockY();
                int Z = l.getBlockZ();
                Location front = new Location(w, X + 1, Y, Z);
                Location back = new Location(w, X - 1, Y, Z);
                Location left = new Location(w, X, Y, Z + 1);
                Location right = new Location(w, X, Y, Z - 1);
                Location top = new Location(w, X, Y + 1, Z);
                Location bottom = new Location(w, X, Y - 1, Z);
                if(!(blocks.contains(front) && blocks.contains(back) && blocks.contains(left) && blocks.contains(right) && blocks.contains(top) && blocks.contains(bottom))){
                    edge.add(l);
                }
            }
            return edge;
        }
    }
    public static ArrayList<Location> makeHollowCube(Location min, Location max, double step, boolean isHorizontal) {
        ArrayList<Location> locations = new ArrayList<>();

        double minX = min.getX();
        double minY = min.getY();
        double minZ = min.getZ();
        double maxX = max.getX();
        double maxY = max.getY();
        double maxZ = max.getZ();

        for (double x = minX; x <= maxX; x+= step) {
            for (double y = minY; y <= maxY; y+= step) {
                for (double z = minZ; z <= maxZ; z+= step) {
                    if (x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ) {
                        if(isHorizontal && y == minY || y == maxY)
                            continue;
                            Location location = new Location(min.getWorld(), x, y, z);
                            locations.add(location);
                    }
                }
            }
        }

        return locations;
    }
    public static ArrayList<Location> destinateAB(Location pointA, Location pointB, double spacing) {
        ArrayList<Location> locations = new ArrayList<>();

        World world = pointA.getWorld();

        // Calculate the direction vector from point A to point B
        double dx = pointB.getX() - pointA.getX();
        double dy = pointB.getY() - pointA.getY();
        double dz = pointB.getZ() - pointA.getZ();

        // Calculate the total distance between point A and point B
        double distance = pointA.distance(pointB);

        // Normalize the direction vector
        double normalizedX = dx / distance;
        double normalizedY = dy / distance;
        double normalizedZ = dz / distance;

        // Calculate the number of steps needed to cover the entire distance with the given spacing
        int steps = (int) (distance / spacing);

        // Calculate the increment for each step
        double stepX = normalizedX * spacing;
        double stepY = normalizedY * spacing;
        double stepZ = normalizedZ * spacing;

        // Start at point A
        double x = pointA.getX();
        double y = pointA.getY();
        double z = pointA.getZ();

        // Generate the line by interpolating points along the direction vector
        for (int i = 0; i < steps; i++) {
            x += stepX;
            y += stepY;
            z += stepZ;

            Location location = new Location(world, x, y, z);
            locations.add(location);
        }

        return locations;
    }
    public static ArrayList<Location> destinateCircle(Location loc, double r2x, double step) {
        ArrayList<Location> locations = new ArrayList<>();

        double radius = 6.5;
        double angle = Math.PI;

        boolean breakLoop = false;
        int iterates = 0;
        while(breakLoop || iterates < 2500) {
            if(angle <= -Math.PI) {
                angle = Math.PI;
                breakLoop = true;
            }
            double x = (radius * Math.sin(angle));
            double z = (radius * Math.cos(angle));
            angle -= .1;
            iterates ++;
            locations.add(new Location(loc.getWorld(), loc.getX() + x, loc.getY(), loc.getZ() + z));
        }

        return locations;
    }
    public static ArrayList<Location> destinateHelix(Location from, double loopRadius, double upsideBarrier, double upsideStep) {
        ArrayList<Location> locations = new ArrayList<>();
        double radius = loopRadius;

        for(double y = 0; y <= upsideBarrier; y+=upsideStep)
            locations.add(new Location(
                            from.getWorld(), from.getX() + (radius * Math.cos(y)), from.getY() + y, from.getZ() + (radius * Math.sin(y))
                    )
            );


        return locations;
    }
}
