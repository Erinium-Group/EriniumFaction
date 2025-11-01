package fr.eriniumgroup.erinium_faction.commands;

import com.mojang.brigadier.CommandDispatcher;
import fr.eriniumgroup.erinium_faction.features.topluck.TopLuckSavedData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;

public class TopLuckConfigCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("topluckconfig")
            .requires(src -> src.hasPermission(2))
            .then(Commands.literal("export")
                .executes(ctx -> {
                    var src = ctx.getSource();
                    var server = src.getServer();
                    var data = TopLuckSavedData.get(server);
                    Path dir = server.getServerDirectory().resolve("config");
                    data.exportTo(dir);
                    src.sendSuccess(() -> Component.literal("TopLuck config exportée dans " + dir.resolve("topluck.json")), true);
                    return 1;
                })
            )
            .then(Commands.literal("reload")
                .executes(ctx -> {
                    var src = ctx.getSource();
                    var server = src.getServer();
                    var data = TopLuckSavedData.get(server);
                    Path path = server.getServerDirectory().resolve("config").resolve("topluck.json");
                    data.importFrom(path);
                    src.sendSuccess(() -> Component.literal("TopLuck config rechargée depuis " + path), true);
                    return 1;
                })
            )
        );
    }
}
