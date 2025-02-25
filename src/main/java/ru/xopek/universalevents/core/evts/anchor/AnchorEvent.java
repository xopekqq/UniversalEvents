package ru.xopek.universalevents.core.evts.anchor;

import com.destroystokyo.paper.ParticleBuilder;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import ru.xopek.universalevents.UniversalEvents;
import ru.xopek.universalevents.core.AbstractEvent;
import ru.xopek.universalevents.core.EventType;
import ru.xopek.universalevents.hologram.UHologram;
import ru.xopek.universalevents.util.MathUtils;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;
import ru.xopek.universalevents.util.StringUtils;
import ru.xopek.universalevents.util.Traverser;
import ru.xopek.universalevents.util.WorldGuardUtils;

import java.util.ArrayList;

public class AnchorEvent implements AbstractEvent {
    private final Location createdLocation;
    @Getter
    private long awaitTicksTillOpen;

    @Getter
    private boolean isTickingStarted;
    @Getter
    private Block createdBlock;
    @Getter
    private UHologram hologram;
    @Getter
    private long ticksStartTimestamp;
    @Getter
    private int id;//2
    private boolean tickingEnded = false;
    private String regionId;
    @Getter
    private Inventory poolInventory;
    private boolean hasExtraLine = false;
    @Getter  private AnchorSubtype anchorSubtype;
    @Getter private EventType eventType = EventType.ANCHOR;
    @Getter private Location minRegionBlock = null, maxRegionBlock = null;
    @Getter private ArrayList<Location> destList;
    @Getter
    private AnchorType anchorType;
    private long lastEffects = -1;
    public AnchorEvent (Location createdLocation, int timeUntillOpen, boolean needsActivate, int id, AnchorSubtype anchorSubtype, AnchorType type) {
        this.createdLocation = createdLocation;
        this.anchorType = type;
        this.anchorSubtype = anchorSubtype;
        this.awaitTicksTillOpen = timeUntillOpen * 1000L;

        this.isTickingStarted = !needsActivate;

        this.id = id;

        this.createNextBlock();

        this.poolInventory = Bukkit.createInventory(null, 54, StringUtils.asColor("&0Якорь"));

        LootManager.fillInventoryWithLoot(this.poolInventory, AnchorType.EPIC);
        this.regionId = null;

        this.setRegion();
    }
    public void triggerAvoidTicking () {
        this.isTickingStarted = true;

        this.ticksStartTimestamp = System.currentTimeMillis();
    }
    public void triggerOpenAction (PlayerInteractEvent evt) {
        var player = evt.getPlayer();
        if(!this.tickingEnded) {
            player.sendMessage(StringUtils.asColor("&cЯкорь еще не открылся!"));
            player.playSound(this.createdLocation, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.0F,1.0F);
        }else {
            player.openInventory(this.poolInventory);
            player.playSound(this.createdLocation, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.0F,1.0F);
        }
    }
    public void createNextBlock () {
        this.createdBlock = this.createdLocation.getBlock();

        this.createdBlock.setType(Material.RESPAWN_ANCHOR);

        RespawnAnchor respawnAnchor = (RespawnAnchor) this.createdBlock.getBlockData();

        respawnAnchor.setCharges(respawnAnchor.getMaximumCharges());

        this.createdBlock.setBlockData(respawnAnchor);

        this.createdBlock.setMetadata("event_id", new FixedMetadataValue(UniversalEvents.getInst(), this.id));


        this.hologram = new UHologram(this.createdLocation.clone().add(0.5,2,0.5));
        if(this.anchorSubtype == AnchorSubtype.DEFAULT)
            this.hologram.addTextLine(StringUtils.asColor("&6&kl&6&kl &#a92ffbЯ&#c725fbк&#e51bfbо&#c725fbр&#a92ffbь &6&kl&6&kl"));
        else {
            this.hologram.addTextLine(StringUtils.asColor("&#8BF6FD&kl&#8BF6FD&kl &#8bf6fdМ&#79f7fdи&#67f8fdр&#54fafdн&#42fbfdы&#30fcfdй &#31f2fdЯ&#32e9fdк&#32dffdо&#33d6fdр&#34ccfdь &#8BF6FD&kl&#8BF6FD&kl"));

        }
        this.hologram.addTextLine(StringUtils.asColor("&fЕще не активирован!"));
    }
    @Override
    public void asyncUpdate() {
        if (this.minRegionBlock != null && System.currentTimeMillis() - this.lastEffects >= 400) {
            this.lastEffects = System.currentTimeMillis();
            for(Location blockLoc : destList) {
                ParticleBuilder pb_ = new ParticleBuilder(Particle.SOUL_FIRE_FLAME)
                        .location(blockLoc.clone())
                        .extra(0)
                        .count(1)
                        .allPlayers();
                pb_.spawn();
            }
        }

        ArrayList<Location> besties = Traverser.destinateCuboid(
                this.createdBlock.getLocation().clone().add(-4,4,-4),
                this.createdBlock.getLocation().clone().add(4.5,0,4.5),
                1,
                false
        );

        for (Location dest : besties) {
            if (Math.random() > .91) {
                ParticleBuilder pb = new ParticleBuilder(Particle.FIREWORKS_SPARK)
                        .location(dest)
                        .extra(0.003)
                        .offset(MathUtils.randomMinMax(-3, 3), MathUtils.randomMinMax(-3, 3), MathUtils.randomMinMax(-3, 3))
                        .count(1)
                        .allPlayers();
                pb.spawn();
            }
        }

    }

