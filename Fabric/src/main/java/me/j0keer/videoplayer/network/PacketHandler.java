package me.j0keer.videoplayer.network;

import me.j0keer.videoplayer.network.messages.PacketListener;
import me.j0keer.videoplayer.network.messages.SendVideoPlayer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import me.j0keer.videoplayer.util.Constants;

public class PacketHandler {
    public final Identifier PLAY_VIDEO = new Identifier(Constants.MOD_ID, "networking");
    public final Identifier STOP_VIDEO = new Identifier(Constants.MOD_ID, "unshow");
    public final Identifier PAUSE_VIDEO = new Identifier(Constants.MOD_ID, "pausecin");
    public final Identifier VIDEO_VOLUME = new Identifier(Constants.MOD_ID, "volume");

    public void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(PLAY_VIDEO, SendVideoPlayer::receive);
        ClientPlayNetworking.registerGlobalReceiver(STOP_VIDEO, PacketListener::stopVideo);
        ClientPlayNetworking.registerGlobalReceiver(PAUSE_VIDEO, PacketListener::pauseVideo);
        ClientPlayNetworking.registerGlobalReceiver(VIDEO_VOLUME, PacketListener::setVolume);
    }

    public void sendPlay(ServerPlayerEntity player, String name, int volume) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(name);
        buf.writeInt(volume);
        ServerPlayNetworking.send(player, PLAY_VIDEO, buf);
    }

    public void sendStop(ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        ServerPlayNetworking.send(player, STOP_VIDEO, buf);
    }

    public void sendPause(ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        ServerPlayNetworking.send(player, PAUSE_VIDEO, buf);
    }

    public void sendVolume(ServerPlayerEntity player, int volume) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(volume);
        ServerPlayNetworking.send(player, VIDEO_VOLUME, buf);
    }
}