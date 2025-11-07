package fr.eriniumgroup.erinium_faction.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import fr.eriniumgroup.erinium_faction.common.network.packets.OpenBannerEditorPacket;
import fr.eriniumgroup.erinium_faction.common.util.BannerImageConverter;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.features.banner.BannerManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.awt.image.BufferedImage;

/**
 * Commandes pour gérer les bannières custom des factions
 * /f banner edit - Ouvre l'éditeur de bannière
 * /f banner get - Donne la bannière avec l'image de la faction
 */
public class BannerCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("f")
                .then(Commands.literal("banner")
                    .then(Commands.literal("edit")
                        .executes(BannerCommand::editBanner))
                    .then(Commands.literal("get")
                        .executes(BannerCommand::getBanner))
                )
        );
    }

    /**
     * Commande /f banner edit
     * Ouvre l'éditeur de bannière pour le joueur
     */
    private static int editBanner(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();

            // Vérifier que le joueur est dans une faction
            String factionId = FactionManager.getPlayerFaction(player.getUUID());
            if (factionId == null) {
                player.sendSystemMessage(Component.translatable("erinium_faction.command.banner.no_faction"));
                return 0;
            }

            Faction faction = FactionManager.getFaction(factionId);
            if (faction == null) {
                player.sendSystemMessage(Component.translatable("erinium_faction.command.banner.faction_not_found"));
                return 0;
            }

            // Vérifier les permissions
            if (!faction.hasPermission(player.getUUID(), "faction.banner.edit")) {
                player.sendSystemMessage(Component.translatable("erinium_faction.command.banner.no_permission_edit"));
                return 0;
            }

            // Vérifier que la faction a acheté la fonctionnalité
            if (!faction.hasCustomBanner()) {
                player.sendSystemMessage(Component.translatable("erinium_faction.command.banner.not_purchased"));
                return 0;
            }

            // Charger l'image existante si elle existe
            BufferedImage existingImage = BannerManager.loadBanner(faction.getId());
            int[] pixels = null;

            if (existingImage != null) {
                // Convertir en tableau de pixels
                pixels = new int[2048];
                existingImage.getRGB(0, 0, 32, 64, pixels, 0, 32);
            }

            // Envoyer un paquet au client pour ouvrir l'éditeur
            PacketDistributor.sendToPlayer(player, new OpenBannerEditorPacket(pixels));

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.translatable("erinium_faction.command.error", e.getMessage()));
            return 0;
        }
    }

    /**
     * Commande /f banner get
     * Donne la bannière custom au joueur
     */
    private static int getBanner(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();

            // Vérifier que le joueur est dans une faction
            String factionId = FactionManager.getPlayerFaction(player.getUUID());
            if (factionId == null) {
                player.sendSystemMessage(Component.translatable("erinium_faction.command.banner.no_faction"));
                return 0;
            }

            Faction faction = FactionManager.getFaction(factionId);
            if (faction == null) {
                player.sendSystemMessage(Component.translatable("erinium_faction.command.banner.faction_not_found"));
                return 0;
            }

            // Vérifier les permissions
            if (!faction.hasPermission(player.getUUID(), "faction.banner.get")) {
                player.sendSystemMessage(Component.translatable("erinium_faction.command.banner.no_permission_get"));
                return 0;
            }

            // Vérifier que la faction a acheté la fonctionnalité
            if (!faction.hasCustomBanner()) {
                player.sendSystemMessage(Component.translatable("erinium_faction.command.banner.not_purchased"));
                return 0;
            }

            // Charger l'image
            BufferedImage image = BannerManager.loadBanner(faction.getId());
            if (image == null) {
                player.sendSystemMessage(Component.translatable("erinium_faction.command.banner.not_found"));
                return 0;
            }

            // Convertir en item bannière
            ItemStack banner = BannerImageConverter.createBanner(image, faction.getId());

            // Donner au joueur
            if (!player.getInventory().add(banner)) {
                player.drop(banner, false);
            }

            // Synchroniser la texture au client
            int[] pixels = new int[2048];
            image.getRGB(0, 0, 32, 64, pixels, 0, 32);
            PacketDistributor.sendToPlayer(player, new fr.eriniumgroup.erinium_faction.common.network.packets.SyncBannerTexturePacket(faction.getId(), pixels));

            player.sendSystemMessage(Component.translatable("erinium_faction.command.banner.success"));
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.translatable("erinium_faction.command.error", e.getMessage()));
            return 0;
        }
    }
}
