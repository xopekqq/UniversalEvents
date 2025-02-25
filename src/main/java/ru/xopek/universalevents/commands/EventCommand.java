package ru.xopek.universalevents.commands;

import ru.xopek.universalevents.UniversalEvents;
import ru.xopek.universalevents.api.EventsAPI;
import ru.xopek.universalevents.core.AbstractEvent;
import ru.xopek.universalevents.core.EventType;
import ru.xopek.universalevents.core.TimeableEvent;
import ru.xopek.universalevents.core.evts.anchor.AnchorEvent;
import ru.xopek.universalevents.core.evts.anchor.AnchorType;
import ru.xopek.universalevents.core.evts.spawner.SpawnerEvent;
import ru.xopek.universalevents.util.ButtonsUtils;
import ru.xopek.universalevents.util.Constants;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.xopek.universalevents.util.StringUtils;

public class EventCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        if(args.length == 0) {
            player.sendMessage(StringUtils.asColor(Constants.eventsPrefix + "Введите подкоманду &7(gps/delay)"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return true;
        }

        switch(args[0].toLowerCase()) {
            case "spawn" -> {
                if(!player.isOp())
                    return false;

                String eventType = args.length > 1 ? args[1] : "ANCHOR";

                AbstractEvent evt = null;
                TimeableEvent timeable = null;

                switch (eventType.toLowerCase()) {
                    case "anchor" -> {
                        evt = new AnchorEvent(
                                player.getLocation().getBlock().getLocation().clone(),
                                args.length > 2 ? Integer.parseInt(args[2]) : 360,
                                true,
                                UniversalEvents.getWorker().nextId(),
                                UniversalEvents.getWorker().nextSubtype(),
                                AnchorType.randomType()
                        );

                        timeable = UniversalEvents
                                .getWorker()
                                .getTimeableEventsMap()
                                .get(eventType);
                    }
                    case "2", "spawner" -> {
                        evt = new SpawnerEvent(
                                UniversalEvents.getWorker().nextId(),
                                player.getLocation().getBlock().getLocation().clone()
                        );

                        timeable = UniversalEvents
                                .getWorker()
                                .getTimeableEventsMap()
                                .get("SPAWNER");

                    }
                }
                if(evt == null) {
                    sender.sendMessage(StringUtils.asColor(Constants.eventsPrefix + "&cОшибка! &fИвент для спавна не найден.."));
                    player.sendTitle(StringUtils.asColor("&cОшибка!"), StringUtils.asColor("&fИвент не найден"), 9 , 38, 10);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.6f,1);
                    return false;
                }
                boolean isSilent = args.length > 3 && args[3].equalsIgnoreCase("silent");
                UniversalEvents.getWorker().spawnEventNaturally(evt, timeable, isSilent);
            }
            case "navbar" -> doNavbar(args, player);
            case "compass" -> doCompass(args, player, false);
            case "coordinate" -> {
                doNavbar(args, player);
                doCompass(args, player, true);
            }
            case "nstop" -> {
                if(!UniversalEvents.getInst().getNavbarMap().containsKey(player.getUniqueId())) {
                    player.sendMessage(StringUtils.asColor("&cОшибка! &fУ вас уже выключен &cНавигационный Бар"));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    return false;
                }
                UniversalEvents.getInst().getNavbarMap().remove(player.getUniqueId());
                player.sendMessage(StringUtils.asColor("&aУспешно! &fВы выключили &#FF3508Навигационный Бар."));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
                break;
            }
            case "status", "gps", "delay" -> {
                player.sendMessage(StringUtils.asColor(Constants.eventsPrefix + "Данные по Ивентам:"));

                AbstractEvent anchorEvent = EventsAPI.getFirstEventWithType(EventType.ANCHOR);
                AbstractEvent spawnerEvent = EventsAPI.getFirstEventWithType(EventType.SPAWNER);


                if (anchorEvent != null && (anchorEvent instanceof AnchorEvent event)) {
                    player.sendMessage(StringUtils.asColor(" &#FBD81C[1]: "));
                    player.sendMessage(StringUtils.asColor("  &7| &fТип: &#FB1E0F" + anchorEvent.getDisplayName()));

                    ButtonsUtils acceptBtn = new ButtonsUtils("  &7| &fКоординаты: &6[" + anchorEvent.getEventLocation().getBlockX() + " ;" + anchorEvent.getEventLocation().getBlockY() + " ;" + anchorEvent.getEventLocation().getBlockZ() + "]");

                    acceptBtn.setHover("&7Нажмите, чтобы указать компасс на координаты ивента, и добавить координаты в навигационный бар");
                    int x = event.getEventLocation().getBlockX();
                    int y = event.getEventLocation().getBlockY();
                    int z = event.getEventLocation().getBlockZ();

                    acceptBtn.setClickEvent("/event coordinate " + x + " " + y + " " + z);
                    if(event.isActive()) {
                        long differenceInMillis = System.currentTimeMillis() - event.getTicksStartTimestamp();
                        long timeTillOpen = (event.getAwaitTicksTillOpen() - differenceInMillis);

                        int timeInSeconds = (int) (timeTillOpen / 1000L);

                        if(timeTillOpen <= 0) {
                            player.sendMessage(StringUtils.asColor("  &7| &fСтатус: &aОткрыт"));
                        }else {
                            player.sendMessage(StringUtils.asColor("  &7| &fСтатус: &cАктивируется &7₍ " + timeInSeconds + " сек &7₎"));

                        }
                    }else {
                        player.sendMessage(StringUtils.asColor("  &7| &fСтатус: &#FB1E0FНе Активирован!"));
                    }

                    player.spigot().sendMessage(acceptBtn.getButton());
                }else {
                    TimeableEvent evt = UniversalEvents.getWorker().getTimeableEventsMap().get("ANCHOR");
                    player.sendMessage(StringUtils.asColor(" &#FBD81C[1]: "));
                    player.sendMessage(StringUtils.asColor("  &7| &fТип: &#FB1E0FЯкорь"));

                    int timeTillSpawn = (int) (evt.getEventDelayBetweenSpawns() - ((System.currentTimeMillis() - evt.getLastEventSpawn()) / 1000L));

                    player.sendMessage(StringUtils.asColor("  &7| &fДо Спавна: &#2BFAFB") + StringUtils.getTime(timeTillSpawn * 1000));
                }


                /**
                 * SPAWNER
                 */
                if (spawnerEvent != null && (spawnerEvent instanceof SpawnerEvent event)) {
                    player.sendMessage(StringUtils.asColor(" &#FBD81C[2]: "));
                    player.sendMessage(StringUtils.asColor("  &7| &fТип: &#FB611E" + spawnerEvent.getDisplayName()));

                    ButtonsUtils acceptBtn = new ButtonsUtils("  &7| &fКоординаты: &6[" + spawnerEvent.getEventLocation().getBlockX() + " ;" + spawnerEvent.getEventLocation().getBlockY() + " ;" + spawnerEvent.getEventLocation().getBlockZ() + "]");
                    acceptBtn.setHover("&7Нажмите, чтобы указать компасс на координаты ивента, и добавить координаты в навигационный бар");

                    int x = event.getEventLocation().getBlockX();
                    int y = event.getEventLocation().getBlockY();
                    int z = event.getEventLocation().getBlockZ();

                    acceptBtn.setClickEvent("/event coordinate " + x + " " + y + " " + z);
                    player.sendMessage(StringUtils.asColor("  &7| &fСтатус: &a" + event.getPhaseState().displayName));

                    player.spigot().sendMessage(acceptBtn.getButton());
                }else {
                    TimeableEvent evt = UniversalEvents.getWorker().getTimeableEventsMap().get("SPAWNER");
                    player.sendMessage(StringUtils.asColor(" &#FBD81C[2]: "));
                    player.sendMessage(StringUtils.asColor("  &7| &fТип: &#FB611EСпавнер"));

                    int timeTillSpawn = (int) (evt.getEventDelayBetweenSpawns() - ((System.currentTimeMillis() - evt.getLastEventSpawn()) / 1000L));

                    player.sendMessage(StringUtils.asColor("  &7| &fДо Спавна: &#2BFAFB") + StringUtils.getTime(timeTillSpawn * 1000));// + " сек");
                }
            }
            case "clear" -> {
                if(!player.isOp() && !player.hasPermission("UniversalEvents.*")) {
                    return false;
                }
                if(!EventsAPI.isAnyEventPlaying()) {
                    player.sendMessage(StringUtils.asColor(Constants.eventsPrefix + "&fНа сервере нет работающих ивентов!"));
                    return false;
                }
                for(AbstractEvent evt : UniversalEvents.getWorker().getEventList()) {
                    evt.callDestroy();
                }
                player.sendMessage(StringUtils.asColor(Constants.eventsPrefix + "&fУспешно очищено &c" + UniversalEvents.getWorker().getEventList().size() + "&f Ивентов!"));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

                UniversalEvents.getWorker().getEventList().clear();
                UniversalEvents.getInst().getNavbarMap().clear();
            }
        }

