package me.j0keer.mediaplayer.commands;

import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import me.j0keer.mediaplayer.Main;
import me.j0keer.mediaplayer.config.Configuration;
import me.j0keer.mediaplayer.network.PacketHandler;
import me.j0keer.mediaplayer.util.StringUtil;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class VideoCommand extends CMD {

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("video")
                .requires((command) -> command.hasPermissionLevel(2))
                .then(CommandManager.literal("list")
                        .executes(this::list))
                .then(CommandManager.literal("play")
                        .then(CommandManager.argument("target", EntityArgumentType.players())
                                .then(CommandManager.argument("video", StringArgumentType.string())
                                        .suggests((context, builder) -> suggest(context, builder, "video", getVideoList()))
                                        .executes(this::play)
                                        .then(
                                                CommandManager.argument("param1", StringArgumentType.string())
                                                        .suggests((context, builder) -> suggestParameters(context, builder, "param1", getParameterTypes(false)))
                                                        .executes(this::play)
                                                        .then(
                                                                CommandManager.argument("param2", StringArgumentType.string())
                                                                        .suggests((context, builder) -> suggestParameters(context, builder, "param2", getParameterTypes(false)))
                                                                        .executes(this::play)
                                                                        .then(
                                                                                CommandManager.argument("param3", StringArgumentType.string())
                                                                                        .suggests((context, builder) -> suggestParameters(context, builder, "param3", getParameterTypes(false)))
                                                                                        .executes(this::play)
                                                                                        .then(
                                                                                                CommandManager.argument("param4", StringArgumentType.string())
                                                                                                        .suggests((context, builder) -> suggestParameters(context, builder, "param4", getParameterTypes(false)))
                                                                                                        .executes(this::play)
                                                                                        )
                                                                        )
                                                        )
                                        )
                                )
                        )
                )
                .then(CommandManager.literal("stop")
                        .then(CommandManager.argument("target", EntityArgumentType.players())
                                .executes(this::stop)
                        )
                )
                .then(CommandManager.literal("pause")
                        .then(CommandManager.argument("target", EntityArgumentType.players())
                                .executes(this::pause)
                        )
                )
                .then(CommandManager.literal("volume")
                        .then(CommandManager.argument("target", EntityArgumentType.players())
                                .executes(this::volume)
                                .then(CommandManager.argument("param1", StringArgumentType.string())
                                        .suggests((context, builder) -> suggestParameters(context, builder, "param1", getParameterTypes(true)))
                                        .executes(this::volume)
                                        .then(
                                                CommandManager.argument("param2", StringArgumentType.string())
                                                        .suggests((context, builder) -> suggestParameters(context, builder, "param2", getParameterTypes(true)))
                                                        .executes(this::volume)
                                        )
                                )
                        )
                )
                .then(CommandManager.literal("reload")
                        .executes(this::reload)
                )
        );
    }

    public static JsonObject getDefault() {
        JsonObject json = new JsonObject();
        json.addProperty("url", "");
        json.addProperty("fallback", "");
        json.addProperty("volume", 50);
        json.addProperty("fadeIn", 0);
        json.addProperty("fadeOut", 0);
        json.addProperty("loop", false);
        return json;
    }

    public List<String> getParameterTypes(boolean volume) {
        List<String> parameters = volume ? Arrays.asList("volume=", "time=") : Arrays.asList("volume=", "fadein=", "fadeout=", "loop=", "fallback=");

        //Add quotes to the parameters at the start and end
        List<String> out = new ArrayList<>();
        for (String p : parameters) {
            out.add("\""+p+"\"");
        }

        return out;
    }

    public CompletableFuture<Suggestions> suggest(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder, String id, List<String> suggestions) {
        String typing = "";
        try {
            typing = StringArgumentType.getString(context, id);
            if (typing.isEmpty()) {
                typing = "";
            }
        } catch (Exception ignored) {}

        List<String> suggest = new ArrayList<>(suggestions);

        StringUtil.copyPartialMatches(typing, suggest, new ArrayList<>()).forEach(builder::suggest);

        return builder.buildFuture();
    }

    public CompletableFuture<Suggestions> suggestParameters(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder, String id, List<String> suggestions) {
        String typing = "";
        try {
            typing = StringArgumentType.getString(context, id);
            if (typing.isEmpty()) {
                typing = "";
            }
        } catch (Exception ignored) {}

        List<String> suggest = new ArrayList<>(suggestions);

        List<String> modifiers = getModifiers(context);
        if (!modifiers.isEmpty()) {
            for (String modifier : modifiers) {
                suggest.removeIf(modifier::startsWith);
            }
        }

        StringUtil.copyPartialMatches(typing, suggest, new ArrayList<>()).forEach(builder::suggest);

        return builder.buildFuture();
    }

    private int list(CommandContext<ServerCommandSource> command) {
        List<String> videos = getVideoList();

        if (videos.isEmpty()) {
            sendMSG(command.getSource(), "No video files found.");
            return Command.SINGLE_SUCCESS;
        }

        sendMSG(command.getSource(), "Video files:");
        for (String a : videos) {
            sendMSG(command.getSource(), a);
        }

        return Command.SINGLE_SUCCESS;
    }

    private int play(CommandContext<ServerCommandSource> context) {
        List<ServerPlayerEntity> players = new ArrayList<>(getPlayers(context));
        if (players.isEmpty()) {
            sendMSG(context.getSource(), "No players found.");
            return Command.SINGLE_SUCCESS;
        }

        String video = StringArgumentType.getString(context, "video");
        String fallback = "";
        int volume = 50;
        int fadeIn = 0;
        int fadeOut = 0;
        boolean loop = false;

        List<String> modifiers = new ArrayList<>(getModifiers(context));

        List<String> parameterTypes = new ArrayList<>();
        for (String modifier : modifiers) {
            modifier = modifier.toLowerCase(Locale.ROOT).replace("\"", "");

            String type = modifier.split("=")[0];
            parameterTypes.add(type);

            String data = modifier.split("=")[1];

            switch (type) {
                case "volume" -> {
                    try {
                        volume = Integer.parseInt(data);
                    } catch (NumberFormatException e) {
                        sendMSG(context.getSource(), "Error with volume parameter.");
                        return Command.SINGLE_SUCCESS;
                    }
                }
                case "fadein" -> {
                    try {
                        fadeIn = Integer.parseInt(data);
                    } catch (NumberFormatException e) {
                        sendMSG(context.getSource(), "Error with fadeIn parameter.");
                        return Command.SINGLE_SUCCESS;
                    }
                }
                case "fadeout" -> {
                    try {
                        fadeOut = Integer.parseInt(data);
                    } catch (NumberFormatException e) {
                        sendMSG(context.getSource(), "Error with fadeOut parameter.");
                        return Command.SINGLE_SUCCESS;
                    }
                }
                case "loop" -> {
                    try {
                        loop = Boolean.parseBoolean(data);
                    } catch (NumberFormatException e) {
                        sendMSG(context.getSource(), "Error with loop parameter.");
                        return Command.SINGLE_SUCCESS;
                    }
                }
            }
        }

        if (getVideoList().contains(video)) {
            if (!parameterTypes.contains("volume")) volume = getVideos().get(video+".volume") != null ? getVideos().getInt(video+".volume") : volume;
            if (!parameterTypes.contains("fadein")) fadeIn = getVideos().get(video+".fadeIn") != null ? getVideos().getInt(video+".fadeIn") : fadeIn;
            if (!parameterTypes.contains("fadeout")) fadeOut = getVideos().get(video+".fadeOut") != null ? getVideos().getInt(video+".fadeOut") : fadeOut;
            if (!parameterTypes.contains("loop")) loop = getVideos().get(video+".loop") != null ? getVideos().getBoolean(video+".loop") : loop;
            if (!parameterTypes.contains("fallback")) fallback = getVideos().get(video+".fallback") != null ? getVideos().getString(video+".fallback") : fallback;
            video = getVideos().getString(video+".url");
        }

        JsonObject json = new JsonObject();
        json.addProperty("url", video);
        json.addProperty("fallback", fallback);
        json.addProperty("volume", volume);
        json.addProperty("fadeIn", fadeIn);
        json.addProperty("fadeOut", fadeOut);
        json.addProperty("loop", loop);
        players.forEach(p -> {
            PacketHandler.sendPlayVideo(p, json);
        });

        String player = players.size() == 1 ? players.get(0).getName().getString() : "múltiples jugadores";
        String parameters = modifiers.isEmpty() ? "" : " con los parámetros: "+String.join(", ", modifiers);
        sendMSG(context.getSource(), true, "Reproduciendo video "+video+" para "+player+parameters+".");
        return Command.SINGLE_SUCCESS;
    }

    private int stop(CommandContext<ServerCommandSource> context) {
        List<ServerPlayerEntity> players = new ArrayList<>(getPlayers(context));
        if (players.isEmpty()) {
            sendMSG(context.getSource(), "No players found.");
            return Command.SINGLE_SUCCESS;
        }

        players.forEach(PacketHandler::sendStopVideo);

        String player = players.size() == 1 ? players.get(0).getName().getString() : "múltiples jugadores";
        sendMSG(context.getSource(),true, "Deteniendo video para "+player+".");
        return Command.SINGLE_SUCCESS;
    }

    private int pause(CommandContext<ServerCommandSource> context) {
        List<ServerPlayerEntity> players = new ArrayList<>(getPlayers(context));
        if (players.isEmpty()) {
            sendMSG(context.getSource(), "No players found.");
            return Command.SINGLE_SUCCESS;
        }

        players.forEach(PacketHandler::sendPauseVideo);

        String player = players.size() == 1 ? players.get(0).getName().getString() : "múltiples jugadores";
        sendMSG(context.getSource(),true, "Pausando video para "+player+".");
        return Command.SINGLE_SUCCESS;
    }

    private int volume(CommandContext<ServerCommandSource> context) {
        List<ServerPlayerEntity> players = new ArrayList<>(getPlayers(context));
        if (players.isEmpty()) {
            sendMSG(context.getSource(), "No players found.");
            return Command.SINGLE_SUCCESS;
        }

        int volume = 0;
        int fadeTime = 0;

        List<String> modifiers = new ArrayList<>(getModifiers(context));

        if (modifiers.isEmpty()) {
            sendMSG(context.getSource(), "No parameters found. Use /video volume <player> volume=<volume> time=<time>.");
            return Command.SINGLE_SUCCESS;
        }

        for (String modifier : modifiers) {
            modifier = modifier.toLowerCase(Locale.ROOT);

            String type = modifier.split("=")[0];
            String data = modifier.split("=")[1];

            switch (type) {
                case "volume" -> {
                    try {
                        volume = Integer.parseInt(data);
                    } catch (NumberFormatException e) {
                        sendMSG(context.getSource(), "Error with volume parameter.");
                        return Command.SINGLE_SUCCESS;
                    }
                }
                case "time" -> {
                    try {
                        fadeTime = Integer.parseInt(data);
                    } catch (NumberFormatException e) {
                        sendMSG(context.getSource(), "Error with time parameter.");
                        return Command.SINGLE_SUCCESS;
                    }
                }
            }
        }

        int finalVolume = volume;
        int finalFadeTime = fadeTime;

        players.forEach(p -> {
            PacketHandler.sendVideoVolume(p, finalVolume, finalFadeTime);
        });

        String player = players.size() == 1 ? players.get(0).getName().getString() : "múltiples jugadores";
        String parameters = modifiers.isEmpty() ? "" : " con los parámetros: "+String.join(", ", modifiers);
        sendMSG(context.getSource(), true, "Cambiando volumen a "+volume+" para "+player+parameters+".");
        return Command.SINGLE_SUCCESS;
    }

    private int reload(CommandContext<ServerCommandSource> context) {
        Main.INSTANCE.reloadConfig();
        sendMSG(context.getSource(), "Configuración recargada.");
        return Command.SINGLE_SUCCESS;
    }

    private List<String> getModifiers(CommandContext<ServerCommandSource> context) {
        String param1 = "";
        String param2 = "";
        String param3 = "";
        String param4 = "";

        try {
            param1 = StringArgumentType.getString(context, "param1");
        } catch (Exception ignored) {}
        try {
            param2 = StringArgumentType.getString(context, "param2");
        } catch (Exception ignored) {}
        try {
            param3 = StringArgumentType.getString(context, "param3");
        } catch (Exception ignored) {}
        try {
            param4 = StringArgumentType.getString(context, "param4");
        } catch (Exception ignored) {}

        List<String> out = new ArrayList<>();
        if (!param1.isEmpty()) out.add(param1);
        if (!param2.isEmpty()) out.add(param2);
        if (!param3.isEmpty()) out.add(param3);
        if (!param4.isEmpty()) out.add(param4);

        return out;
    }

    private Collection<ServerPlayerEntity> getPlayers(CommandContext<ServerCommandSource> context) {
        try {
            return EntityArgumentType.getPlayers(context, "target");
        } catch (CommandSyntaxException e) {
            sendMSG(context.getSource(), "Error with target parameter.");
            return new ArrayList<>();
        }
    }

    public static List<String> getVideoList() {
        return new ArrayList<>(getVideos().getKeys());
    }

    public static Configuration getVideos() {
        return Main.INSTANCE.getConfig().getSection("videos");
    }

}