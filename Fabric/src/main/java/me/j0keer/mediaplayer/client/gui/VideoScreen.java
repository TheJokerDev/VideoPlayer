package me.j0keer.mediaplayer.client.gui;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.lib720.caprica.vlcj.player.base.State;
import me.srrapero720.watermedia.api.math.MathAPI;
import me.srrapero720.watermedia.api.player.SyncVideoPlayer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import me.j0keer.mediaplayer.Main;
import me.j0keer.mediaplayer.client.ClientHandler;
import me.j0keer.mediaplayer.util.KeyBinding;
import me.j0keer.mediaplayer.util.URLFixer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class VideoScreen extends Screen {
    private static final DateFormat FORMAT = new SimpleDateFormat("HH:mm:ss");
    static {
        FORMAT.setTimeZone(TimeZone.getTimeZone("GMT-00:00"));
    }

    private final SyncVideoPlayer player;
    private boolean activeFadeIn = false;
    private final long fadeIn;
    private boolean activeFadeOut = false;
    private boolean activeVolumeChange = false;
    private final long fadeOut;
    private int volume;

    private double masterVolume = -1;

    // STATUS
    int tick = 0;
    int closingOnTick = -1;
    float fadeLevel = 0;
    boolean started;
    boolean closing = false;

    // VIDEO INFO
    int videoTexture = -1;
    @Override
    protected void init() {
        if (MinecraftClient.getInstance().currentScreen != null) {
            this.width = MinecraftClient.getInstance().currentScreen.width;
            this.height = MinecraftClient.getInstance().currentScreen.height;
            ClientHandler.height = this.height;
            ClientHandler.width = this.width;
        }
        super.init();
    }

    public void pause(){
        this.player.pause();
    }

    public boolean isPaused(){
        return this.player.isPaused();
    }

    public void resume(){
        this.player.play();
    }

    int stoppedAttempts = 0;
    int otherAttempts = 0;

    public VideoScreen(JsonObject json) {
        super(Text.of(""));
        this.volume = (int) (json.get("volume").getAsInt() * getModifiedVolume());
        this.fadeIn = json.get("fadeIn").getAsInt();
        this.fadeOut = json.get("fadeOut").getAsInt();
        if (fadeIn > 0) {
            activeFadeIn = true;
        }
        if (fadeOut > 0) {
            activeFadeOut = true;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        this.masterVolume = mc.options.getSoundVolume(SoundCategory.MASTER);

        mc.options.getSoundVolumeOption(SoundCategory.MASTER).setValue(0.001);

        this.player = new SyncVideoPlayer(mc);
        setVolume(activeFadeIn ? 0 : volume);
        player.start(URLFixer.fix(json.get("url").getAsString(), json.get("fallback").getAsString(), URLFixer.MediaType.VIDEO));

        boolean loop = json.get("loop").getAsBoolean();
        player.setRepeatMode(loop);
        player.raw().mediaPlayer().controls().setRepeat(loop);

        Main.SCREEN = this;
        started = true;
    }

    private double tempVolume = -1;

    private double volumeChange = -1;
    private double finalVolume = -1;
    private boolean added = false;

    public void setVolume(int volume, double time){
        this.volume = (int) (volume * getModifiedVolume());
        if (player == null) return;
        if (time > 0 && getVolume() != volume){
            tempVolume = getVolume();
            activeVolumeChange = true;
            volumeChange = calculateVolumeIncrement(getVolume(), volume, time);
            finalVolume = volume;
            added = getVolume() < volume;
            return;
        }
        setVolume(volume);
    }

    private double optionVolume = -1;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta){
        if (!started) return;

        videoTexture = player.prepareTexture();

        if (player.isEnded() || player.isStopped() || player.getRawPlayerState().equals(State.ERROR)) {
            if (fadeLevel == 1 || closing) {
                closing = true;
                if (closingOnTick == -1) closingOnTick = tick + 20;
                if (tick >= closingOnTick) fadeLevel = Math.max(fadeLevel - (delta / 8), 0.0f);
                renderBlackBackground(context);
                if (fadeLevel == 0) close();
                return;
            }
        }

        boolean playingState = player.isPlaying() && player.getRawPlayerState().equals(State.PLAYING);
        fadeLevel = (playingState) ? Math.max(fadeLevel - (delta / 8), 0.0f) : Math.min(fadeLevel + (delta / 16), 1.0f);
        if (playingState || player.isStopped() || player.isEnded()) {
            renderTexture(context, videoTexture);
        }

        // DEBUG RENDERING
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            draw(context, String.format("State: %s", player.getRawPlayerState().name()), getHeightCenter(-12));
            draw(context, String.format("Time: %s (%s) / %s (%s)", FORMAT.format(new Date(player.getTime())), player.getTime(), FORMAT.format(new Date(player.getDuration())), player.getDuration()), getHeightCenter(0));
            draw(context, String.format("Media Duration: %s (%s)", FORMAT.format(new Date(player.getMediaInfoDuration())), player.getMediaInfoDuration()), getHeightCenter(12));
        }
    }

    private void renderBlackBackground(DrawContext stack) {
        RenderSystem.enableBlend();
        stack.fill(0, 0, width, height, MathAPI.argb((int) (fadeLevel * 255), 0, 0, 0));
        RenderSystem.disableBlend();
    }

    private int getHeightCenter(int offset) {
        return (height / 2) + offset;
    }

    private void draw(DrawContext stack, String text, int height) {
        stack.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, text, 10, height, 0xffffff);
    }

    private void renderTexture(DrawContext guiGraphics, int texture) {
        if (player.getDimensions() == null) return; // Checking if video available

        RenderSystem.enableBlend();
        guiGraphics.fill(0, 0, width, height, MathAPI.argb(255, 0, 0, 0));
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
        bufferBuilder.vertex(matrix4f, (float)0, (float)0, (float)0).texture(0, 0).next();
        bufferBuilder.vertex(matrix4f, (float)0, (float)height, (float)0).texture(0, 1).next();
        bufferBuilder.vertex(matrix4f, (float)width, (float)height, (float)0).texture(1, 1).next();
        bufferBuilder.vertex(matrix4f, (float)width, (float)0, (float)0).texture(1, 0).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public void onTick() {
        if (player == null) return;

        if (optionVolume != getModifiedVolume()) {
            double last = optionVolume;
            optionVolume = getModifiedVolume();
            setVolume(player.getVolume()/last);
        }

        if (player.isEnded()) {
            if (player.getRepeatMode()) {
                player.seekTo(0);
                if (fadeIn > 0) activeFadeIn = true;
                if (fadeOut > 0) activeFadeOut = true;
                setVolume(activeFadeIn ? 0 : volume);
                player.play();
                return;
            }
            Main.SCREEN = null;
            stop();
            return;
        }
        if (activeVolumeChange && finalVolume!=-1) {
            if ((tempVolume >= finalVolume && added) || (tempVolume <= finalVolume && !added)) {
                activeVolumeChange = false;
                volumeChange = -1;
                tempVolume = -1;
                finalVolume = -1;
                setVolume((int) finalVolume);
                return;
            }
            tempVolume += volumeChange;
            setVolume((int) tempVolume);
            return;
        }
        if (activeFadeIn) {
            if (player.getTime() != -1) {
                if (getVolume() >= (this.volume * getModifiedVolume())) {
                    activeFadeIn = false;
                    tempVolume = -1;
                    return;
                }
                double add = calculateVolumeIncrement(0, volume, fadeIn);
                if (tempVolume < 0) {
                    tempVolume = 0;
                }

                tempVolume += add;
                setVolume((int) tempVolume);
            }
            return;
        }

        if (activeFadeOut) {
            double left = player.getDuration() - player.getTime();
            left = left/1000;
            left = left*20;
            if (left <= fadeOut) {
                if (getVolume() <= 0) {
                    if (player.getRepeatMode()) {
                        activeFadeOut = true;
                        activeFadeIn = true;
                    }
                    tempVolume = -1;
                }
                if (tempVolume < 0) {
                    tempVolume = volume;
                }
                double remove = calculateVolumeIncrement(0, volume, fadeOut);
                tempVolume-=remove;
                setVolume((int) tempVolume);
            }
        }
    }

    public double getModifiedVolume() {
        return MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.RECORDS);
    }

    public int getVolume(){
        int currentVolume = player.getVolume();
        double modifiedVolume = getModifiedVolume();
        if (modifiedVolume > 0 && currentVolume == 0) {
            currentVolume = 1;
        }
        if (currentVolume == 0) return 0;
        return (int) (currentVolume / modifiedVolume);
    }

    public void setVolume (double volume) {
        player.setVolume((int) (volume * getModifiedVolume()));
    }

    public double calculateVolumeIncrement(int startVolume, int finalVolume, double fadeTime) {
        if (startVolume == finalVolume) {
            return 0;
        }
        return (double)(finalVolume - startVolume) / fadeTime;
    }

    public void stop(){
        player.stop();
        player.release();
    }

    @Override
    public void tick() {
        super.tick();
        onTick();
        tick++;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(KeyBinding.EXIT_KEY.matchesKey(keyCode,scanCode)){
            this.close();
            return true;
        }else
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
            stop();
            started = false;
            MinecraftClient.getInstance().options.getSoundVolumeOption(SoundCategory.MASTER).setValue(masterVolume);
            GlStateManager._deleteTexture(videoTexture);
            player.release();
        }
    }
}