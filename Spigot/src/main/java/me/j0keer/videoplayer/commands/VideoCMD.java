package me.j0keer.videoplayer.commands;

import me.j0keer.videoplayer.VideoPlayer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public record VideoCMD(VideoPlayer plugin) implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!sender.hasPermission("videoplayer.use")) {
            plugin.getUtils().sendMSG(sender, "{prefix}&cYou don't have permission to use this command!");
            return true;
        }
        if (args.length == 0) {
            plugin.getUtils().sendMSG(sender, "{prefix}&cUsage: /video <reload|list|play> [video] [player] <volume=>");
            return true;
        }
        String action = args[0].toLowerCase();

        switch (action) {
            case "reload" -> {
                if (!sender.hasPermission("videoplayer.reload")) {
                    plugin.getUtils().sendMSG(sender, "{prefix}&cYou don't have permission to use this command!");
                    return true;
                }
                plugin.reloadConfig();
                plugin.getUtils().sendMSG(sender, "{prefix}&aConfig reloaded!");
            }
            case "list" -> {
                BaseComponent[] prefix = TextComponent.fromLegacyText(plugin.getUtils().ct(plugin.getPrefix()+"&aVideos available:"));
                List<BaseComponent[]> components = new ArrayList<>() {{add(prefix);}};
                plugin.getUtils().sendMSG(sender, "{prefix}&aVideos available:");
                for (String video : getVideos()) {
                    TextComponent text = new TextComponent(TextComponent.fromLegacyText(plugin.getUtils().ct(" &7- &f" + video)));
                    text.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/video play @a " + video));
                    components.add(new BaseComponent[] {text});
                }
                plugin.getUtils().sendMSG(sender, components.toArray(new BaseComponent[0][0]));
            }
            case "play" -> {
                if (args.length < 3) {
                    plugin.getUtils().sendMSG(sender, "{prefix}&cUsage: /video play [player] [video] <volume=>");
                    return true;
                }
                String video = args[2].toLowerCase();
                List<Player> players = plugin.getUtils().getPlayers(sender, args[1]);
                if (players.isEmpty()) {
                    plugin.getUtils().sendMSG(sender, "{prefix}&cPlayer not found!");
                    return true;
                }
                if (!getVideos().contains(video)) {
                    plugin.getUtils().sendMSG(sender, "{prefix}&cVideo not found!");
                    return true;
                }
                String url = plugin.getConfig().getString("videos." + video+".url");
                int volume = plugin.getConfig().get("videos." + video + ".volume") == null ? 65 : plugin.getConfig().getInt("videos." + video + ".volume");

                if (args.length == 4) {
                    String volumeArg = args[3];
                    if (volumeArg.startsWith("volume=")) {
                        try {
                            volume = Integer.parseInt(volumeArg.substring(7));
                        } catch (NumberFormatException e) {
                            plugin.getUtils().sendMSG(sender, "{prefix}&cInvalid volume!");
                            return true;
                        }
                    }
                }
                plugin.getUtils().playVideo(players, url, volume);
                String player = players.size() == 1 ? players.get(0).getName() : "multiple players";
                plugin.getUtils().sendMSG(sender, "{prefix}&aPlaying video &f" + video + " &afor &f" + player + "&a...");
            }
            case "stop" -> {
                if (args.length < 2) {
                    plugin.getUtils().sendMSG(sender, "{prefix}&cUsage: /video stop [player]");
                    return true;
                }
                List<Player> players = plugin.getUtils().getPlayers(sender, args[1]);
                if (players.isEmpty()) {
                    plugin.getUtils().sendMSG(sender, "{prefix}&cPlayer not found!");
                    return true;
                }
                plugin.getUtils().stopVideo(players);
                String player = players.size() == 1 ? players.get(0).getName() : "multiple players";
                plugin.getUtils().sendMSG(sender, "{prefix}&aStopped video for &f" + player + "&a...");
            }
            case "volume" -> {
                if (args.length < 3) {
                    plugin.getUtils().sendMSG(sender, "{prefix}&cUsage: /video volume [player] <volume>");
                    return true;
                }
                List<Player> players = plugin.getUtils().getPlayers(sender, args[1]);
                if (players.isEmpty()) {
                    plugin.getUtils().sendMSG(sender, "{prefix}&cPlayer not found!");
                    return true;
                }
                int volume;
                try {
                    volume = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    plugin.getUtils().sendMSG(sender, "{prefix}&cInvalid volume!");
                    return true;
                }
                plugin.getUtils().setVolume(players, volume);
                String player = players.size() == 1 ? players.get(0).getName() : "multiple players";
                plugin.getUtils().sendMSG(sender, "{prefix}&aSet volume to &f" + volume + " &afor &f" + player + "&a...");
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!sender.hasPermission("videoplayer.use")) {
            return new ArrayList<>();
        }
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], List.of("reload", "list", "play", "stop", "volume"), new ArrayList<>());
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
        if (args.length == 3 && args[0].equalsIgnoreCase("play")) {
            return StringUtil.copyPartialMatches(args[2], getVideos(), new ArrayList<>());
        }

        boolean volumeToTab = false;
        String argVolume = "";

        if (args.length == 4 && args[0].equalsIgnoreCase("play")) {
            volumeToTab = true;
            argVolume = args[3];
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("volume")) {
            volumeToTab = true;
            argVolume = args[2];
        }
        if (volumeToTab && !argVolume.isEmpty()) {
            return StringUtil.copyPartialMatches(argVolume, List.of("volume="), new ArrayList<>());
        }

        return new ArrayList<>();
    }

    public Set<String> getVideos() {
        return plugin.getConfig().getConfigurationSection("videos").getKeys(false);
    }
}
