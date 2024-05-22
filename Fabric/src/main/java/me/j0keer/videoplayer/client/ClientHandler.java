package me.j0keer.videoplayer.client;

import me.j0keer.videoplayer.network.PacketHandler;
import me.j0keer.videoplayer.util.Constants;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import me.j0keer.videoplayer.client.gui.VideoScreen;
import me.j0keer.videoplayer.util.KeyBinding;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class ClientHandler implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Constants.LOGGER.info("Client side loaded");

        KeyBinding.register();

        PacketHandler packetHandler = new PacketHandler();
        packetHandler.registerClient();
    }

    public static void openVideo(MinecraftClient client, String video, int volume) {
        client.execute(() -> client.setScreen(new VideoScreen(video, volume)));
    }

    private static MinecraftClient getMC() {
        return MinecraftClient.getInstance();
    }

    private static boolean isDev() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static void log(String msg) {
        if (!isDev()) return;
        getMC().player.sendMessage(Text.of(msg));
    }

    public static void actionBar(String msg) {
        if (!isDev()) return;
        getMC().player.sendMessage(Text.of(msg), true);
    }
}