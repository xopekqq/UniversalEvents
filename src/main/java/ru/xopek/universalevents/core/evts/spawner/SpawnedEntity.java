package ru.xopek.universalevents.core.evts.spawner;

import com.destroystokyo.paper.ParticleBuilder;
import ru.xopek.universalevents.UniversalEvents;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import ru.xopek.universalevents.util.StringUtils;
import ru.xopek.universalevents.util.Traverser;

import java.util.ArrayList;

@Getter @Setter
public class SpawnedEntity {
    private LivingEntity livingEntity;
    private Location spawnLocation;
    private PhaseState phaseState;
    private EntityType type;
    private SpawnerEvent eventInst;
    private int id;
    public SpawnedEntity (SpawnerEvent eventInst, Location toSpawn , EntityType withType, PhaseState withPhase, int spawnId) {
        this.spawnLocation = toSpawn;
        this.type = withType;
        this.phaseState = withPhase;

        this.livingEntity = (LivingEntity) toSpawn.getWorld().spawnEntity(toSpawn, withType);

        this.eventInst = eventInst;
        this.id = spawnId;
        this.livingEntity.setCustomNameVisible(true);

        double maxHealth = 140 * phaseState.stateId;
        this.livingEntity.setMaxHealth(maxHealth);
        this.livingEntity.setHealth(maxHealth);
        this.livingEntity.setCustomName(this.getNameFromMob() + StringUtils.asColor(" &7| &c" + ((int) this.livingEntity.getHealth()) + "❤"));

        this.livingEntity.setMetadata("interactableId", new FixedMetadataValue(UniversalEvents.getInst(), spawnId));
        this.livingEntity.setMetadata("eventId", new FixedMetadataValue(UniversalEvents.getInst(), eventInst.getEventId()));
    }

    public void removeEntity () {
        this.livingEntity.remove();
    }

    public void onDamage (double damage) {
        double newHealth = Math.max(0, this.livingEntity.getHealth() - damage);
        int fixedHealth = (int) newHealth;
        this.livingEntity.setCustomName(this.getNameFromMob() + StringUtils.asColor(" &7| &c" + fixedHealth + "❤"));

    }
    public void onDied () {
        int phaseId = this.phaseState.getStateId();

        this.eventInst.getSpawnedEntities().remove(this.id);

        for(int i = 0; i < Math.min(3, phaseId / 1.5); i++) {
            ItemStack itemToDrop = SpawnerDropper.getItemToDrop(this.phaseState);

            if(itemToDrop != null) {
                this.livingEntity.getWorld().dropItemNaturally(this.livingEntity.getLocation().clone().add(0, .2, 0), itemToDrop);
            }
        }

        switch (phaseId) {
            case 2,3,4,5 -> this.livingEntity.getWorld().createExplosion(
                    this.livingEntity,
                    this.phaseState.stateId,
                    false,
                    false
            );
        }

        Location from = this.livingEntity.getBoundingBox().getMin().toLocation(this.livingEntity.getWorld());
        Location to = this.livingEntity.getBoundingBox().getMax().toLocation(this.livingEntity.getWorld());

        ArrayList<Location> besties = Traverser.destinateCuboid(
                from.clone(),
                to.clone(),
                0.25,
                false
        );

        for (Location dest : besties) {
            ParticleBuilder pb = new ParticleBuilder(Particle.SOUL_FIRE_FLAME)
                    .location(dest)
                    .count(1)
                    .allPlayers();
            pb.spawn();
       }

    }

    public String getNameFromMob () {
        if(this.livingEntity == null)
            return "Неизвестно";

        switch (this.livingEntity.getType()) {
            case WITHER_SKELETON: return "Визер-Скелет";
            case SPIDER: return "Паук";
            case CAVE_SPIDER: return "Пещерный-Паук";
            case ZOMBIE: return "Зомби";
            case BLAZE: return "Всполох";
            case COW: return "Корова";
            case SKELETON: return "Скелет";
            case CHICKEN: return "Курица";
            case CREEPER: return "Крипер";
            case PIG: return "Поруся";
        }

        return "Неизвестно";
    }

}
