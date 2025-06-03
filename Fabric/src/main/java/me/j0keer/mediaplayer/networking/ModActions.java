package me.j0keer.mediaplayer.networking;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.j0keer.mediaplayer.Main;
import me.j0keer.mediaplayer.client.gui.VideoScreen;
import net.minecraft.client.MinecraftClient;

public class ModActions {
    public static boolean execute(String text) {
        if (text.startsWith("[mediaplayer]")) {
            text = text.replace("[mediaplayer]", "");

            JsonObject json = JsonParser.parseString(text).getAsJsonObject();

            String action = json.get("action").getAsString().toLowerCase();

            switch (action) {
                case "play" -> {
                    MinecraftClient.getInstance().execute(() -> {
                        if (Main.SCREEN != null) {
                            Main.SCREEN.close();
                        }
                        Main.SCREEN = new VideoScreen(json);
                        MinecraftClient.getInstance().setScreen(Main.SCREEN);
                    });
                }
                case "stop" -> {
                    MinecraftClient.getInstance().execute(() -> {
                        if (Main.SCREEN != null) {
                            Main.SCREEN.close();
                            Main.SCREEN = null;
                        }
                    });
                }
                case "pause" -> {
                    MinecraftClient.getInstance().execute(() -> {
                        if (Main.SCREEN != null) {
                            if (Main.SCREEN.isPaused()) {
                                Main.SCREEN.resume();
                            } else {
                                Main.SCREEN.pause();
                            }
                        }
                    });
                }
                case "volume" -> {
                    MinecraftClient.getInstance().execute(() -> {
                        if (Main.SCREEN != null) {
                            int volume = json.get("volume").getAsInt();
                            double time = json.has("time") ? json.get("time").getAsDouble() : 0.0;
                            Main.SCREEN.setVolume(volume, time);
                        }
                    });
                }
            }

            return true;
        }

        return false;
    }
}