        return false;
    }

    public static void doNavbar (String[] args, Player player) {
        int x = -1, y = -1 , z = -1;

        try {
            x = Integer.parseInt(args[1]);
            y = Integer.parseInt(args[2]);
            z = Integer.parseInt(args[3]);
        }catch (Exception e) {
            player.sendMessage(StringUtils.asColor("&cОшибка! &fЗначения должны быть числами"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return;
        }

        UniversalEvents.getInst().getNavbarMap().put(player.getUniqueId(), "[" + x + "; " + y + "; " + z + "]");
        player.sendMessage(StringUtils.asColor("&aВнимание! &fВы включили &#FF3508Навигационный Бар, &fчтобы\nвыключить его напишите &#FF3508/event nstop"));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
    }
    public static void doCompass(String[] args, Player player, boolean byTag) {
        int x = -1, y = -1 , z = -1;

        try {
            x = Integer.parseInt(args[1]);
            y = Integer.parseInt(args[2]);
            z = Integer.parseInt(args[3]);
        }catch (Exception e) {
            if(byTag) return;
            player.sendMessage(StringUtils.asColor("&cОшибка! &fЗначения должны быть числами"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return;
        }

        player.setCompassTarget(new Location(player.getLocation().getWorld(), x, y, z));
    }
}
