package ru.xopek.universalevents.util;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.md_5.bungee.api.ChatColor.COLOR_CHAR;

public class StringUtils {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F\\d]{6})");

    public static String asColor(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder builder = new StringBuilder(message.length() + 4 * 8);

        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(builder, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }

        message = matcher.appendTail(builder).toString();
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String getTime(long time) {

        long total = (int) Math.floor(time / 1000);

        int D = 0;
        int H = 0;
        int M = 0;
        int S = 0;

        for (; total > 86400; total -= 86400, D++) ;
        for (; total > 3600; total -= 3600, H++) ;
        for (; total > 60; total -= 60, M++) ;
        S += total;

        if (S == 0) return S + "c ";
        if (M == 0) return S + "c ";
        if (H == 0) return M + "м " + S + "с ";
        if (D == 0) return H + "ч " + M + "м " + S + "с ";
        return D + "д " + H + "ч " + M + "м " + S + "с ";
    }
}
