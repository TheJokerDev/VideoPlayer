package me.j0keer.mediaplayer.network.messages;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.j0keer.mediaplayer.Main;
import me.j0keer.mediaplayer.client.ClientHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public class VideoPlayerPacket {
    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        String jsonStr = buf.readString();
        JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
        ClientHandler.openVideo(client, json);
    }

    public static void changeVolume(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int volume = buf.readInt();
        double time = -1;
        try {
            time = buf.readDouble();
        } catch (Exception ignored) {}

        if (Main.SCREEN != null) {
            double finalTime = time;
            client.execute(() -> {
                Main.SCREEN.setVolume(volume, finalTime);
            });
        }
    }
}