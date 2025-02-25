package ru.xopek.universalevents.config;

import ru.xopek.universalevents.UniversalEvents;
import ru.xopek.universalevents.core.EventType;
import ru.xopek.universalevents.core.TimeableEvent;
import ru.xopek.universalevents.core.evts.spawner.PhaseState;
import ru.xopek.universalevents.util.Constants;
import org.bukkit.configuration.ConfigurationSection;
import ru.xopek.universalevents.util.StringUtils;

import java.util.HashMap;

import static org.bukkit.Bukkit.getLogger;

public class Configurations {
    public HashMap<String, String> stringsCachedMap;
    public HashMap<String, PhaseState> registeredPhaseStates;
    public Configurations () {
        stringsCachedMap = new HashMap<>();
        registeredPhaseStates = new HashMap<>();

        ConfigurationSection config = UniversalEvents.getInst().getConfig();

        ConfigurationSection spawnerFields = config.getConfigurationSection("events.spawner");

        int spawnerDelayInSeconds = spawnerFields.getInt("eventDelay");
        String displayName = spawnerFields.getString("displayName");


        ConfigurationSection spawnerPhaseFields = spawnerFields.getConfigurationSection("phases");

        int publicId = 0;
        for(String field : spawnerPhaseFields.getKeys(false)) {
            ConfigurationSection phaseSection = spawnerPhaseFields.getConfigurationSection(field);

            String displayName_ = phaseSection.getString("displayName");
            long timeInMillis = phaseSection.getInt("timeout");

            PhaseState phaseState = new PhaseState(publicId, timeInMillis, displayName_, phaseSection.getString("hologramName"));

            registeredPhaseStates.put(field, phaseState);
            publicId++;
        }

        TimeableEvent timeableSpawner = new TimeableEvent(
                displayName,
                spawnerFields.getString("announceMessage"),
                spawnerDelayInSeconds,
                EventType.SPAWNER
        );
        timeableSpawner.getData().put("ticksWith", 0);
        timeableSpawner.getData().put("hasEventAnnounced", false);

        UniversalEvents.getWorker().getTimeableEventsMap()
                .put("SPAWNER", timeableSpawner);

        /**
         * Serialize anchor data ?? xd
         */
        ConfigurationSection anchorFields = config.getConfigurationSection("events.anchor");

        int anchorDelayInSeconds = anchorFields.getInt("eventDelay");
        displayName = anchorFields.getString("displayName");


        TimeableEvent timeableAnchor = new TimeableEvent(
                displayName,
                anchorFields.getString("announceMessage"),
                anchorDelayInSeconds,
                EventType.ANCHOR
        );
        timeableAnchor.getData().put("ticksWith", 0);
        timeableAnchor.getData().put("hasEventAnnounced", false);

        if(anchorFields.contains("announceSound")) {
            timeableAnchor.getData().put("announceSound", anchorFields.getString("announceSound"));
        }

        UniversalEvents.getWorker().getTimeableEventsMap()
                .put("ANCHOR", timeableAnchor);

        getLogger().info(StringUtils.asColor(Constants.eventsPrefix + "Успешно загрузил &c" + UniversalEvents.getWorker().getTimeableEventsMap().size() + " &fивентов!"));
    }
}
