package me.j0keer.mediaplayer.util;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public class StringUtil {

    @NotNull
    public static <T extends Collection<? super String>> T copyPartialMatches(@NotNull String token, @NotNull Iterable<String> originals, @NotNull T collection) throws UnsupportedOperationException, IllegalArgumentException {
        for (String string : originals) {
            if (startsWithIgnoreCase(string, token)) {
                collection.add(string);
            }
        }

        return collection;
    }

    public static boolean startsWithIgnoreCase(@NotNull String string, @NotNull String prefix) throws IllegalArgumentException, NullPointerException {
        if (prefix.isEmpty()) return true;
        return string.length() >= prefix.length() && string.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
