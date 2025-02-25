package ru.xopek.universalevents.core.evts.anchor;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import ru.xopek.universalevents.util.MathUtils;

import java.util.ArrayList;

public class LootManager {
    public static void fillInventoryWithLoot(Inventory inventory, AnchorType anchorType) {

        switch(anchorType) {
            case DEFAULT: {
                addWhileChances(anchorType, inventory,
                        MathUtils.randomMinMax(2,6), //equip
                        0, //amulet
                        MathUtils.randomMinMax(1,2), //sfera
                        MathUtils.randomMinMax(2,10), //rare
                        MathUtils.randomMinMax(2,8), // def equip
                        MathUtils.randomMinMax(19,22), //ok
                        MathUtils.randomMinMax(16,28)); // shit
                break;
            }
            case RARE: {
                addWhileChances(anchorType, inventory,
                        MathUtils.randomMinMax(2,9), //equip
                        MathUtils.randomMinMax(1,1), //amulet
                        MathUtils.randomMinMax(1,2), //sfera
                        MathUtils.randomMinMax(4,13), //rare

                        MathUtils.randomMinMax(2,7), // def equip
                        MathUtils.randomMinMax(15,23), //ok
                        MathUtils.randomMinMax(17,26)); // shit
                break;
            }
            case EPIC: {
                addWhileChances(anchorType, inventory,
                        MathUtils.randomMinMax(3,9), //equip
                        MathUtils.randomMinMax(0,2), //amulet
                        MathUtils.randomMinMax(1,4), //sfera
                        MathUtils.randomMinMax(4,14), //rare
                        MathUtils.randomMinMax(5,11), // def equip
                        MathUtils.randomMinMax(15,21), //ok
                        MathUtils.randomMinMax(17,28)); // shit
                break;
            }
            case MYTHIC: {
                addWhileChances(anchorType, inventory,
                        MathUtils.randomMinMax(5,12), //equip
                        MathUtils.randomMinMax(0,4), //amulet
                        MathUtils.randomMinMax(1,5), //sfera
                        MathUtils.randomMinMax(7,15), //rare
                        MathUtils.randomMinMax(6,9), // def equip
                        MathUtils.randomMinMax(17,24), //ok
                        MathUtils.randomMinMax(17,26)); // shit
            }
        }
    }
    public static ItemStack buildBook(Enchantment ench , int level) {
        ItemStack build_book = new ItemStack(Material.ENCHANTED_BOOK , 1);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) build_book.getItemMeta();
        meta.addStoredEnchant(ench, level,true);

        build_book.setItemMeta(meta);

        //10/09/2023 - TODO: FIX CUSTOM ENCHANT IMPL

