package me.j0keer.videoplayer.commands.suggest;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import me.j0keer.videoplayer.VideoPlayer;

import java.util.concurrent.CompletableFuture;

public class VideoSuggest implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        if (!VideoPlayer.INSTANCE.getConfig().getKeys().isEmpty()) {
            for (String s : VideoPlayer.INSTANCE.getConfig().getKeys()) {
                builder.suggest(s);
            }
        }
        return builder.buildFuture();
    }
}
