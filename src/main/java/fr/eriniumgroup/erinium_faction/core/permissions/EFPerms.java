package fr.eriniumgroup.erinium_faction.core.permissions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.rank.EFRManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.NbtIo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * EFPerms – Façade centrale de permissions.
 *
 * - has(player, node): s’appuie sur EFRManager (ranks serveurs) + OP bypass
 * - guardDispatcher: applique automatiquement un requires(server.command.<root>)
 *   à chaque commande littérale enregistrée dans le dispatcher.
 * - writeCommandsSnapshot: exporte la liste des commandes disponibles pour aide à la config (commands.dat).
 * - Helpers pour nodes génériques joueur: player.place / player.break / player.interact.
 */
public class EFPerms {

    // ---------------------- API de base ----------------------

    public static boolean has(ServerPlayer player, String node) {
        if (player == null || node == null || node.isBlank()) return false;
        // OP bypass (niveau 2 par cohérence vanilla)
        if (player.hasPermissions(2)) return true;
        return EFRManager.get().hasPermission(player, node);
    }

    public static boolean hasOrError(ServerPlayer player, String node) {
        boolean ok = has(player, node);
        if (!ok && player != null) player.sendSystemMessage(Component.translatable("erinium_faction.common.no_permission"));
        return ok;
    }

    // ---------------------- Commandes: garde globale ----------------------

    /**
     * Ajoute/renforce la predicate requires des commandes au niveau racine:
     * server.command.<root> pour chaque LiteralCommandNode.
     * Conserve la predicate existante (AND logique).
     */
    public static void guardDispatcher(CommandDispatcher<CommandSourceStack> dispatcher) {
        try {
            Set<String> names = new HashSet<>();
            for (CommandNode<CommandSourceStack> node : dispatcher.getRoot().getChildren()) {
                if (node instanceof LiteralCommandNode<CommandSourceStack> lit) {
                    String name = lit.getName();
                    names.add(name);
                    Predicate<CommandSourceStack> oldReq = lit.getRequirement();
                    Predicate<CommandSourceStack> extra = (src) -> {
                        ServerPlayer sp;
                        try { sp = src.getPlayer(); } catch (Exception e) { sp = null; }
                        if (sp == null) return true; // console ou commande non-joueur
                        return has(sp, "server.command." + name);
                    };
                    Predicate<CommandSourceStack> combined = oldReq.and(extra);
                    trySetRequirement(node, combined);
                }
            }
            // Snapshot pour aide admin
            writeCommandsSnapshot(names);
        } catch (Exception e) {
            EFC.log.warn("Perms", "Impossible d'installer la garde des commandes: {}", e.toString());
        }
    }

    /**
     * Réécrit la predicate requirement d’un nœud Brigadier via réflexion.
     */
    @SuppressWarnings("rawtypes")
    private static void trySetRequirement(CommandNode node, Predicate requirement) {
        try {
            Field f = CommandNode.class.getDeclaredField("requirement");
            f.setAccessible(true);
            f.set(node, requirement);
        } catch (Throwable t) {
            EFC.log.warn("Perms", "Echec set requirement via reflection: {}", t.toString());
        }
    }

    /**
     * Ecrit la liste des commandes vues dans un fichier NBT (commands.dat) pour référence.
     */
    private static void writeCommandsSnapshot(Set<String> names) {
        try {
            Path dir = EFRManager.getDataDir();
            Files.createDirectories(dir);
            Path out = dir.resolve("commands.dat");
            CompoundTag root = new CompoundTag();
            ListTag list = new ListTag();
            for (String n : names) list.add(StringTag.valueOf(n));
            root.put("commands", list);
            try (OutputStream os = Files.newOutputStream(out)) {
                NbtIo.writeCompressed(root, os);
            }
        } catch (Exception e) {
            EFC.log.warn("Perms", "Impossible d'écrire commands.dat: {}", e.toString());
        }
    }

    // ---------------------- Helpers actions joueur ----------------------

    public static boolean canPlace(ServerPlayer player) { return has(player, "player.place"); }
    public static boolean canBreak(ServerPlayer player) { return has(player, "player.break"); }
    public static boolean canInteract(ServerPlayer player) { return has(player, "player.interact"); }
}