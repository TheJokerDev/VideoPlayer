package me.j0keer.mediaplayer;

import lombok.Getter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import me.j0keer.mediaplayer.client.gui.VideoScreen;
import me.j0keer.mediaplayer.commands.VideoCommand;
import me.j0keer.mediaplayer.util.Constants;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import me.j0keer.mediaplayer.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;


@Getter
public class Main implements ModInitializer {
    public FileUtils config;
    public static Main INSTANCE;

    public static VideoScreen SCREEN = null;

    private static MinecraftServer server;

    @Override
    public void onInitialize() {
        INSTANCE = this;
        Constants.LOGGER.info("VideoPlayer Mod is initialized");

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (server != null) {
                server = client.getServer();
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, registrationEnvironment) -> {
            new VideoCommand().register(dispatcher);
        });

        saveDefaultConfig();
    }

    public static void log(String... msg){
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) return;
        if (server == null) return;
        Arrays.stream(msg).toList().forEach(s->server.getPlayerManager().getPlayerList().forEach(p->p.sendMessage(Text.of(s))));
    }

    public File getDataFolder() {
        return new File("config/" + Constants.MOD_ID + "/");
    }

    public void saveResource(String file, File f){
        if (!f.exists()){
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("config/"+file)){
                if (in == null) {
                    Constants.LOGGER.error("Could not find " + file + " in jar file. Please report this to the mod author.");
                    return;
                };
                Files.copy(in, f.toPath());
            } catch (IOException ignored) {
            }
        }
    }

    public void saveDefaultConfig(){
        File file = new File(getDataFolder(), "config.yml");
        File configFolder = new File("config");
        if (!configFolder.exists()){
            configFolder.mkdir();
        }
        if (!getDataFolder().exists()){
            getDataFolder().mkdir();
        }
        if (!file.exists()){
            saveResource("config.yml", file);
        }
        config = new FileUtils(file);
    }

    public void saveConfig(){
        config.save();
    }

    public void reloadConfig(){
        config.init();
    }
}