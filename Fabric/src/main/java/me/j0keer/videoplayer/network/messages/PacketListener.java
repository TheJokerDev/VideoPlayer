package me.j0keer.videoplayer.network.messages;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import me.j0keer.videoplayer.VideoPlayer;

public class PacketListener {
    public static void stopVideo(MinecraftClient client, ClientPlayNetworkHandler ignoredHandler, PacketByteBuf ignoredBuf, PacketSender ignoredSender) {
        if (VideoPlayer.SCREEN != null) {
            client.execute(() -> {
                VideoPlayer.SCREEN.close();
                VideoPlayer.SCREEN = null;
            });
        }
    }

    public static void pauseVideo(MinecraftClient client, ClientPlayNetworkHandler ignoredHandler, PacketByteBuf ignoredBuf, PacketSender ignoredSender) {
        if (VideoPlayer.SCREEN != null) {
            client.execute(() -> {
                if (VideoPlayer.SCREEN.isPaused()) VideoPlayer.SCREEN.resume();
                else
                    VideoPlayer.SCREEN.pause();
            });
        }
    }

    public static void setVolume(MinecraftClient client, ClientPlayNetworkHandler ignoredHandler, PacketByteBuf ignoredBuf, PacketSender ignoredSender) {
        if (VideoPlayer.SCREEN != null) {
            client.execute(() -> {
                VideoPlayer.SCREEN.setVolume(ignoredBuf.readInt());
            });
        }
    }
}
