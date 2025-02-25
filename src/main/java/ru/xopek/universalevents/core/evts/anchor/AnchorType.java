package ru.xopek.universalevents.core.evts.anchor;

import ru.xopek.universalevents.util.MathUtils;

public enum AnchorType {
    DEFAULT,
    RARE,
    EPIC,
    MYTHIC;
    public static AnchorType randomType() {
        int ran2d = MathUtils.randomMinMax(0, 100);

        if(ran2d >= 86)
            return AnchorType.EPIC;
        if(ran2d >= 70)
            return AnchorType.MYTHIC;
        if(ran2d >= 50)
            return AnchorType.RARE;

        return AnchorType.DEFAULT;
    }
}
