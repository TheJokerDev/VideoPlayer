package me.j0keer.mediaplayer.network;

import com.google.gson.JsonObject;
import me.j0keer.mediaplayer.network.messages.PacketListener;
import me.j0keer.mediaplayer.network.messages.VideoPlayerPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import me.j0keer.mediaplayer.util.Constants;

public class PacketHandler {
    public static final Identifier PLAY_VIDEO = new Identifier(Constants.MOD_ID, "networking");
    public static final Identifier STOP_VIDEO = new Identifier(Constants.MOD_ID, "unshow");
    public static final Identifier PAUSE_VIDEO = new Identifier(Constants.MOD_ID, "pausecin");

    public static final Identifier VIDEO_VOLUME = new Identifier(Constants.MOD_ID, "volume");

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(PLAY_VIDEO, VideoPlayerPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(PAUSE_VIDEO, PacketListener::pauseVideo);
        ClientPlayNetworking.registerGlobalReceiver(STOP_VIDEO, PacketListener::stopVideo);
        ClientPlayNetworking.registerGlobalReceiver(VIDEO_VOLUME, VideoPlayerPacket::changeVolume);
    }

    public static void sendPlayVideo(ServerPlayerEntity player, JsonObject json) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(json.toString());
        ServerPlayNetworking.send(player, PLAY_VIDEO, buf);
    }

    public static void sendStopVideo(ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        ServerPlayNetworking.send(player, STOP_VIDEO, buf);
    }

    public static void sendPauseVideo(ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        ServerPlayNetworking.send(player, PAUSE_VIDEO, buf);
    }

    public static void sendVideoVolume(ServerPlayerEntity player, int volume, int fadeTime) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(volume);
        buf.writeInt(fadeTime);
        ServerPlayNetworking.send(player, VIDEO_VOLUME, buf);
    }
}