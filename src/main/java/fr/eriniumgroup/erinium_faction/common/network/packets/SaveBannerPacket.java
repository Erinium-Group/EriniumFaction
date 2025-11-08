package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.EriniumFaction;
import fr.eriniumgroup.erinium_faction.common.network.codecs.IntArrayCodec;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.faction.FactionSavedData;
import fr.eriniumgroup.erinium_faction.features.banner.BannerManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.awt.image.BufferedImage;

/**
 * Paquet pour sauvegarder une bannière du client vers le serveur
 */
public record SaveBannerPacket(
    int[] pixels // 32x64 = 2048 pixels en format ARGB
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SaveBannerPacket> TYPE = new CustomPacketPayload.Type<>(
        ResourceLocation.fromNamespaceAndPath(EriniumFaction.MODID, "save_banner")
    );

    public static final StreamCodec<ByteBuf, SaveBannerPacket> STREAM_CODEC = StreamCodec.composite(
        IntArrayCodec.INT_ARRAY,
        SaveBannerPacket::pixels,
        SaveBannerPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handler côté serveur
     */
    public static void handleData(SaveBannerPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;

        context.enqueueWork(() -> {
            // Vérifier que le joueur est dans une faction
            String factionId = FactionManager.getPlayerFaction(player.getUUID());
            if (factionId == null) {
                player.sendSystemMessage(Component.translatable("erinium_faction.command.banner.no_faction"));
                return;
            }

            Faction faction = FactionManager.getFaction(factionId);
            if (faction == null) {
                player.sendSystemMessage(Component.translatable("erinium_faction.command.banner.faction_not_found"));
                return;
            }

            // Vérifier les permissions
            if (!faction.hasPermission(player.getUUID(), "faction.banner.edit")) {
                player.sendSystemMessage(Component.translatable("erinium_faction.command.banner.no_permission_edit"));
                return;
            }

            // Vérifier que la faction a acheté la fonctionnalité
            if (!faction.hasCustomBanner()) {
                player.sendSystemMessage(Component.translatable("erinium_faction.command.banner.not_purchased"));
                return;
            }

            // Valider les données
            if (packet.pixels == null || packet.pixels.length != 2048) {
                player.sendSystemMessage(Component.translatable("erinium_faction.command.banner.invalid_data"));
                return;
            }

            // Convertir en BufferedImage
            BufferedImage image = new BufferedImage(32, 64, BufferedImage.TYPE_INT_ARGB);
            image.setRGB(0, 0, 32, 64, packet.pixels, 0, 32);

            // Sauvegarder
            if (BannerManager.saveBanner(faction.getId(), image)) {
                player.sendSystemMessage(Component.translatable("erinium_faction.command.banner.saved"));
                FactionSavedData.get(player.server).setDirty();

                // SYNCHRONISATION INTELLIGENTE: Envoyer la nouvelle texture à tous les joueurs en ligne
                // Cela permet de mettre à jour les capes, items et bannières en temps réel
                SyncBannerTexturePacket syncPacket = new SyncBannerTexturePacket(faction.getId(), packet.pixels);

                // Envoyer à tous les joueurs du serveur
                for (var serverPlayer : player.server.getPlayerList().getPlayers()) {
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer, syncPacket);
                }
            } else {
                player.sendSystemMessage(Component.translatable("erinium_faction.command.banner.save_error"));
            }
        });
    }
}
