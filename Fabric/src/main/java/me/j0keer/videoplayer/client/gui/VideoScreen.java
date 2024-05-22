package me.j0keer.videoplayer.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.j0keer.videoplayer.VideoPlayer;
import me.j0keer.videoplayer.client.ClientHandler;
import me.j0keer.videoplayer.util.Constants;
import me.lib720.caprica.vlcj.player.base.State;
import me.srrapero720.watermedia.api.WaterMediaAPI;
import me.srrapero720.watermedia.api.player.SyncVideoPlayer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import me.j0keer.videoplayer.util.KeyBinding;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class VideoScreen extends Screen {
    private static final DateFormat FORMAT = new SimpleDateFormat("HH:mm:ss");

    static {
        FORMAT.setTimeZone(TimeZone.getTimeZone("GMT-00:00"));
    }

    // STATUS
    int tick = 0;
    boolean started;

    // TOOLS
    private final SyncVideoPlayer player;

    // VIDEO INFO
    int videoTexture = -1;

    @Override
    protected void init() {
        super.init();
    }

    double volume;

    public VideoScreen(String video, int volume) {
        super(Text.of(""));
        MinecraftClient mc = MinecraftClient.getInstance();
        this.volume = mc.options.getSoundVolume(SoundCategory.AMBIENT);
        Constants.LOGGER.info("Volume: " + this.volume);

        mc.options.getSoundVolumeOption(SoundCategory.AMBIENT).setValue(0.001);
        Constants.LOGGER.info("Volume set off: " + 0.001f);

        VideoPlayer.SCREEN = this;

        this.player = new SyncVideoPlayer(null, mc, GlAllocationUtils::allocateByteBuffer);
        player.setVolume(volume);
        player.start(video);

        started = true;
    }

    public void setVolume(int volume) {
        this.player.setVolume(volume);
    }

    public void pause() {
        this.player.pause();
    }

    public boolean isPaused() {
        return this.player.isPaused();
    }

    public void resume() {
        this.player.play();
    }
    int stoppedAttempts = 0;
    int otherAttempts = 0;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!started) return;

        videoTexture = player.prepareTexture();

        boolean playingState = player.isPlaying() && player.getRawPlayerState().equals(State.PLAYING);
        State playerState = player.getRawPlayerState();

        if (playingState) {
            if (stoppedAttempts > 0) stoppedAttempts = 0;
            if (otherAttempts > 0) otherAttempts = 0;
            renderTexture(context, videoTexture);
        } else if (player.isStopped() || player.isEnded()) {
            if (stoppedAttempts < 10) {
                stoppedAttempts++;
                return;
            }
            this.close();
        } else {
            boolean close = false;

            if (playerState.equals(State.ERROR)) {
                close = true;
            } else if (playerState.equals(State.NOTHING_SPECIAL)) {
                otherAttempts++;
                if (otherAttempts > 80) {
                    close = true;
                }
            }

            if (close) {
                this.close();
                return;
            }
        }

        // DEBUG RENDERING
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            ClientHandler.log("State: " + playerState.name() + " | Attempts: "+(stoppedAttempts + otherAttempts));
            draw(context, String.format("State: %s", player.getRawPlayerState().name()), getHeightCenter(-12));
            draw(context, String.format("Time: %s (%s) / %s (%s)", FORMAT.format(new Date(player.getTime())), player.getTime(), FORMAT.format(new Date(player.getDuration())), player.getDuration()), getHeightCenter(0));
            draw(context, String.format("Media Duration: %s (%s)", FORMAT.format(new Date(player.getMediaInfoDuration())), player.getMediaInfoDuration()), getHeightCenter(12));
        }
    }

    private int getHeightCenter(int offset) {
        return (height / 2) + offset;
    }

    private void draw(DrawContext stack, String text, int height) {
        stack.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, text, 0, height, 0xffffff);
    }

    private void renderTexture(DrawContext guiGraphics, int texture) {
        if (player.getDimensions() == null) return; // Checking if video available

        RenderSystem.enableBlend();
        guiGraphics.fill(0, 0, width, height, WaterMediaAPI.math_colorARGB(255, 0, 0, 0));
        RenderSystem.disableBlend();

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texture);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        Matrix4f matrix4f = guiGraphics.getMatrices().peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix4f, (float) 0, (float) 0, (float) 0).texture(0, 0).next();
        bufferBuilder.vertex(matrix4f, (float) 0, (float) height, (float) 0).texture(0, 1).next();
        bufferBuilder.vertex(matrix4f, (float) width, (float) height, (float) 0).texture(1, 1).next();
        bufferBuilder.vertex(matrix4f, (float) width, (float) 0, (float) 0).texture(1, 0).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    @Override
    public void tick() {
        super.tick();
        tick++;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyBinding.EXIT_KEY.matchesKey(keyCode, scanCode)) {
            this.close();
            return true;
        } else
            return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void close() {
        super.close();
        stoppedAttempts = 0;
        otherAttempts = 0;
        if (started) {
            this.player.stop();
            started = false;
            Constants.LOGGER.info("Volume set on: " + this.volume);
            MinecraftClient.getInstance().options.getSoundVolumeOption(SoundCategory.AMBIENT).setValue(volume);
            GlStateManager._deleteTexture(videoTexture);
            player.release();
        }
    }

}
