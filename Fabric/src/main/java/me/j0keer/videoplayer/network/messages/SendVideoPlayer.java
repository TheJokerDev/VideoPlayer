package me.j0keer.videoplayer.network.messages;

import me.j0keer.videoplayer.client.ClientHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public class SendVideoPlayer {
    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        String name = buf.readString();
        int volume = buf.readInt();
        ClientHandler.openVideo(client, name, volume);
    }
}