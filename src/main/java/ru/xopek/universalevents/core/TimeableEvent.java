package ru.xopek.universalevents.core;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter @Setter
public class TimeableEvent {
    private long lastEventSpawn, eventDelayBetweenSpawns;
    private String displayName;
    private String announceMessage;
    private EventType eventType;
    private HashMap<String, Object> data;
    public TimeableEvent(String displayName, String announceMessage,long eventDelayBetweenSpawns, EventType eventType) {
        this.displayName = displayName;
        this.eventDelayBetweenSpawns = eventDelayBetweenSpawns;
        this.announceMessage = announceMessage;
        this.eventType = eventType;

        this.lastEventSpawn = System.currentTimeMillis();

        this.data = new HashMap<>();
    }
}
