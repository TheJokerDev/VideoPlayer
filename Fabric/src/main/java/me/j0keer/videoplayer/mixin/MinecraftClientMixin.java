package me.j0keer.videoplayer.mixin;

import me.j0keer.videoplayer.VideoPlayer;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
    private void onDisconnect(CallbackInfo ci) {
        if (VideoPlayer.SCREEN != null) {
            VideoPlayer.SCREEN.close();
            VideoPlayer.SCREEN = null;
        }
    }
}