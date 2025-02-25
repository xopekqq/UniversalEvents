package ru.xopek.universalevents.core;

import org.bukkit.Location;

public interface AbstractEvent {
    Location getEventLocation();
    void update(long now);
    void asyncUpdate();
    boolean isActive();
    void callDestroy ();
    EventType getEventType();

    int getEventId();
    String getDisplayName();

}
