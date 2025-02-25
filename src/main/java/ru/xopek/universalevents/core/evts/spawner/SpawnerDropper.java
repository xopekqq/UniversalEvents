package ru.xopek.universalevents.core.evts.spawner;

import ru.xopek.universalevents.core.evts.anchor.AnchorType;
import ru.xopek.universalevents.core.evts.anchor.LootManager;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class SpawnerDropper {
    public static ItemStack getItemToDrop (PhaseState phaseState) {
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        switch (phaseState.stateId) {
            case 1 -> {
                items.addAll(LootManager.addMusor());
                items.addAll(LootManager.addNormalItem(AnchorType.DEFAULT));
            }
            case 2 -> {
                items.addAll(LootManager.addMusor());
                items.addAll(LootManager.addNormalItem(AnchorType.DEFAULT));
                items.addAll(LootManager.addNormalEquip(AnchorType.RARE));
            }
            case 3 -> {
                items.addAll(LootManager.addMusor());
                items.addAll(LootManager.addNormalItem(AnchorType.EPIC));
                items.addAll(LootManager.addNormalEquip(AnchorType.EPIC));
                items.addAll(LootManager.addRareItems(AnchorType.EPIC));
                items.addAll(LootManager.addSfera(AnchorType.EPIC));
                items.addAll(LootManager.addAmulet(AnchorType.EPIC));
            }
            default -> {
                items.addAll(LootManager.addMusor());
                items.addAll(LootManager.addNormalItem(AnchorType.MYTHIC));
                items.addAll(LootManager.addNormalEquip(AnchorType.MYTHIC));
                items.addAll(LootManager.addRareItems(AnchorType.MYTHIC));
                items.addAll(LootManager.addSfera(AnchorType.MYTHIC));
                items.addAll(LootManager.addAmulet(AnchorType.MYTHIC));
            }
        }

        return items.get((int) (Math.floor(Math.random() * items.size())));
    }
}
