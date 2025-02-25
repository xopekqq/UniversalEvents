package ru.xopek.universalevents;

import com.destroystokyo.paper.ParticleBuilder;
import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent;
import ru.xopek.universalevents.api.EventsAPI;
import ru.xopek.universalevents.core.AbstractEvent;
import ru.xopek.universalevents.core.EventType;
import ru.xopek.universalevents.core.evts.anchor.AnchorEvent;
import ru.xopek.universalevents.core.evts.spawner.SpawnedEntity;
import ru.xopek.universalevents.core.evts.spawner.SpawnerEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.xopek.universalevents.util.MathUtils;
import ru.xopek.universalevents.util.StringUtils;
import ru.xopek.universalevents.util.Traverser;

import java.util.ArrayList;

public class EventProcessor implements Listener {

    @EventHandler
    public void spawnerSpawn (PreSpawnerSpawnEvent evt) {
        Block block = evt.getSpawnerLocation().getBlock();

        if(block.hasMetadata("event_id")) {
            evt.setShouldAbortSpawn(true);
            evt.setCancelled(true);
        }
    }
    @EventHandler
    public void entityKilled (EntityDeathEvent evt) {
        LivingEntity ent = evt.getEntity();

        if(ent.hasMetadata("interactableId")) {
            int entityId = ent.getMetadata("interactableId").get(0).asInt();
            int eventId = ent.getMetadata("eventId").get(0).asInt();

            AbstractEvent aEvt = EventsAPI.getAnyEvent(eventId);

            if(aEvt != null && (aEvt instanceof SpawnerEvent evt_)) {
                evt.setDroppedExp(0);
                evt.getDrops().clear();

                evt_.onEntityDied(entityId, ent);

            }
        }
    }
    @EventHandler
    public void entityExplode (EntityExplodeEvent evt) {
        if(evt.getEntity() instanceof LivingEntity ent) {
            if(ent.hasMetadata("interactableId")) {

                int entityId = ent.getMetadata("interactableId").get(0).asInt();
                int eventId = ent.getMetadata("eventId").get(0).asInt();

                AbstractEvent aEvt = EventsAPI.getAnyEvent(eventId);

                if((aEvt instanceof SpawnerEvent evt_)) {

                    if(evt_.getPhaseState().getStateId() > 1) {
                        ArrayList<Location> besties = Traverser.destinateCuboid(
                                ent.getLocation().clone().add(-2,2,-2),
                                ent.getLocation().clone().add(2,0,2),
                                0.5,
                                false
                        );
                        ParticleBuilder pb = new ParticleBuilder(Particle.EXPLOSION_HUGE)
                                .count(1)
                                .location(ent.getLocation())
                                .allPlayers();

                        pb.spawn();

                        for(Player player : ent.getLocation().getNearbyPlayers(15,15,15)) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 1));
                            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, .6F, 1);
                        }

                        for(Location dest : besties) {
                            ParticleBuilder pb_ = new ParticleBuilder(Particle.FLAME)
                                    .location(dest)
                                    .extra(0.03)
                                    .offset(MathUtils.randomMinMax(-3,3),MathUtils.randomMinMax(-3,3),MathUtils.randomMinMax(-3,3))
                                    .count(1)
                                    .allPlayers();
                            pb_.spawn();
                        }
                    }

                    evt_.getSpawnedEntities().remove(entityId);
                }
            }
        }
    }
    @EventHandler
    public void entityKilled (EntityDamageEvent evt) {
        Entity ent = evt.getEntity();

        if(ent instanceof LivingEntity && ent.hasMetadata("interactableId")) {
            int entityId = ent.getMetadata("interactableId").get(0).asInt();
            int eventId = ent.getMetadata("eventId").get(0).asInt();

            AbstractEvent aEvt = EventsAPI.getAnyEvent(eventId);

            if(aEvt != null && (aEvt instanceof SpawnerEvent evt_)) {
                SpawnedEntity entx = evt_.getSpawnedEntities().get(entityId);

                if (entx != null) {
                    entx.onDamage(evt.getFinalDamage());
                }
            }
        }
    }
    @EventHandler
    public void blockBreak (BlockBreakEvent evt) {
        if(evt.getBlock().getType().equals(Material.SPAWNER)) {
            Block broken = evt.getBlock();

            if(broken.hasMetadata("event_id")) {
                int evtId = broken.getMetadata("event_id").get(0).asInt();

                var event = EventsAPI.getAnyEvent(evtId);

                if(event != null && (event instanceof SpawnerEvent evt_)) {
                    evt_.onBreak(evt);
                }
            }
        }
    }
    @EventHandler
    public void dasd (PlayerInteractEvent evt) {
        if(evt.getHand() != EquipmentSlot.HAND)
            return;
        var player = evt.getPlayer();
        var block = evt.getClickedBlock();

        if(block != null && block.hasMetadata("event_id")) {
            if(evt.getAction() == Action.LEFT_CLICK_BLOCK)
                if(player.isOp()) return;

            int evtId = block.getMetadata("event_id").get(0).asInt();

            var event = EventsAPI.getAnyEvent(evtId);

            if(event != null) {
                EventType eventType = event.getEventType();

                switch (eventType) {
                    case ANCHOR -> {
                        var anchorEvt = (AnchorEvent) event;

                        if(!anchorEvt.isTickingStarted()) {
                            anchorEvt.triggerAvoidTicking();
                        }else {
                            anchorEvt.triggerOpenAction(evt);
                        }

                        evt.setCancelled(true);
                    }
                    case SPAWNER -> {
                        var spawnerEvent = (SpawnerEvent) event;

                        spawnerEvent.triggerClickAction(evt);
                    }
                }

            }else {
                player.sendMessage(StringUtils.asColor("&cСтранно, эвент не найден.."));
            }
        }
    }
}
