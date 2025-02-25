package ru.xopek.universalevents.hologram;

import lombok.Getter;

import java.util.ArrayList;

public class UHologramsCache {
    @Getter private final ArrayList<String> cachedNames;
    @Getter private final ArrayList<UHologram> hologramsCache;
    private final String symbols;
    private final int nameMaxLength;
    public UHologramsCache() {
        /**
         * Init
         */
        this.cachedNames = new ArrayList<>();
        this.hologramsCache = new ArrayList<>();
        this.symbols = "abcdefghijklmnopABCDEFGHIJKLMNOPxXzZwWyYcCrRsStTVv";
        this.nameMaxLength = Byte.MAX_VALUE >> 3;

    }
    public String getName () {
        String q_ = this.generateCescarName(this.nameMaxLength);

        while(this.cachedNames.contains(q_))
            q_ = this.generateCescarName(this.nameMaxLength);

        this.cachedNames.add(q_);
        return q_;
    }
    public void onHologramDeleted(UHologram hologram) {
        this.cachedNames.remove(hologram.getName());
        this.hologramsCache.remove(hologram);
    }
    public String generateCescarName (int len) {
        StringBuilder name = new StringBuilder();

        for(int i = 0; i < len; i++)
            name.append(symbols.charAt((int) Math.floor(Math.random() * symbols.length())));

        return name.toString();
    }
}