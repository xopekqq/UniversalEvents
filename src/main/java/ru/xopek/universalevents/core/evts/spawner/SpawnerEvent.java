package ru.xopek.universalevents.core.evts.spawner;

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
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import ru.xopek.universalevents.util.StringUtils;
import ru.xopek.universalevents.util.Traverser;
import ru.xopek.universalevents.util.WorldGuardUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

@Getter
public class SpawnerEvent implements AbstractEvent {
    @Setter
    private Location eventLocation;
    @Getter
    private int eventId, nextSubId = 1;
    @Getter
    private PhaseState phaseState;
    @Getter
    private String regionId;
    @Getter private EventType eventType = EventType.SPAWNER;
    @Getter
    private Block createdBlock;
    private long lastParticles = -1;
    @Getter
    private UHologram hologram = null;
    @Getter
    private long phaseDuration = -1, lastPhaseRult = -1;
    @Getter
    private int durabillity = -1, maxDurabillity = 100;
    @Getter
    private HashSet<Location> freeSpots = new HashSet<>();
    @Getter
    private HashMap<Integer, SpawnedEntity> spawnedEntities = new HashMap<>();
    public SpawnerEvent (int eventId, Location spawnLocation) {
        this.eventLocation = spawnLocation;
        this.eventId = eventId;
        this.durabillity = this.maxDurabillity;

        this.phaseState = UniversalEvents
                .getConfigurations()
                .registeredPhaseStates
                .get("INACTIVE");

        this.createNextBlock();

        this.regionId = null;

        this.setRegion();
    }
    public void triggerClickAction (PlayerInteractEvent evt) {
        if(this.phaseState.stateId != 0) {
            return;
        }

        this.phaseState = UniversalEvents
                .getConfigurations()
                .registeredPhaseStates
                .get("FIRST_PHASE");

        this.onPhaseUpdated(false);
    }
    public void createNextBlock () {
        this.createdBlock = this.eventLocation.getBlock();

        this.createdBlock.setType(Material.SPAWNER);

        this.createdBlock.setMetadata("event_id", new FixedMetadataValue(UniversalEvents.getInst(), this.eventId));

        this.hologram = new UHologram(this.eventLocation.clone().add(0.5,2,0.5));
        this.hologram.addTextLine(StringUtils.asColor("&#F6FB1A&k&ll " + this.getDisplayName() + " &#F6FB1A&k&ll"));
        this.hologram.addTextLine(StringUtils.asColor("&fНе активен!"));

        Location downRow = this.createdBlock.getLocation().clone().add(2,0.5,0);
        this.freeSpots.add(downRow);

        Location secondRow = this.createdBlock.getLocation().clone().add(-2,0.5,0);
        this.freeSpots.add(secondRow);

        Location thirdRow = this.createdBlock.getLocation().clone().add(0,0.5,2);
        this.freeSpots.add(thirdRow);

        Location fourRow = this.createdBlock.getLocation().clone().add(0,0.5,-2);
        this.freeSpots.add(fourRow);
    }
    public void onEntityDied (int entityId , LivingEntity ent) {
        SpawnedEntity entity = this.spawnedEntities.get(entityId);

        if(entity == null)
            return;

        entity.onDied();
    }
    public void onBreak (BlockBreakEvent evt) {
        evt.setCancelled(true);
        evt.setExpToDrop(0);
        evt.setDropItems(false);

        Player player = evt.getPlayer();
        boolean isMobsKilledEnough = this.spawnedEntities.isEmpty();
        boolean isFlushed = System.currentTimeMillis() - this.phaseDuration >= 1000 * 180;

        if(this.getPhaseState().getStateId() < 5 || (!isMobsKilledEnough && !isFlushed)) {
            if(System.currentTimeMillis() - lastParticles >= 600) {

                if(this.phaseState.getStateId() < 5)
                    player.sendMessage(StringUtils.asColor(this.getDisplayName() + " &7| &fСпавнер еще не достиг &c5-ой &fфазы!"));
                else {
                    player.sendMessage(StringUtils.asColor(this.getDisplayName() + "&7 | &eУбейте всех мобов!"));
                }

                player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, .6F, 1.75F);

                lastParticles = System.currentTimeMillis();
                for (double d1 = 0; d1 < 1; d1 += .1) {
                    ParticleBuilder particleBuilder = new ParticleBuilder(Particle.SMOKE_NORMAL)
                            .location(this.createdBlock.getLocation().clone().add(.5, 0, .5))
                            .extra(0.04)
                            .offset(Math.random() / 5, .6 + d1, Math.random() / 5)
                            .count(15)
                            .allPlayers();

                    particleBuilder.spawn();
                }
            }

            return;
        }

