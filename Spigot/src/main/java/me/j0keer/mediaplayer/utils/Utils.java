package me.j0keer.mediaplayer.utils;

import me.j0keer.mediaplayer.MediaPlayer;
import me.j0keer.mediaplayer.netty.FriendlyByteBuf;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public record Utils(MediaPlayer plugin) {

    public String ct(String in) {
        return ChatColor.translateAlternateColorCodes('&', in);
    }

    public void sendMSG(CommandSender sender, String... msg) {
        for (String m : msg) {
            sendMSG(sender, m);
        }
    }

    public void sendMSG(CommandSender sender, BaseComponent[]... msg) {
        for (BaseComponent[] m : msg) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(TextComponent.toPlainText(m));
            } else {
                p.spigot().sendMessage(m);
            }
        }
    }

    public void sendMSG(CommandSender sender, String msg) {
        if (msg.contains("{prefix}")) {
            msg = msg.replace("{prefix}", plugin.getPrefix());
        }
        msg = ct(msg);

        if (!(sender instanceof Player p)) {
            sender.sendMessage(msg);
        } else {
            p.sendMessage(msg);
        }
    }

    public List<Player> getPlayers(CommandSender sender, String arg){
        List<Player> players = new ArrayList<>();
        Collection<String> filters = arg.contains("[") ? Arrays.stream(arg.substring(arg.indexOf("[")+1, arg.indexOf("]")).split(",")).toList() : new ArrayList<>();
        String selector = arg.contains("[") ? arg.substring(0, arg.indexOf("[")) : arg;
        if (selector.equalsIgnoreCase("@a") || selector.equalsIgnoreCase("@e") || selector.equalsIgnoreCase("@p")){
            players.addAll(plugin().getServer().getOnlinePlayers());
            for (String filter : filters){
                String key = filter.split("=")[0];
                String value = filter.split("=")[1];
                if (key.equalsIgnoreCase("gamemode")){
                    players.removeIf(p->!p.getGameMode().name().equalsIgnoreCase(value.toUpperCase()));
                }
                if (key.equalsIgnoreCase("permission")){
                    players.removeIf(p->!p.hasPermission(value));
                }
                if (key.equalsIgnoreCase("group")){
                    players.removeIf(p-> {
                        User user = plugin().getLuckPerms().getUserManager().getUser(p.getUniqueId());
                        //Get groups into List<String>
                        if (user == null) return true;
                        return !user.getPrimaryGroup().equalsIgnoreCase(value);
                    });
                }
            }
        } else if (selector.equalsIgnoreCase("@r")){
            players.add(plugin().getServer().getOnlinePlayers().stream().findAny().orElse(null));
        } else if (selector.equalsIgnoreCase("@s")){
            if (sender instanceof Player){
                players.add((Player) sender);
            }
        } else {
            Player p = plugin().getServer().getPlayer(selector);
            if (p != null){
                players.add(p);
            }
        }
        return players;
    }

    public void playVideo(List<Player> players, String video, int volume){
        FriendlyByteBuf buf = new FriendlyByteBuf();
        try {
            buf.writeUtf(video);
            buf.writeInt(volume);
            players.forEach(p->p.sendPluginMessage(plugin(), "videoplayer:networking", buf.array()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            buf.clear();
        }
    }

    public void stopVideo(List<Player> players){
        FriendlyByteBuf buf = new FriendlyByteBuf();
        try {
            buf.writeUtf("stop");
            players.forEach(p->p.sendPluginMessage(plugin(), "videoplayer:unshow", buf.array()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            buf.clear();
        }
    }

    public void setVolume(List<Player> players, int volume){
        FriendlyByteBuf buf = new FriendlyByteBuf();
        try {
            buf.writeInt(volume);
            players.forEach(p->p.sendPluginMessage(plugin(), "videoplayer:volume", buf.array()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            buf.clear();
        }
    }
}
