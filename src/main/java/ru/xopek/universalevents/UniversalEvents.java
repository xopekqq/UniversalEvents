package ru.xopek.universalevents;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.xopek.universalevents.api.EventsAPI;
import ru.xopek.universalevents.commands.EventCommand;
import ru.xopek.universalevents.config.Configurations;
import ru.xopek.universalevents.core.AbstractEvent;
import ru.xopek.universalevents.hologram.UHologramsCache;
import ru.xopek.universalevents.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static org.bukkit.Bukkit.*;
import static org.bukkit.Bukkit.getScheduler;

public final class UniversalEvents extends JavaPlugin {

    @Getter private static UniversalEvents inst;
    @Getter private static boolean isDebug = true;
    @Getter private static EventWorker worker;
    @Getter private static Configurations configurations;

    @Getter private UHologramsCache hologramsCache;
    @Getter private ArrayList<Player> playerTargetCoordsList = new ArrayList<>();
    @Getter private HashMap<UUID, String> navbarMap = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        inst = this;
        worker = new EventWorker();
        configurations = new Configurations();
        hologramsCache = new UHologramsCache();

        getPluginCommand("universalevents").setExecutor(new EventCommand());

        getPluginManager().registerEvents(new EventProcessor(), this);

        getScheduler().scheduleAsyncRepeatingTask(this, () -> {
            for(AbstractEvent evt : getWorker().getEventList()) {
                evt.asyncUpdate();
            }
        }, 5, 5);

        getScheduler().scheduleSyncRepeatingTask(this, () -> {
            ArrayList<UUID> toRemove = new ArrayList<>();

            navbarMap.forEach((userId, stl) -> {
                Player player = Bukkit.getPlayer(userId);

                if(player == null) {
                    toRemove.add(userId);
                    return;
                }
                player.sendActionBar(StringUtils.asColor("&fИвент находится на координатах: &#FF3508" + stl));
            });

            if(!EventsAPI.isAnyEventPlaying()) {
                toRemove.addAll(navbarMap.keySet());
            }

            for(UUID uuid : toRemove) {
                navbarMap.remove(uuid);
            }
        }, 30, 30);
    }

    @Override
    public void onDisable() {
        for(AbstractEvent event : worker.getEventList())
            event.callDestroy();
    }
}
