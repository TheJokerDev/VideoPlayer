package me.j0keer.videoplayer;

import lombok.Getter;
import me.j0keer.videoplayer.commands.VideoCMD;
import me.j0keer.videoplayer.utils.Utils;
import net.luckperms.api.LuckPerms;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class VideoPlayer extends JavaPlugin {
    private Utils utils;

    private LuckPerms luckPerms;

    @Override
    public void onEnable() {
        long ms = System.currentTimeMillis();
        saveDefaultConfig();
        utils = new Utils(this);

        console("{prefix}Enabling VideoPlayer &7v" + getDescription().getVersion() + "&f...");
        PluginManager pm = getServer().getPluginManager();

        console("{prefix}Loading dependencies...");
        if (pm.getPlugin("PlaceholderAPI") != null) {
            console("{prefix}  &aHooked into PlaceholderAPI!");
        } else {
            console("{prefix}  &cPlaceholderAPI not found! Disabling plugin...");
            pm.disablePlugin(this);
            return;
        }

        if (pm.isPluginEnabled("LuckPerms")){
            RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null) {
                luckPerms = provider.getProvider();
            }
            console("{prefix}  &aHooked into LuckPerms!");
        } else {
            console("{prefix}  &cLuckPerms not found! Disabling plugin...");
            pm.disablePlugin(this);
            return;
        }

        loadCmds();
        registerOutgoingChannel();

        ms = System.currentTimeMillis() - ms;
        console("{prefix}&aEnabled VideoPlayer in " + ms + "ms!");
        console("{prefix}Developed with <3 by &eJ0keer&f.");
    }

    public void loadCmds() {
        console("{prefix}Loading commands...");
        VideoCMD videoCMD = new VideoCMD(this);
        getCommand("video").setExecutor(videoCMD);
        getCommand("video").setTabCompleter(videoCMD);
        console("{prefix}  &aCommand &f/video &aloaded!");
    }

    public void registerOutgoingChannel() {
        console("{prefix}Registering outgoing channels...");
        getServer().getMessenger().registerOutgoingPluginChannel(this, "videoplayer:networking");
        getServer().getMessenger().registerOutgoingPluginChannel(this, "videoplayer:unshow");
        getServer().getMessenger().registerOutgoingPluginChannel(this, "videoplayer:volume");
        console("{prefix}  &aOutgoing channels registered!");
    }

    @Override
    public void onDisable() {
        console("{prefix}Disabling VideoPlayer &7v" + getDescription().getVersion() + "&f...");
        console("{prefix}See you soon!");
    }

    public void console(String... msg){
        getUtils().sendMSG(getServer().getConsoleSender(), msg);
    }

    public String getPrefix() {
        return getConfig().getString("prefix", "&bVideoPlayer&8 » &f");
    }
}