        this.durabillity = Math.max(0, this.durabillity - 1);

        if(this.durabillity <= 0) {
            this.scheduleBoom();
        }
    }

    /**
     * Ends event here ../
     */
    public void scheduleBoom () {
        this.createdBlock.setType(Material.AIR);
        this.hologram.clearHologram();

        this.createdBlock.getWorld()
                .createExplosion(this.eventLocation, 4f, false,false);
        ParticleBuilder pb = new ParticleBuilder(Particle.EXPLOSION_HUGE)
                .count(1)
                .location(this.createdBlock.getLocation())
                .allPlayers();

        pb.spawn();

        ArrayList<Location> besties = Traverser.destinateCuboid(
                this.eventLocation.clone().add(-2,-2,-2),
                this.eventLocation.clone().add(2,2,2),
                0.25,
                false
        );

        for(Player player : this.eventLocation.getNearbyPlayers(15,15,15)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 1));
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, .6F, 1);
        }

        for(Location dest : besties) {
            ParticleBuilder pb_ = new ParticleBuilder(Particle.SOUL_FIRE_FLAME)
                    .location(dest)
                    .extra(0.03)
                    .offset(MathUtils.randomMinMax(-3,3), MathUtils.randomMinMax(-3,3), MathUtils.randomMinMax(-3,3))
                    .count(1)
                    .allPlayers();
            pb_.spawn();
        }


        for(int i = 0; i < 30; i++) {
            ItemStack item = SpawnerDropper.getItemToDrop(this.phaseState);
            if(item == null)
                continue;
            Item item_ = this.createdBlock.getWorld().dropItem(this.createdBlock.getLocation().clone().add(0,0.5,0), item);

            double randomX = (-(Math.random() / 1.8)) + Math.random() / 1.3;
            double randomZ = (-(Math.random() / 1.8)) + Math.random() / 1.3;

            item_.setVelocity(
                    item_.getVelocity()
                            .add(new Vector(randomX, 0.3, randomZ))
            );
        }


        UniversalEvents.getWorker().clearEvent(this);
        this.callDestroy();

    }
    public void onPhaseUpdated (boolean doNext) {
        if(doNext) {
            PhaseState phaseState_ = UniversalEvents
                    .getConfigurations()
                    .registeredPhaseStates
                    .values()
                    .stream()
                    .filter(p -> p.stateId == (this.phaseState.stateId + 1))
                    .findFirst()
                    .orElse(null);

            if(phaseState_ == null) {
                return;
            }

            this.phaseState = phaseState_;
        }
        this.phaseDuration = System.currentTimeMillis();

        long phaseDiff = System.currentTimeMillis() - this.phaseDuration;
        int duration = (int) ((this.phaseState.timeInMillis - phaseDiff) / 1000);

        /**
         * First phase
         */
        if(this.phaseState.stateId == 1) {
            this.hologram.setText(1, StringUtils.asColor(this.phaseState.getHologramName()));
            this.hologram.addTextLine(StringUtils.asColor("&fОсталось: &#fb9419" + duration + " сек"));
        }
        switch (this.phaseState.stateId) {
            case 1,2,3,4 -> {
                for(Location spot : this.freeSpots) {
                    int entityId = (this.eventId * 1000) + (++this.nextSubId);

                    this.spawnedEntities.put(entityId, new SpawnedEntity(
                            this,
                            spot,
                            randomSpawnableType(),
                            this.phaseState,
                            entityId
                    ));

                    entityId = (this.eventId * 1000) + (++this.nextSubId);
                    this.spawnedEntities.put(entityId, new SpawnedEntity(
                            this,
                            spot,
                            randomSpawnableType(),
                            this.phaseState,
                            entityId
                    ));
                }
            }
            case 5 -> {
                this.hologram.setText(1, StringUtils.asColor(this.phaseState.getHologramName()));
            }
        }
    }
    public static SpawnerEvent serializeRandomized () {
        var locationAt = MathUtils.getRandomSafeLocation(-1950,1950,-1950,1950);
        var spawner = new SpawnerEvent(UniversalEvents.getWorker().nextId(), locationAt);

        return spawner;
    }
    @Override
    public void update(long now) {
        if(this.createdBlock.getType() != Material.SPAWNER)
            return;

        /**
         * Just random mob here
         */

        CreatureSpawner cs = (CreatureSpawner) this.createdBlock.getState();
        cs.setSpawnedType(randomSpawnerType());
        cs.update(true, false);

        long phaseDiff = System.currentTimeMillis() - this.phaseDuration;
        int duration = (int) ((this.phaseState.timeInMillis - phaseDiff) / 1000);

        if(this.phaseState.timeInMillis != -1) {
            if(duration <= 0) {
                this.onPhaseUpdated(true);
            }
        }

        for (SpawnedEntity ent : this.spawnedEntities.values()) {
            Location spawnLoc = ent.getSpawnLocation();

            if(ent.getLivingEntity().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                ent.getLivingEntity().removePotionEffect(PotionEffectType.INVISIBILITY);
            }

            if (ent.getLivingEntity() instanceof Monster mnst) {
                if(ent.getLivingEntity().getType() == EntityType.CREEPER) {
                    Creeper creeper = (Creeper) mnst;
                    creeper.setIgnited(false);
                    creeper.setPowered(false);
                    creeper.setFuseTicks(0);
                    creeper.setMaxFuseTicks(9999);
                }

                LivingEntity ent_ = mnst.getTarget();

                if(ent_ != null && ent_.getType() != EntityType.PLAYER) {
                    mnst.setTarget(null);
                }
            }
            if(ent.getLivingEntity().getLocation().distance(spawnLoc) >= 12) {
                ent.getLivingEntity().teleport(spawnLoc);
            }
        }

        if(this.phaseState.stateId != 0 && this.phaseState.timeInMillis != -1) {
            this.hologram.setText(1, StringUtils.asColor(this.phaseState.getHologramName()));
            this.hologram.setText(2, StringUtils.asColor("&fОсталось: &#fb9419" + duration + " сек"));
        }
        if(this.phaseState.stateId >= 5) {
            boolean isFlushed = System.currentTimeMillis() - this.phaseDuration >= 1000 * 180;

            if(isFlushed || this.getSpawnedEntities().isEmpty()) {
                this.hologram.setText(2, "&fПрочность: " + this.getColorForDurabillity() + this.durabillity);
            }else {
                if(System.currentTimeMillis() - this.phaseDuration >= 1000 * 180) {
                    return;
                }
                this.hologram.setText(2, "&eУбейте всех мобов!");

            }
        }
    }
    @Override
    public void asyncUpdate () {
        this.updateEffects();
    }
    public void updateEffects() {
        ArrayList<Location> besties = Traverser.destinateCuboid(
                this.createdBlock.getLocation().clone().add(-2,2,-2),
                this.createdBlock.getLocation().clone().add(2.5,0,2.5),
                1,
                false
        );

        switch (this.getPhaseState().getStateId()) {
            case 0,1 -> {
                for (Location dest : besties) {
                    if (Math.random() > .87) {
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
            case 2,3 -> {
                for(Location dest1 : besties) {
                    ParticleBuilder pb = new ParticleBuilder(Particle.SMOKE_LARGE)
                            .location(dest1)
                            .extra(0.003)
                            .offset(MathUtils.randomMinMax(-3, 3), MathUtils.randomMinMax(-3, 3), MathUtils.randomMinMax(-3, 3))
                            .count(1)
                            .allPlayers();
                    pb.spawn();
                }
            }
            default -> {
                for(Location dest1 : besties) {
                    ParticleBuilder pb = new ParticleBuilder(Particle.FLAME)
                            .location(dest1)
                            .extra(0.003)
                            .offset(MathUtils.randomMinMax(-3, 3), MathUtils.randomMinMax(-3, 3), MathUtils.randomMinMax(-3, 3))
                            .count(1)
                            .allPlayers();
                    pb.spawn();
                }
            }
        }
    }

    @Override
    public boolean isActive() {
        return this.phaseState.stateId != 0;
    }

    @Override
    public void callDestroy() {
        for(SpawnedEntity ent : this.spawnedEntities.values()) {
            ent.removeEntity();
        }
        if(this.regionId != null) {
            RegionManager rm = WorldGuardUtils.getRegionManagerWithWorld(this.createdBlock.getWorld());

            rm.removeRegion(this.regionId);

        }
        if(this.hologram != null)
            this.hologram.clearHologram();

        this.createdBlock.setType(Material.AIR);
        this.createdBlock.removeMetadata("event_id", UniversalEvents.getInst());

    }

    @Override
    public String getDisplayName() {
        return StringUtils.asColor("&#fb9419С&#f9b619п&#f8d91aа&#f6fb1aв&#f8d91aн&#f9b619е&#fb9419р");
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

        /**
         * Some region configs
         */
        ProtectedCuboidRegion region = new ProtectedCuboidRegion(this.regionId, min, max);

        region.setPriority(0);

        /**
         * RG Flags
         */
        region.setFlag(Flags.PVP, StateFlag.State.ALLOW);
        region.setFlag(Flags.MOB_DAMAGE, StateFlag.State.ALLOW);
        region.setFlag(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY);
        region.setFlag(Flags.TNT, StateFlag.State.DENY);
        region.setFlag(Flags.PISTONS, StateFlag.State.ALLOW);
        region.setFlag(Flags.USE, StateFlag.State.ALLOW);
        region.setFlag(Flags.CHEST_ACCESS, StateFlag.State.ALLOW);
        region.setFlag(Flags.GREET_MESSAGE, StringUtils.asColor("&fВы вошли в регион &#fb9419С&#f9b619п&#f8d91aа&#f6fb1aв&#f8d91aн&#f9b619е&#fb9419ра"));
        rm.addRegion(region);
    }
    public static EntityType[] possibleEntities = { EntityType.WITHER_SKELETON, EntityType.SKELETON, EntityType.BLAZE, EntityType.ZOMBIE, EntityType.BEE, EntityType.ENDERMAN, EntityType.CHICKEN, EntityType.WITCH, EntityType.PIG, EntityType.COW, EntityType.CREEPER, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.STRIDER, EntityType.CAT, EntityType.PHANTOM};
    public static EntityType[] spawnableEntities = { EntityType.WITHER_SKELETON, EntityType.ZOMBIE, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.BLAZE, EntityType.CREEPER, EntityType.SKELETON };

    public static EntityType randomSpawnableType() {
        return spawnableEntities[(int) Math.floor(Math.random() * spawnableEntities.length)];
    }
    public static EntityType randomSpawnerType () {
        return possibleEntities[(int) Math.floor(Math.random() * possibleEntities.length)];
    }

    public String getColorForDurabillity () {
        if(this.durabillity >= (this.maxDurabillity / 1.1))
            return "&#14FB2C";
        if(this.durabillity >= (this.maxDurabillity / 1.35)) {
            return "&#22FB00";
        }
        if(this.durabillity >= (this.maxDurabillity / 1.5)) {
            return "&#40FB00";
        }
        if(this.durabillity >= (this.maxDurabillity / 2)) {
            return "&#6DFB00";
        }
        if(this.durabillity >= (this.maxDurabillity / 3)) {
            return "&#C8FB00";
        }
        if(this.durabillity >= (this.maxDurabillity / 4)) {
            return "&#FBE40F";
        }
        if(this.durabillity >= (this.maxDurabillity / 6)) {
            return "&#FBB90D";
        }
        if(this.durabillity >= (this.maxDurabillity / 8)) {
            return "&#FB8C08";
        }
        if(this.durabillity >= (this.maxDurabillity / 10)) {
            return "&#FB5005";
        }
        return "&#FB0000";
    }
}
