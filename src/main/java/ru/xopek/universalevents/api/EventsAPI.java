package ru.xopek.universalevents.api;

import ru.xopek.universalevents.UniversalEvents;
import ru.xopek.universalevents.core.AbstractEvent;
import ru.xopek.universalevents.core.EventType;

import java.util.concurrent.ConcurrentLinkedQueue;

public class EventsAPI {
    public static boolean isAnyEventPlaying () {
        ConcurrentLinkedQueue<AbstractEvent> eventsList = UniversalEvents.getWorker().getEventList();

        return !eventsList.isEmpty();
    }
    public static AbstractEvent getAnyEvent (int eventId) {
        return UniversalEvents.getWorker().getEventList()
                .stream()
                .filter(f -> f.getEventId() == eventId)
                .findFirst()
                .orElse(null);
    }
    public static AbstractEvent getFirstEventWithType (EventType eventType) {
        return UniversalEvents.getWorker().getEventList()
                .stream()
                .filter(f -> f.getEventType() == eventType)
                .findFirst()
                .orElse(null);
    }
    public static AbstractEvent getFirstEvent () {
        return UniversalEvents.getWorker().getEventList()
                .stream().
                findFirst().
                orElse(null);
    }
}
