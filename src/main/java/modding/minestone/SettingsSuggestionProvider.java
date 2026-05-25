package modding.minestone;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class SettingsSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        Collection<String> settingOptions = new ArrayList<String>(Arrays.asList("showConfirmation", "showErrors", "showUserMessages"));

        for (String option : settingOptions) {
            builder.suggest(option);
        }

        return builder.buildFuture();
    }
}
