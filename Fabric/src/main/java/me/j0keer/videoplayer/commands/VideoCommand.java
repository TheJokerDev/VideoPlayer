package me.j0keer.videoplayer.commands;

import me.j0keer.videoplayer.VideoPlayer;
import me.j0keer.videoplayer.commands.suggest.VideoSuggest;
import me.j0keer.videoplayer.commands.suggest.VolumeSuggest;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record VideoCommand(VideoPlayer mod) {

    private int start(CommandContext<ServerCommandSource> command) {
        Collection<ServerPlayerEntity> players;
        try {
            players = EntityArgumentType.getPlayers(command, "target");
        } catch (CommandSyntaxException e) {
            command.getSource().sendError(Text.of("Error with target parameter."));
            return 0;
        }

        String video = StringArgumentType.getString(command, "archive");

        int volume = IntegerArgumentType.getInteger(command, "volume");

        if (video == null) {
            command.getSource().sendError(Text.of("Error with file not exist"));
            return 0;
        }

        if (!VideoPlayer.INSTANCE.getConfig().getKeys().isEmpty()) {
            List<String> urls = new ArrayList<>(VideoPlayer.INSTANCE.getConfig().getKeys());
            if (urls.contains(video)) {
                video = VideoPlayer.INSTANCE.getConfig().getString(video+".url");
                volume = VideoPlayer.INSTANCE.getConfig().getInt(video+".volume", volume);
            }
        }

        for (ServerPlayerEntity player : players) {
            mod().getPacketHandler().sendPlay(player, video, volume);
        }

        return Command.SINGLE_SUCCESS;
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("video")
                .requires((command) -> command.hasPermissionLevel(2))
                .then(CommandManager.literal("play")
                        .then(CommandManager.argument("target", EntityArgumentType.players())
                                .then(CommandManager.argument("volume", IntegerArgumentType.integer())
                                        .suggests(new VolumeSuggest())
                                        .then(CommandManager.argument("archive", StringArgumentType.greedyString())
                                                .suggests(new VideoSuggest())
                                                .executes(this::start))
                                )
                        )
                )
                .then(CommandManager.literal("stop")
                        .then(CommandManager.argument("target", EntityArgumentType.players())
                                .executes(this::stop)
                        )
                )
                .then(CommandManager.literal("volume")
                        .then(CommandManager.argument("target", EntityArgumentType.players())
                                .then(CommandManager.argument("volume", IntegerArgumentType.integer())
                                        .suggests(new VolumeSuggest())
                                        .executes(this::volume)
                                )
                        )
                )
                .then(CommandManager.literal("reload")
                        .executes((command) -> {
                                    VideoPlayer.INSTANCE.reloadConfig();
                                    command.getSource().sendMessage(Text.of("Reloaded config"));
                                    return Command.SINGLE_SUCCESS;
                                }
                        )
                )
        );
    }

    private int stop(CommandContext<ServerCommandSource> command) {

        Collection<ServerPlayerEntity> players;
        try {
            players = EntityArgumentType.getPlayers(command, "target");
        } catch (CommandSyntaxException e) {
            command.getSource().sendError(Text.of("Error with target parameter."));
            return Command.SINGLE_SUCCESS;
        }

        for (ServerPlayerEntity player : players) {
            mod().getPacketHandler().sendStop(player);
        }

        return Command.SINGLE_SUCCESS;
    }

    private int volume(CommandContext<ServerCommandSource> command) {

        Collection<ServerPlayerEntity> players;
        try {
            players = EntityArgumentType.getPlayers(command, "target");
        } catch (CommandSyntaxException e) {
            command.getSource().sendError(Text.of("Error with target parameter."));
            return Command.SINGLE_SUCCESS;
        }

        int volume = IntegerArgumentType.getInteger(command, "volume");

        for (ServerPlayerEntity player : players) {
            mod().getPacketHandler().sendVolume(player, volume);
        }

        return Command.SINGLE_SUCCESS;
    }
}
