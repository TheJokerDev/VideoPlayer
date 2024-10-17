package me.j0keer.mediaplayer.network.messages;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import me.j0keer.mediaplayer.Main;
import me.j0keer.mediaplayer.client.ClientHandler;

public class PacketListener {
    public static void stopVideo(MinecraftClient client, ClientPlayNetworkHandler ignoredHandler, PacketByteBuf ignoredBuf, PacketSender ignoredSender) {
        int fadeOut = 0;
        try {
            fadeOut = ignoredBuf.readInt();
        } catch (Exception ignored) {
        }
        if (Main.SCREEN != null) {
            client.execute(() -> {
                Main.SCREEN.close();
                Main.SCREEN.resize(client, ClientHandler.width, ClientHandler.height);
                ClientHandler.width = 0;
                ClientHandler.height = 0;
                Main.SCREEN = null;
            });
        }
    }

    public static void pauseVideo(MinecraftClient client, ClientPlayNetworkHandler ignoredHandler, PacketByteBuf ignoredBuf, PacketSender ignoredSender) {
        if (Main.SCREEN != null) {
            client.execute(() -> {
                if (Main.SCREEN.isPaused()) Main.SCREEN.resume();
                else
                    Main.SCREEN.pause();
            });
        }
    }
}