    @Override
    public void callDestroy () {
        this.hologram.clearHologram();
        this.createdBlock.setType(Material.AIR);
        this.createdBlock.removeMetadata("event_id", UniversalEvents.getInst());

        /**
         * Region manager && id manager
         */

        if(this.regionId != null) {
            RegionManager rm = WorldGuardUtils.getRegionManagerWithWorld(this.createdBlock.getWorld());

            rm.removeRegion(this.regionId);
        }

    }

    @Override
    public int getEventId() {
        return this.id;
    }

    @Override
    public String getDisplayName() {
        return this.anchorSubtype == AnchorSubtype.MIRNI ? "Мирный Якорь" : "Якорь";
    }
    @Override
    public Location getEventLocation() {
        return createdLocation;
    }
    @Override
    public void update(long now) {
        if(this.isTickingStarted) {
            long differenceInMillis = now - this.ticksStartTimestamp;
            long timeTillOpen = (this.awaitTicksTillOpen - differenceInMillis);

            if(timeTillOpen >= 1) {
                int secondsTillOpen = (int) (timeTillOpen / 1000L);
                this.hologram.setText(1, StringUtils.asColor("&cАктивируется"));

                if(!this.hasExtraLine) {
                    this.hologram.addTextLine(" ");
                    this.hasExtraLine = true;
                }else {
                    this.hologram.setText(2, StringUtils.asColor("&fДо открытия &6{0} сек".replace("{0}", String.valueOf(secondsTillOpen))));

                }
            }else {

                if(this.tickingEnded) {
                    if(timeTillOpen <= -(1000 * 360)) {
                        this.callDestroy();
                        UniversalEvents.getWorker().clearEvent(this);
                    }
                }else {
                    this.tickingEnded = true;

                    this.hologram.setText(1, StringUtils.asColor("&aОткрыт!"));
                    this.hologram.clearTextLine(2);

                    this.fillWithLoot();
                }

            }


        }
    }
    public void fillWithLoot () {

    }
    @Override
    public boolean isActive() {
        return this.isTickingStarted;
    }
    public static AnchorEvent serializeRandomized () {
        var locationAt = MathUtils.getRandomSafeLocation(-1950,1950,-1950,1950);
        var anchorEvent = new AnchorEvent(locationAt, 360, true, UniversalEvents.getWorker().nextId(), UniversalEvents.getWorker().nextSubtype() , AnchorType.randomType());

        return anchorEvent;
    }
    public void setRegion() {

        int bx = this.createdBlock.getLocation().getBlockX();
        int by = this.createdBlock.getLocation().getBlockY();
        int bz = this.createdBlock.getLocation().getBlockZ();
        /**
         * Region manager && id manager
         */
        RegionManager rm = WorldGuardUtils.getRegionManagerWithWorld(this.createdBlock.getWorld());
        this.regionId = WorldGuardUtils.createPSID(bx,by, bz);

        /**
         * Creating CuboidArea
         */
        BlockVector3 min = WorldGuardUtils.getMinVector(bx , by, bz, 10,255,10),
                     max = WorldGuardUtils.getMaxVector(bx, by, bz,10,255,10);

        if(this.anchorSubtype == AnchorSubtype.MIRNI) {


            this.minRegionBlock = new Location(
                    this.getEventLocation().getWorld(),
                    this.createdBlock.getLocation().getX() - 10,
                    Math.max(59, this.createdBlock.getLocation().getY() - 40),
                    this.createdBlock.getLocation().getZ() - 10
            );
            this.maxRegionBlock = new Location(
                    this.getEventLocation().getWorld(),
                    this.createdBlock.getLocation().getX() + 10,
                    Math.min(230, this.createdBlock.getLocation().getY() + 70),
                    this.createdBlock.getLocation().getZ() + 10
            );
            this.destList =
                    Traverser.makeHollowCube(
                            this.minRegionBlock.clone(),
                            this.maxRegionBlock.clone(),
                            2,
                            true
                    );
        }
        /**
         * Some region configs
         */
        ProtectedCuboidRegion region = new ProtectedCuboidRegion(this.regionId, min, max);

        region.setPriority(0);

        /**
         * RG Flags
         */
        region.setFlag(Flags.PVP, this.getAnchorSubtype().equals(AnchorSubtype.MIRNI) ? StateFlag.State.DENY : StateFlag.State.ALLOW);
        region.setFlag(Flags.MOB_DAMAGE, StateFlag.State.ALLOW);
        region.setFlag(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY);
        region.setFlag(Flags.TNT, StateFlag.State.DENY);
        region.setFlag(Flags.PISTONS, StateFlag.State.ALLOW);
        region.setFlag(Flags.USE, StateFlag.State.ALLOW);
        region.setFlag(Flags.CHEST_ACCESS, StateFlag.State.ALLOW);
        region.setFlag(Flags.GREET_MESSAGE, StringUtils.asColor("&#30FCFD[❄] &fВы вошли в регион &#a92ffbЯ&#c725fbк&#e51bfbо&#c725fbр&#a92ffbя"));
        region.setFlag(Flags.FAREWELL_MESSAGE, StringUtils.asColor("&#30FCFD[❄] &fВы вышли из региона &#a92ffbЯ&#c725fbк&#e51bfbо&#c725fbр&#a92ffbя"));
        rm.addRegion(region);

    }
}
