package me.j0keer.mediaplayer.client;

import com.google.gson.JsonObject;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import me.j0keer.mediaplayer.Main;
import me.j0keer.mediaplayer.client.gui.VideoScreen;
import me.j0keer.mediaplayer.network.PacketHandler;
import me.j0keer.mediaplayer.util.KeyBinding;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ClientHandler implements ClientModInitializer {

    public static List<String> inDownload = new ArrayList<>();
    public static int height = 0;
    public static int width = 0;

    public static void openVideo(MinecraftClient client, JsonObject json) {
        if (Main.SCREEN != null) {
            Main.SCREEN.stop();
        }
        new Thread(() -> {
            client.execute(() -> client.setScreen(new VideoScreen(json)));
        }).start();
    }

    @Override
    public void onInitializeClient() {
        KeyBinding.register();
        PacketHandler.registerClient();
    }
}