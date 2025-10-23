package fr.eriniumgroup.erinium_faction.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Custom argument type for faction names with autocompletion
 */
public class FactionArgumentType implements ArgumentType<String> {
    private static final SimpleCommandExceptionType FACTION_NOT_FOUND = new SimpleCommandExceptionType(Component.literal("Faction introuvable"));

    public static FactionArgumentType faction() {
        return new FactionArgumentType();
    }

    public static String getFaction(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, String.class);
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String name = reader.readString();
        if (!FactionManager.factionExists(name)) {
            throw FACTION_NOT_FOUND.create();
        }
        return name;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(FactionManager.getAllFactions().stream().map(f -> f.getName()).collect(Collectors.toList()), builder);
    }
}