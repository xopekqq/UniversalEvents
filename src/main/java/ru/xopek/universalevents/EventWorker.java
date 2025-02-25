package ru.xopek.universalevents;

import lombok.Getter;
import ru.xopek.universalevents.core.AbstractEvent;
import ru.xopek.universalevents.core.TimeableEvent;
import ru.xopek.universalevents.core.evts.anchor.AnchorEvent;
import ru.xopek.universalevents.core.evts.anchor.AnchorSubtype;
import ru.xopek.universalevents.core.evts.spawner.SpawnerEvent;
import ru.xopek.universalevents.util.Constants;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.xopek.universalevents.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getScheduler;

public class EventWorker {
    @Getter
    private static final long EventLoopMillis = 1000 * 3600;
    @Getter
    private ConcurrentLinkedQueue<AbstractEvent> eventList;
    private int lastId = 0;
    @Getter
    private HashMap<String, TimeableEvent> timeableEventsMap;
    public EventWorker () {
        this.timeableEventsMap = new HashMap<>();

        this.eventList = new ConcurrentLinkedQueue<>();

        this.initialize();
    }
    public void initialize () {
        getScheduler().scheduleSyncRepeatingTask(UniversalEvents.getInst(), () -> {
            long now = System.currentTimeMillis();

            for(Map.Entry<String, TimeableEvent> entry : timeableEventsMap.entrySet()) {
                TimeableEvent inst = entry.getValue();
                long lastSpawnedNaturally = inst.getLastEventSpawn();

                boolean hasAnnouncedEvent = Boolean.parseBoolean(String.valueOf(inst.getData().get("hasEventAnnounced")));

                long maxDelay = inst.getEventDelayBetweenSpawns() * 1000;
                long beforeEventTime = maxDelay - (now - lastSpawnedNaturally);

                if(!hasAnnouncedEvent && beforeEventTime <= 1000 * 60 * 4) {
                    inst.getData().put("hasEventAnnounced", true);
                    this.announceEventNaturally(inst);
                }

                if(beforeEventTime <= 0) {
                    inst.getData().put("hasEventAnnounced", false);
                    inst.setLastEventSpawn(now);

                    this.generateEvent(inst);
                }
            }

            for(AbstractEvent event : this.eventList) {
                event.update(now);
            }
        }, 20 , 20);
    }
    public AnchorSubtype nextSubtype() {
        TimeableEvent event = this.timeableEventsMap.getOrDefault("ANCHOR", null);

        if(event == null) {
            getLogger().warning(StringUtils.asColor(Constants.eventsPrefix + "Ошибка при выполнении метода &e EventWorker.#predicateType &cвозможный ивент не зарегистрирован!"));
            return AnchorSubtype.DEFAULT;
        }
        int ticksWithStableSpawn = Integer.parseInt(String.valueOf(event.getData().get("ticksWith")));

        if(ticksWithStableSpawn > 2) {
            event.getData().put("ticksWith", 0);

            if(UniversalEvents.isDebug())
                getLogger().info(StringUtils.asColor(Constants.eventsPrefix + "Debugger: &ePeace &fevent generation used"));

            return AnchorSubtype.MIRNI;
        }

        if(UniversalEvents.isDebug())
            getLogger().info(StringUtils.asColor(Constants.eventsPrefix + "Debugger: &aDefault &fevent generation used"));

        event.getData().put("ticksWith", ticksWithStableSpawn + 1);

        return AnchorSubtype.DEFAULT;
    }
    public int nextId () {
        return ++this.lastId;
    }
    public void generateEvent (TimeableEvent timeableEvent) {
        switch (timeableEvent.getEventType()) {
            case ANCHOR -> {
                AnchorEvent randomAnchor = AnchorEvent.serializeRandomized();

                this.spawnEventNaturally(randomAnchor, timeableEvent, false);
            }
            case SPAWNER -> {
                SpawnerEvent randomSpawner = SpawnerEvent.serializeRandomized();

                this.spawnEventNaturally(randomSpawner, timeableEvent, false);
            }
        }
    }
    public void spawnEventNaturally(AbstractEvent evt, TimeableEvent timeable, boolean isSilent) {
        this.pushEvent(evt, isSilent);
    }
    public void announceEventNaturally(TimeableEvent evt) {
        Sound soundToPlay = null;

        if(evt.getData().containsKey("announceSound")) {
            soundToPlay = Sound.valueOf(
                    String.valueOf(
                            evt.getData().get("announceSound")
                    )
            );
        }
        Bukkit.broadcastMessage(StringUtils.asColor(evt.getAnnounceMessage()));

        for(Player player : Bukkit.getOnlinePlayers()) {

            if(evt.getData().containsKey("announceSound")) {
                player.playSound(player.getLocation(), soundToPlay, .8f, 1);
            }

        }
    }
    public void pushEvent (AbstractEvent evt, boolean isSilent) {
        if(!isSilent)
            createEventAnnounce(evt);

        this.eventList.add(evt);
    }
    public void clearEvent(AbstractEvent evt) {
        this.eventList.remove(evt);
    }
    public void createEventAnnounce (AbstractEvent evt) {
        int x = evt.getEventLocation().getBlockX();
        int y = evt.getEventLocation().getBlockY();
        int z = evt.getEventLocation().getBlockZ();

        String hovertext = "&7Нажмите, чтобы указать в хотбаре";

        HoverEvent hover = new HoverEvent(Action.SHOW_TEXT, new Text(StringUtils.asColor(hovertext)));
        ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event navbar " + x + " " + y + " " + z);

        BaseComponent[] comp = null;
        switch (evt.getEventType()) {
            case ANCHOR -> {
                AnchorEvent anchorEvent = (AnchorEvent) evt;
                if(anchorEvent.getAnchorSubtype().equals(AnchorSubtype.MIRNI)) {
                    comp = TextComponent.fromLegacyText(StringUtils.asColor("            &#ADF3FD[" + x + ";" + y + "; " + z + "]"));
                }else {
                    comp = TextComponent.fromLegacyText(StringUtils.asColor("            &#E90000[" + x + ";" + y + "; " + z + "]"));
                }
            }
            case SPAWNER -> {
                comp = TextComponent.fromLegacyText(StringUtils.asColor("             &#F6FB1A[" + x + ";" + y + "; " + z + "]"));

            }
        }

        for (BaseComponent component : comp) {
            component.setHoverEvent(hover);
            component.setClickEvent(click);
        }
        for(Player player : Bukkit.getOnlinePlayers()) {
            switch (evt.getEventType()) {
                case ANCHOR -> {
                    sendAnchorMessage((AnchorEvent) evt, player, comp);
                }
                case SPAWNER -> {
                    sendSpawnerMessage((SpawnerEvent) evt, player, comp);
                }
            }

        }
    }
    public void sendSpawnerMessage(SpawnerEvent evt, Player to, BaseComponent[] pos) {
        to.sendMessage(StringUtils.asColor("&#fb9419&m &#fb9919&m &#fb9e19&m &#faa319&m &#faa819&m &#faad19&m &#fab119&m &#f9b619&m &#f9bb19&m &#f9c019&m &#f9c519&m &#f8ca1a&m &#f8cf1a&m &#f8d41a&m &#f8d91a&m &#f7de1a&m &#f7e21a&m &#f7e71a&m &#f7ec1a&m &#f6f11a&m &#f6f61a&m &#f6fb1a&m &#f6f61a&m &#f6f11a&m &#f7ec1a&m &#f7e71a&m &#f7e21a&m &#f7de1a&m &#f8d91a&m &#f8d41a&m &#f8cf1a&m &#f8ca1a&m &#f9c519&m &#f9c019&m &#f9bb19&m &#f9b619&m &#fab119&m &#faad19&m &#faa819&m &#faa319&m &#fb9e19&m &#fb9919&m &#fb9419&m "));
        to.sendMessage(StringUtils.asColor("              &#F6FB1A&k&ll &#fb9419С&#f9b619п&#f8d91aа&#f6fb1aв&#f8d91aн&#f9b619е&#fb9419р &#F6FB1A&k&ll"));
        to.sendMessage(StringUtils.asColor("     &fПоявился на координатах"));
        to.spigot().sendMessage(pos);

        to.sendMessage(StringUtils.asColor("&#fb9419&m &#fb9919&m &#fb9e19&m &#faa319&m &#faa819&m &#faad19&m &#fab119&m &#f9b619&m &#f9bb19&m &#f9c019&m &#f9c519&m &#f8ca1a&m &#f8cf1a&m &#f8d41a&m &#f8d91a&m &#f7de1a&m &#f7e21a&m &#f7e71a&m &#f7ec1a&m &#f6f11a&m &#f6f61a&m &#f6fb1a&m &#f6f61a&m &#f6f11a&m &#f7ec1a&m &#f7e71a&m &#f7e21a&m &#f7de1a&m &#f8d91a&m &#f8d41a&m &#f8cf1a&m &#f8ca1a&m &#f9c519&m &#f9c019&m &#f9bb19&m &#f9b619&m &#fab119&m &#faad19&m &#faa819&m &#faa319&m &#fb9e19&m &#fb9919&m &#fb9419&m "));
    }
    public void sendAnchorMessage(AnchorEvent evt, Player to, BaseComponent[] pos) {
        if(evt.getAnchorSubtype().equals(AnchorSubtype.MIRNI)) {
            to.sendMessage(StringUtils.asColor("&#21acfb&m &#21b0fb&m &#22b3fb&m &#22b7fb&m &#23bbfb&m &#23bffb&m &#23c2fb&m &#24c6fb&m &#24cafb&m &#24cdfb&m &#25d1fb&m &#25d5fb&m &#26d9fb&m &#26dcfb&m &#26e0fb&m &#27e4fb&m &#27e7fb&m &#27ebfb&m &#28effb&m &#28f3fb&m &#29f6fb&m &#29fafb&m &#29f6fb&m &#28f3fb&m &#28effb&m &#27ebfb&m &#27e7fb&m &#27e4fb&m &#26e0fb&m &#26dcfb&m &#26d9fb&m &#25d5fb&m &#25d1fb&m &#24cdfb&m &#24cafb&m &#24c6fb&m &#23c2fb&m &#23bffb&m &#23bbfb&m &#22b7fb&m &#22b3fb&m &#21b0fb&m &#21acfb&m "));
            to.sendMessage(StringUtils.asColor("           &#3F95FB&k&ll &#21acfbМ&#22b4fbи&#23bcfbр&#23c3fbн&#24cbfbы&#25d3fbй &#26dbfbЯ&#27e3fbк&#27eafbо&#28f2fbр&#29fafbь &#3F95FB&k&ll"));
            to.sendMessage(StringUtils.asColor("         &fРедкость: " + getRarityData(evt)));
            to.sendMessage(StringUtils.asColor("     &fПоявился на координатах"));
            to.spigot().sendMessage(pos);

            to.sendMessage(StringUtils.asColor("&#21acfb&m &#21b0fb&m &#22b3fb&m &#22b7fb&m &#23bbfb&m &#23bffb&m &#23c2fb&m &#24c6fb&m &#24cafb&m &#24cdfb&m &#25d1fb&m &#25d5fb&m &#26d9fb&m &#26dcfb&m &#26e0fb&m &#27e4fb&m &#27e7fb&m &#27ebfb&m &#28effb&m &#28f3fb&m &#29f6fb&m &#29fafb&m &#29f6fb&m &#28f3fb&m &#28effb&m &#27ebfb&m &#27e7fb&m &#27e4fb&m &#26e0fb&m &#26dcfb&m &#26d9fb&m &#25d5fb&m &#25d1fb&m &#24cdfb&m &#24cafb&m &#24c6fb&m &#23c2fb&m &#23bffb&m &#23bbfb&m &#22b7fb&m &#22b3fb&m &#21b0fb&m &#21acfb&m "));

        }else {
            to.sendMessage(StringUtils.asColor("&#e9671e&m &#e9621d&m &#e95d1b&m &#e9581a&m &#e95318&m &#e94e17&m &#e94a15&m &#e94514&m &#e94013&m &#e93b11&m &#e93610&m &#e9310e&m &#e92c0d&m &#e9270b&m &#e9220a&m &#e91d09&m &#e91907&m &#e91406&m &#e90f04&m &#e90a03&m &#e90501&m &#e90000&m &#e90501&m &#e90a03&m &#e90f04&m &#e91406&m &#e91907&m &#e91d09&m &#e9220a&m &#e9270b&m &#e92c0d&m &#e9310e&m &#e93610&m &#e93b11&m &#e94013&m &#e94514&m &#e94a15&m &#e94e17&m &#e95318&m &#e9581a&m &#e95d1b&m &#e9621d&m &#e9671e&m "));
            to.sendMessage(StringUtils.asColor("              &6&k&ll &#E90000Якорь &6&k&ll"));
            to.sendMessage(StringUtils.asColor("       &fРедкость: " + getRarityData(evt)));
            to.sendMessage(StringUtils.asColor("     &fПоявился на координатах"));
            to.spigot().sendMessage(pos);//sendMessage(StringUtils.AsColor("            &#E90000[" + x + ";" + y + "; " + z + "]"));

            to.sendMessage(StringUtils.asColor("&#e9671e&m &#e9621d&m &#e95d1b&m &#e9581a&m &#e95318&m &#e94e17&m &#e94a15&m &#e94514&m &#e94013&m &#e93b11&m &#e93610&m &#e9310e&m &#e92c0d&m &#e9270b&m &#e9220a&m &#e91d09&m &#e91907&m &#e91406&m &#e90f04&m &#e90a03&m &#e90501&m &#e90000&m &#e90501&m &#e90a03&m &#e90f04&m &#e91406&m &#e91907&m &#e91d09&m &#e9220a&m &#e9270b&m &#e92c0d&m &#e9310e&m &#e93610&m &#e93b11&m &#e94013&m &#e94514&m &#e94a15&m &#e94e17&m &#e95318&m &#e9581a&m &#e95d1b&m &#e9621d&m &#e9671e&m "));
        }
    }
    public static String getRarityData (AnchorEvent evt) {
        return switch (evt.getAnchorType()) {
            case DEFAULT -> "&#27F084Обычный";
            case RARE -> "&#F06413Редкий";
            case EPIC -> "&#41bf23Д&#32d417р&#22e80cе&#13fd00в&#22e80cн&#32d417и&#41bf23й";
            case MYTHIC -> "&#c927f0М&#d524ebи&#e121e6ф&#ed1ee1и&#f91bdcч&#f91bdcе&#ed1ee1с&#e121e6к&#d524ebи&#c927f0й";
        };
    }
}
