package me.j0keer.mediaplayer.commands;

import com.google.gson.JsonObject;
import me.j0keer.mediaplayer.MediaPlayer;
import me.j0keer.mediaplayer.netty.FriendlyByteBuf;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public record VideoCMD(MediaPlayer plugin) implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 0){
            sendMSG(commandSender, "§cUsa /video <play|stop|volume> <fadeIn> <fadeOut> <volume> <url>");
            return true;
        }
        String sub = strings[0];
        if(sub.equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sendMSG(commandSender, "{prefix}&aConfiguración recargada.");
            return true;
        }
        if (sub.equalsIgnoreCase("list")){
            BaseComponent[] prefix = TextComponent.fromLegacyText(plugin.getUtils().ct(plugin.getPrefix()+"&aVideos disponibles:"));
            List<BaseComponent[]> components = new ArrayList<>() {{add(prefix);}};
            for (String video : getFile().getKeys(false)) {
                TextComponent text = new TextComponent(TextComponent.fromLegacyText(plugin.getUtils().ct(" &7- &f" + video)));
                text.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/video play @a " + video));
                components.add(new BaseComponent[] {text});
            }
            plugin.getUtils().sendMSG(commandSender, components.toArray(new BaseComponent[0][0]));
        }
        if (sub.equalsIgnoreCase("play")){
            if (strings.length < 3){
                sendMSG(commandSender, "§cUsa /video play <player/s> <volume> <url> <fadeIn> <fadeOut> <loop>");
                return true;
            }
            String name = strings[1];
            List<Player> players = plugin.getUtils().getPlayers(commandSender, name);
            if (players.isEmpty()){
                sendMSG(commandSender, "{prefix}No se encontraron jugadores.");
                return true;
            }

            String url = strings[2];
            String fallback = "";
            int volume = 50;
            int fadeIn = 0;
            int fadeOut = 0;
            boolean loop = false;

            Vector<String> vector = new Vector<>(Arrays.stream(strings).toList());
            vector.remove(0);
            vector.remove(0);
            vector.remove(0);
            strings = vector.toArray(new String[0]);
            List<String> modifiers = new ArrayList<>(Arrays.stream(strings).toList());
            List<String> parameterTypes = new ArrayList<>();
            for (String value : modifiers) {
                value = value.toLowerCase();
                String type = value.split("=")[0];
                parameterTypes.add(type);
                String data = value.split("=")[1];
                switch (type) {
                    case "volume"-> {
                        try {
                            volume = Integer.parseInt(data);
                        } catch (NumberFormatException e) {
                            sendMSG(commandSender, "{prefix}&cEl volumen debe ser un número (volume=50)");
                            return true;
                        }
                    }
                    case "fadein"-> {
                        try {
                            fadeIn = Integer.parseInt(data);
                        } catch (NumberFormatException e) {
                            sendMSG(commandSender, "{prefix}&cEl fadeIn debe ser un número (fadein=50)");
                            return true;
                        }
                    }
                    case "fadeout"-> {
                        try {
                            fadeOut = Integer.parseInt(data);
                        } catch (NumberFormatException e) {
                            sendMSG(commandSender, "{prefix}&cEl fadeOut debe ser un número (fadeout=50)");
                            return true;
                        }
                    }

                    case "loop"-> {
                        try {
                            loop = Boolean.parseBoolean(data);
                        } catch (NumberFormatException e) {
                            sendMSG(commandSender, "{prefix}&cEl loop debe ser un booleano (loop=false)");
                            return true;
                        }
                    }

                    case "fallback" -> {
                        fallback = data;
                    }
                }
            }

            String audio = url;
            if (getFile().contains(url)){
                if (!parameterTypes.contains("volume")) volume = getFile().get(url+ ".volume") != null ? getFile().getInt(url+ ".volume") : volume;
                if (!parameterTypes.contains("fadein")) fadeIn = getFile().get(url+".fadeIn") != null ? getFile().getInt(url+".fadeIn") : fadeIn;
                if (!parameterTypes.contains("fadeout")) fadeOut = getFile().get(url+".fadeOut") != null ? getFile().getInt(url+".fadeOut") : fadeOut;
                if (!parameterTypes.contains("loop")) loop = getFile().get(url+".loop") != null && getFile().getBoolean(url+".loop");
                if (!parameterTypes.contains("fallback")) fallback = getFile().getString(url+ ".fallback");
                url = getFile().getString(url+ ".url");
            }

            JsonObject json = new JsonObject();
            json.addProperty("url", url);
            json.addProperty("volume", volume);
            json.addProperty("fadeIn", fadeIn);
            json.addProperty("fadeOut", fadeOut);
            json.addProperty("loop", loop);
            json.addProperty("fallback", fallback);

            players.forEach(p -> playVideo(p, json));
            String player = players.size() == 1 ? players.get(0).getName() : "múltiples jugadores";
            String parameters = modifiers.isEmpty() ? "" : " &acon los parámetros: &b"+String.join(", ", modifiers);
            plugin.getUtils().sendMSG(commandSender, "{prefix}&aReproduciendo &f"+audio+"&a para &f" + player + parameters+"&a...");
            return true;
        }
        if (sub.equalsIgnoreCase("stop")){
            if (strings.length < 2){
                sendMSG(commandSender, "§cUsa /video stop <player>");
                return true;
            }
            String name = strings[1];
            List<Player> players = plugin.getUtils().getPlayers(commandSender, name);
            if (players.isEmpty()){
                sendMSG(commandSender, "{prefix}No se encontraron jugadores.");
                return true;
            }

            players.forEach(this::stopVideo);
            String player = players.size() == 1 ? players.get(0).getName() : "múltiples jugadores";
            plugin.getUtils().sendMSG(commandSender, "{prefix}&aDeteniendo video para &f" + player + "&a...");
            return true;
        }
        if (sub.equalsIgnoreCase("pause")){
            if (strings.length < 2){
                sendMSG(commandSender, "§cUsa /video pause <player>");
                return true;
            }
            String name = strings[1];
            List<Player> players = plugin.getUtils().getPlayers(commandSender, name);
            if (players.isEmpty()){
                sendMSG(commandSender, "{prefix}No se encontraron jugadores.");
                return true;
            }

            players.forEach(this::pauseVideo);
            String player = players.size() == 1 ? players.get(0).getName() : "múltiples jugadores";
            plugin.getUtils().sendMSG(commandSender, "{prefix}&aPausando video para &f" + player + "&a...");
            return true;
        }
        if (sub.equalsIgnoreCase("volume")){
            if (strings.length < 3){
                sendMSG(commandSender, "§cUsa /video volume <player> [volume=50] [time=0]");
                return true;
            }
            String name = strings[1];
            List<Player> players = plugin.getUtils().getPlayers(commandSender, name);
            if (players.isEmpty()){
                sendMSG(commandSender, "{prefix}No se encontraron jugadores.");
                return true;
            }
            int volume = 0;
            int fadeTime = 0;

            Vector<String> vector = new Vector<>(Arrays.stream(strings).toList());
            vector.remove(0);
            vector.remove(0);
            strings = vector.toArray(new String[0]);
            List<String> modifiers = new ArrayList<>(Arrays.stream(strings).toList());
            for (String value : modifiers) {
                value = value.toLowerCase();
                String type = value.split("=")[0];
                String data = value.split("=")[1];
                switch (type) {
                    case "volume"-> {
                        try {
                            volume = Integer.parseInt(data);
                        } catch (NumberFormatException e) {
                            sendMSG(commandSender, "{prefix}&cEl volumen debe ser un número (volume=50)");
                            return true;
                        }
                    }
                    case "time"-> {
                        try {
                            fadeTime = Integer.parseInt(data);
                        } catch (NumberFormatException e) {
                            sendMSG(commandSender, "{prefix}&cEl time debe ser un número (time=50)");
                            return true;
                        }
                    }
                }
            }

            int finalVolume = volume;
            int finalFadeTime = fadeTime;
            players.forEach(p -> volume(p, finalVolume, finalFadeTime));
            String player = players.size() == 1 ? players.get(0).getName() : "múltiples jugadores";
            plugin.getUtils().sendMSG(commandSender, "{prefix}&aEstableciendo volumen a &f" + volume + " &apara &f" + player + "&a...");
            return true;
        }
        return false;
    }

    public void sendMSG(CommandSender sender, String... msg){
        plugin.getUtils().sendMSG(sender, msg);
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 1){
            return StringUtil.copyPartialMatches(args[0], List.of("play", "stop", "volume", "reload", "list"), new ArrayList<>());
        }
        if (args.length == 2 && (List.of("play", "stop", "volume").contains(args[0]))) {
            List<String> completions = new ArrayList<>(Arrays.asList("@a", "@e", "@p", "@r", "@s"));
            String arg = args[1];
            if (arg.startsWith("@a") || arg.startsWith("@e") || arg.startsWith("@p")){
                String init = arg.toLowerCase().split("\\[")[0];
                completions.clear();
                if (arg.contains("[")){
                    completions.add(init+"[gamemode=");
                    completions.add(init+"[permission=");
                    completions.add(init+"[group=");
                    if (arg.contains("gamemode=")){
                        completions.clear();
                        completions.add(init+"[gamemode=creative");
                        completions.add(init+"[gamemode=survival");
                        completions.add(init+"[gamemode=adventure");
                        completions.add(init+"[gamemode=spectator");
                    } else if (arg.contains("permission=")){
                        completions.clear();
                    } else if (arg.contains("group=")){
                        completions.clear();
                        plugin.getLuckPerms().getGroupManager().getLoadedGroups().forEach(group->completions.add(init+"[group="+group.getName()));
                    }
                } else {
                    completions.add(init+"[");
                }
                return StringUtil.copyPartialMatches(arg, completions, new ArrayList<>());
            } else {
                completions.addAll(plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList());
            }
            return StringUtil.copyPartialMatches(arg, completions, new ArrayList<>());
        }
        if (args.length == 3){
            if (args[0].equalsIgnoreCase("play")){
                return new ArrayList<>(getFile().getKeys(false));
            }
        }
        boolean isVolume = args[0].equalsIgnoreCase("volume");
        boolean allow = args[0].equalsIgnoreCase("play") || isVolume;
        if (args.length > (isVolume ? 2 : 3) && allow){
            String actual = args[args.length - 1];
            List<String> types = getStrings(args);
            return StringUtil.copyPartialMatches(actual, types, new ArrayList<>());
        }
        return new ArrayList<>();
    }

    private static List<String> getStrings(String[] args) {
        List<String> types = new ArrayList<>(Arrays.asList("volume=", "fadein=", "fadeout=", "loop="));
        if (args[0].equalsIgnoreCase("volume")){
            types.clear();
            types.add("volume=");
            types.add("time=");
        }
        for (String string : args) {
            List<String> type2 = new ArrayList<>(types);
            for (String s1 : type2) {
                if (string.startsWith(s1)){
                    types.remove(s1);
                }
            }
        }
        return types;
    }

    public void playVideo(Player player, JsonObject json){
        FriendlyByteBuf buf = new FriendlyByteBuf();
        try {
            buf.writeUtf(json.toString());
            player.sendPluginMessage(plugin, "mediaplayer:networking", buf.array());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            buf.clear();
        }
    }

    public void pauseVideo(Player player) {
        FriendlyByteBuf buf = new FriendlyByteBuf();
        try {
            buf.writeUtf("pause please");
            player.sendPluginMessage(plugin, "mediaplayer:pausecin", buf.array());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopVideo(Player player){
        FriendlyByteBuf buf = new FriendlyByteBuf();
        try {
            buf.writeUtf("stop please");
            player.sendPluginMessage(plugin, "mediaplayer:unshow", buf.array());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            buf.clear();
        }
    }

    public void volume(Player p, int volume, double time){
        FriendlyByteBuf buf = new FriendlyByteBuf();
        try {
            buf.writeInt(volume);
            buf.writeDouble(time);
            p.sendPluginMessage(plugin, "mediaplayer:volume", buf.array());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            buf.clear();
        }
    }

    public Configuration getFile(){
        return plugin.getConfig();
    }

}