        return build_book;
    }
    public static void addWhileChances(AnchorType type, Inventory inv, int equipItems, int amuletItems , int sferaItems , int rareItems , int defaultEquipItems , int normalItem , int musorItem) {
        int randomSlot = getRandomEmptySlot(inv);

        int equipCounter = 0, amuletCounter = 0, sferaCounter = 0, rareCounter = 0, defaultEQCounter = 0, normalCounter = 0, musorCounter = 0;

        if(equipItems > 0) {
            while (equipCounter < equipItems && randomSlot != -1) {
                randomSlot = getRandomEmptySlot(inv);

                ArrayList<ItemStack> itemList = addEquipItem(type);
                equipCounter++;
                if(inv.firstEmpty() != -1 && itemList.size() > 0) inv.setItem(randomSlot, itemList.get((int) Math.floor(Math.random() * itemList.size())));
            }
        }
        if(amuletItems > 0) {
            while (amuletCounter < amuletItems && randomSlot != -1) {
                randomSlot = getRandomEmptySlot(inv);

                amuletCounter++;
                ArrayList<ItemStack> itemList = addAmulet(type);
                if(inv.firstEmpty() != -1 && itemList.size() > 0) inv.setItem(randomSlot, itemList.get((int) Math.floor(Math.random() * itemList.size())));
            }
        }
        if(sferaItems > 0) {
            while (sferaCounter < sferaItems && randomSlot != -1) {
                randomSlot = getRandomEmptySlot(inv);

                sferaCounter++;
                ArrayList<ItemStack> itemList = addSfera(type);
                if(inv.firstEmpty() != -1 && itemList.size() > 0) inv.setItem(randomSlot, itemList.get((int) Math.floor(Math.random() * itemList.size())));
            }
        }
        if(rareItems > 0) {
            while (rareCounter < rareItems && randomSlot != -1) {
                randomSlot = getRandomEmptySlot(inv);

                rareCounter++;
                ArrayList<ItemStack> itemList = addRareItems(type);
                if(inv.firstEmpty() != -1 && itemList.size() > 0) inv.setItem(randomSlot, itemList.get((int) Math.floor(Math.random() * itemList.size())));
            }
        }
        if(defaultEquipItems > 0) {
            while (defaultEQCounter < defaultEquipItems && randomSlot != -1) {
                randomSlot = getRandomEmptySlot(inv);

                defaultEQCounter++;
                ArrayList<ItemStack> itemList = addNormalEquip(type);
                if(inv.firstEmpty() != -1 && itemList.size() > 0) inv.setItem(randomSlot, itemList.get((int) Math.floor(Math.random() * itemList.size())));
            }
        }
        if(normalItem > 0) {
            while (normalCounter < normalItem && randomSlot != -1) {
                randomSlot = getRandomEmptySlot(inv);

                normalCounter++;
                ArrayList<ItemStack> itemList = addNormalItem(type);
                if(inv.firstEmpty() != -1 && itemList.size() > 0) inv.setItem(randomSlot, itemList.get((int) Math.floor(Math.random() * itemList.size())));
            }
        }
        if(musorItem > 0) {
            while (musorCounter < musorItem && randomSlot != -1) {
                randomSlot = getRandomEmptySlot(inv);

                musorCounter++;
                ArrayList<ItemStack> itemList = addMusor();
                if(inv.firstEmpty() != -1 && itemList.size() > 0) inv.setItem(randomSlot, itemList.get((int) Math.floor(Math.random() * itemList.size())));
            }
        }

    }
    public static ArrayList<ItemStack> addNormalEquip (AnchorType anchorType) {
        ArrayList<ItemStack> items = new ArrayList<>();

        if(anchorType == AnchorType.DEFAULT) {
           items.add(new ItemStack(Material.CHAINMAIL_HELMET));
           items.add(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
           items.add(new ItemStack(Material.CHAINMAIL_LEGGINGS));
           items.add(new ItemStack(Material.CHAINMAIL_BOOTS));
        }
        if(anchorType == AnchorType.RARE) {
            items.add(new ItemStack(Material.GOLDEN_HELMET));
            items.add(new ItemStack(Material.GOLDEN_CHESTPLATE));
            items.add(new ItemStack(Material.GOLDEN_LEGGINGS));
            items.add(new ItemStack(Material.GOLDEN_BOOTS));
        }
        if(anchorType == AnchorType.EPIC) {
            items.add(new ItemStack(Material.DIAMOND_HELMET));
            items.add(new ItemStack(Material.DIAMOND_CHESTPLATE));
            items.add(new ItemStack(Material.DIAMOND_LEGGINGS));
            items.add(new ItemStack(Material.DIAMOND_BOOTS));
        }
        if(anchorType == AnchorType.MYTHIC) {
            items.add(new ItemStack(Material.NETHERITE_HELMET));
            items.add(new ItemStack(Material.NETHERITE_CHESTPLATE));
            items.add(new ItemStack(Material.NETHERITE_LEGGINGS));
            items.add(new ItemStack(Material.NETHERITE_BOOTS));
        }
        return items;
    }
    public static ArrayList<ItemStack> addEquipItem(AnchorType anchorType) {
        ArrayList<ItemStack> items = new ArrayList<>();

        if(anchorType == AnchorType.DEFAULT) {
            items.add(new ItemStack(Material.CHAINMAIL_HELMET));
            items.add(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
            items.add(new ItemStack(Material.CHAINMAIL_LEGGINGS));
            items.add(new ItemStack(Material.CHAINMAIL_BOOTS));
        }
        if(anchorType == AnchorType.RARE) {
            items.add(new ItemStack(Material.GOLDEN_HELMET));
            items.add(new ItemStack(Material.GOLDEN_CHESTPLATE));
            items.add(new ItemStack(Material.GOLDEN_LEGGINGS));
            items.add(new ItemStack(Material.GOLDEN_BOOTS));
        }
        if(anchorType == AnchorType.EPIC) {
            items.add(new ItemStack(Material.DIAMOND_HELMET));
            items.add(new ItemStack(Material.DIAMOND_CHESTPLATE));
            items.add(new ItemStack(Material.DIAMOND_LEGGINGS));
            items.add(new ItemStack(Material.DIAMOND_BOOTS));
        }
        if(anchorType == AnchorType.MYTHIC) {
            items.add(new ItemStack(Material.NETHERITE_HELMET));
            items.add(new ItemStack(Material.NETHERITE_CHESTPLATE));
            items.add(new ItemStack(Material.NETHERITE_LEGGINGS));
            items.add(new ItemStack(Material.NETHERITE_BOOTS));
        }
        return items;
        // inventory.setItem(slot , items.get((int) Math.floor(Math.random() * items.size())));
    }
    public static ArrayList<ItemStack> addAmulet (AnchorType anchorType) {
        ArrayList<ItemStack> items = new ArrayList<>();
        return items;
    }
    public static ArrayList<ItemStack> addSfera (AnchorType anchorType) {
        ArrayList<ItemStack> items = new ArrayList<>();
        return items;
    }
    public static ArrayList<ItemStack> addRareItems (AnchorType anchorType) {
        ArrayList<ItemStack> items = new ArrayList<>();

        items.add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE,  MathUtils.randomMinMax(4,8)));
        items.add(new ItemStack(Material.GOLDEN_APPLE,  MathUtils.randomMinMax(8,20)));
        items.add(new ItemStack(Material.NETHERITE_INGOT, MathUtils.randomMinMax(1,3)));
        items.add(new ItemStack(Material.NETHERITE_SCRAP, MathUtils.randomMinMax(3,20)));

        items.add(new ItemStack(Material.FIREWORK_ROCKET, MathUtils.randomMinMax(16,64)));
        items.add(new ItemStack(Material.MAGMA_CREAM, MathUtils.randomMinMax(16,64)));
        items.add(new ItemStack(Material.BEACON, 1));
        items.add(new ItemStack(Material.GLOWSTONE_DUST, MathUtils.randomMinMax(16,64)));

        items.add(buildBook(Enchantment.DAMAGE_ALL, 5));
        items.add(buildBook(Enchantment.DURABILITY, 4));
        items.add(buildBook(Enchantment.DIG_SPEED, 5));
        items.add(buildBook(Enchantment.SOUL_SPEED, 3));
        items.add(buildBook(Enchantment.PROTECTION_ENVIRONMENTAL, 4));
        items.add(buildBook(Enchantment.MENDING, 1));
        items.add(buildBook(Enchantment.WATER_WORKER, 3));
        items.add(buildBook(Enchantment.LOOT_BONUS_MOBS, 5));
        items.add(buildBook(Enchantment.LOOT_BONUS_BLOCKS, 5));

        items.add(new ItemStack(Material.DIAMOND_BLOCK, MathUtils.randomMinMax(1,6)));
        items.add(new ItemStack(Material.EMERALD_BLOCK, MathUtils.randomMinMax(1,2)));

        return items;
    }
    public static ArrayList<ItemStack> addNormalItem (AnchorType anchorType) {
        ArrayList<ItemStack> items = new ArrayList<>();

        items.add(new ItemStack(Material.GOLD_INGOT, 1 + (int) (Math.floor(Math.random() * 64))));
        items.add(new ItemStack(Material.IRON_INGOT, 1 + (int) (Math.floor(Math.random() * 64))));
        items.add(new ItemStack(Material.COAL, 1 + (int) (Math.floor(Math.random() * 64))));
        items.add(new ItemStack(Material.OBSIDIAN, 1 + (int) (Math.floor(Math.random() * 64))));
        items.add(new ItemStack(Material.GUNPOWDER, MathUtils.randomMinMax(30,64)));
        items.add(new ItemStack(Material.SPIDER_SPAWN_EGG, MathUtils.randomMinMax(1,3)));
        items.add(new ItemStack(Material.BLAZE_SPAWN_EGG, MathUtils.randomMinMax(1,1)));
        items.add(new ItemStack(Material.ZOMBIE_SPAWN_EGG, MathUtils.randomMinMax(1,2)));
        items.add(new ItemStack(Material.CHICKEN_SPAWN_EGG, MathUtils.randomMinMax(1,2)));
        items.add(new ItemStack(Material.PIG_SPAWN_EGG, MathUtils.randomMinMax(1,4)));
        items.add(new ItemStack(Material.PIGLIN_SPAWN_EGG, MathUtils.randomMinMax(1,1)));
        items.add(new ItemStack(Material.BAT_SPAWN_EGG, MathUtils.randomMinMax(1,4)));
        items.add(new ItemStack(Material.COW_SPAWN_EGG, MathUtils.randomMinMax(1,4)));
        items.add(new ItemStack(Material.BLAZE_ROD, 1 + (int) (Math.floor(Math.random() * 64))));
        items.add(new ItemStack(Material.FIREWORK_STAR, 1 + (int) (Math.floor(Math.random() * 64))));
        items.add(new ItemStack(Material.GLISTERING_MELON_SLICE, MathUtils.randomMinMax(16,64)));
        items.add(new ItemStack(Material.MAGMA_CREAM, MathUtils.randomMinMax(16,64)));
        return items;
    }
    public static ArrayList<ItemStack> addMusor () {
        ArrayList<ItemStack> items = new ArrayList<>();

        items.add(new ItemStack(Material.GOLD_INGOT , (int) (32 + Math.floor(Math.random() * 32))));
        items.add(new ItemStack(Material.GOLD_INGOT , 23));
        items.add(new ItemStack(Material.BOOKSHELF, (int) (15 + Math.floor(Math.random() * 32))));
        items.add(new ItemStack(Material.ENCHANTING_TABLE, 1));
        items.add(new ItemStack(Material.ANVIL, 1));
        items.add(new ItemStack(Material.SLIME_BALL,  (int) (2 + Math.floor(Math.random() * 32))));
        items.add(new ItemStack(Material.TOTEM_OF_UNDYING, 1));
        items.add(new ItemStack(Material.ENDER_PEARL, MathUtils.randomMinMax(4,16)));
        items.add(new ItemStack(Material.CHORUS_FRUIT, (int) (25 + Math.floor(Math.random() * 32))));
        items.add(new ItemStack(Material.EXPERIENCE_BOTTLE, (int) (7 + Math.floor(Math.random() * 58))));
        items.add(new ItemStack(Material.DIAMOND, (int) (7 + Math.floor(Math.random() * 32))));
        items.add(new ItemStack(Material.STRING, (int) (10 + Math.floor(Math.random() * 54))));
        items.add(new ItemStack(Material.REDSTONE, (int) (25 + Math.floor(Math.random() * 32))));
        items.add(new ItemStack(Material.BOOK, (int) (8 + Math.floor(Math.random() * 14))));
        items.add(new ItemStack(Material.DRAGON_HEAD, 1));
        items.add(new ItemStack(Material.CREEPER_HEAD, (int) (1 + Math.floor(Math.random() * 2))));
        items.add(new ItemStack(Material.WITHER_SKELETON_SKULL, (int) (1 + Math.floor(Math.random() * 2))));
        items.add(new ItemStack(Material.TURTLE_EGG, (int) (1 + Math.floor(Math.random() * 6))));
        items.add(new ItemStack(Material.ZOMBIE_HEAD, (int) (1 + Math.floor(Math.random() * 3))));
        items.add(new ItemStack(Material.TNT, (int) (1 + Math.floor(Math.random() * 25))));
        items.add(new ItemStack(Material.GHAST_TEAR, (int) (8 + Math.floor(Math.random() * 32))));
        items.add(new ItemStack(Material.IRON_HORSE_ARMOR, 1));
        items.add(new ItemStack(Material.DIAMOND_HORSE_ARMOR, 1));
        items.add(new ItemStack(Material.END_CRYSTAL,  (int) (1 + Math.floor(Math.random() * 4))));
        items.add(new ItemStack(Material.MUSIC_DISC_13, 1));
        items.add(new ItemStack(Material.CRYING_OBSIDIAN, MathUtils.randomMinMax(16,64)));
        items.add(new ItemStack(Material.QUARTZ, MathUtils.randomMinMax(16,64)));
        items.add(new ItemStack(Material.FEATHER, MathUtils.randomMinMax(16,64)));
        items.add(new ItemStack(Material.FERMENTED_SPIDER_EYE, MathUtils.randomMinMax(16,64)));
        items.add(new ItemStack(Material.GOLDEN_CARROT, MathUtils.randomMinMax(16,64)));

        items.add(new ItemStack(Material.NETHER_STAR,1));
        items.add(new ItemStack(Material.HEART_OF_THE_SEA,1));
        items.add(new ItemStack(Material.NAME_TAG,1));
        items.add(new ItemStack(Material.ENDER_CHEST,1));
        items.add(new ItemStack(Material.LEAD,1));
        items.add(new ItemStack(Material.SCUTE,1));

        return items;
    }
    public static int getRandomEmptySlot(Inventory inventory) {
        ArrayList<Integer> emptySlots = new ArrayList<>();

        for(int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);

            if(itemStack == null || itemStack.getType() == Material.AIR)  emptySlots.add(i);
        }

        if(emptySlots.size() == 0) return -1;

        return emptySlots.get(MathUtils.randomMinMax(0,emptySlots.size()));
    }

}
