package ru.xopek.universalevents.hologram;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.utils.items.HologramItem;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import ru.xopek.universalevents.UniversalEvents;

import java.util.ArrayList;

public class UHologram {

    private ArrayList<HologramLine> textLines, itemLines;

    @Getter private Hologram dhHologram;
    @Getter private String name;

    public UHologram(Location location) {
        this.dhHologram = DHAPI.createHologram(UniversalEvents.getInst().getHologramsCache().getName(), location);

        this.name = this.dhHologram.getName();

        this.textLines = new ArrayList<>();
        this.itemLines = new ArrayList<>();
    }


    public void teleportHologram(Location newLocation) { DHAPI.moveHologram(dhHologram, newLocation); }
    public void clearHologram() {
        UniversalEvents.getInst().getHologramsCache().onHologramDeleted(this);
        dhHologram.delete();
    }
    public void addTextLine(String text) { this.textLines.add(DHAPI.addHologramLine(dhHologram, text)); }
    public void addItemLine(ItemStack itemStack) { this.itemLines.add(DHAPI.addHologramLine(dhHologram, itemStack)); }
    public void setText(int line_index, String text){
        if(this.textLines.size() < line_index)
            this.addTextLine(text);
        else {
            this.textLines.get(line_index).setText(text);
        }
    }
    public void setItemLine(int line_index, ItemStack itemStack) {
        if(this.itemLines.size() < line_index)
            this.addItemLine(itemStack);
        else {
            this.itemLines.get(line_index).setItem(HologramItem.fromItemStack(itemStack));
        }
    }
    public void clearItemLine(int line_index) {
        this.itemLines.get(line_index).delete();
        this.itemLines.remove(line_index);
    }
    public void clearTextLine(int line_index) {
        this.textLines.get(line_index).delete();
        this.textLines.remove(line_index);
    }
}
