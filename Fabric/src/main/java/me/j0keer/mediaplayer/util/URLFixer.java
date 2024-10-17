package me.j0keer.mediaplayer.util;

import java.io.File;

public class URLFixer {
    public enum MediaType {
        AUDIO,
        VIDEO
    }
    public static String fix(String in, String fallback, MediaType type) {
        boolean isLocal = in.startsWith("{local}/");
        boolean isFile = in.startsWith("{file}/");

        if (isLocal) {
            File file = new File("config/"+Constants.MOD_ID+"/"+ type.name().toLowerCase() + "s/" +in.replace("{local}/", ""));
            if (!file.exists() && !fallback.isEmpty()) {
                return fix(fallback, null, type);
            }

            String path = "{local}/" + file.getAbsolutePath();
            path = path.replace("{local}/", "local://");
            return path;
        }

        if (isFile) {
            File file = new File(in.replace("{file}/", ""));
            if (!file.exists() && fallback != null) {
                return fix(fallback, null, type);
            }

            String path = "{file}/" + file.getAbsolutePath();
            path = path.replace("{file}/", "local://");
            return path;
        }

        return in;
    }
}
