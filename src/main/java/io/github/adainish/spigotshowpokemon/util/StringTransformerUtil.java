package io.github.adainish.spigotshowpokemon.util;

public class StringTransformerUtil {
    public static String formattedString(String s) {
        if (s == null)
            return "";
        return s.replaceAll("&", "§");
    }
}
