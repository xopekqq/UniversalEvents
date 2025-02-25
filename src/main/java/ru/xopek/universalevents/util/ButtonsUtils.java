package ru.xopek.universalevents.util;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;

public class ButtonsUtils {
    private TextComponent button;

    public ButtonsUtils(String buttonText) {
        this.button = new TextComponent(TextComponent.fromLegacyText(StringUtils.asColor(buttonText)));
    }

    public void setHover(String textToShow) {
        this.button.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new Content[]{new Text(StringUtils.asColor(textToShow))}));
    }

    public void setClickEvent(String commandString) {
        this.button.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, commandString));
    }

    public TextComponent getButton() {
        return this.button;
    }
}
