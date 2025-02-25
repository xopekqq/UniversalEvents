package ru.xopek.universalevents.core.evts.spawner;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class PhaseState {
    public final int stateId;
    public final long timeInMillis;
    public final String displayName, hologramName;
}
