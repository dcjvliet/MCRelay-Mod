package modding.minestone;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.CompletableFuture;

public class ChannelSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        UserSettings settings = SettingsManager.getUserSettings(player.getUUID());

        String selectedGuild = StringArgumentType.getString(context, "server");

        for (DiscordGuild guild : settings.guilds) {
            if (!guild.name.equals(selectedGuild)) {
                continue;
            }

            for (String channel : guild.channels) {
                builder.suggest("\"" + channel + "\"");
            }
        }

        return builder.buildFuture();
    }
}
