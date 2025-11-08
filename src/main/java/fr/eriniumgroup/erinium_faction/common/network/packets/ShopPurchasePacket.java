package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.EriniumFaction;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.faction.FactionSavedData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Paquet pour acheter des items dans l'AdminShop
 * Gère les achats uniques (comme la bannière) et les consommables
 */
public record ShopPurchasePacket(
    String itemId // ID unique de l'item à acheter (ex: "custom_banner", "item_diamond_64", etc.)
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ShopPurchasePacket> TYPE = new CustomPacketPayload.Type<>(
        ResourceLocation.fromNamespaceAndPath(EriniumFaction.MODID, "shop_purchase")
    );

    public static final StreamCodec<ByteBuf, ShopPurchasePacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        ShopPurchasePacket::itemId,
        ShopPurchasePacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handler côté serveur
     */
    public static void handleData(ShopPurchasePacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;

        context.enqueueWork(() -> {
            // Vérifier que le joueur est dans une faction
            String factionId = FactionManager.getPlayerFaction(player.getUUID());
            if (factionId == null) {
                player.sendSystemMessage(Component.translatable("erinium_faction.command.shop.no_faction"));
                return;
            }

            Faction faction = FactionManager.getFaction(factionId);
            if (faction == null) {
                player.sendSystemMessage(Component.translatable("erinium_faction.command.shop.faction_not_found"));
                return;
            }

            // Vérifier les permissions d'achat
            if (!faction.hasPermission(player.getUUID(), "faction.manage.shop")) {
                player.sendSystemMessage(Component.translatable("erinium_faction.command.shop.no_permission"));
                return;
            }

            // Traiter l'achat selon l'item
            switch (packet.itemId) {
                case "custom_banner" -> purchaseCustomBanner(player, faction);
                // Ajouter d'autres items ici
                default -> {
                    player.sendSystemMessage(Component.translatable("erinium_faction.command.shop.unknown_item", packet.itemId));
                    EFC.log.warn("Unknown shop item purchase attempt: " + packet.itemId);
                }
            }
        });
    }

    /**
     * Achat de la fonctionnalité bannière custom (50 000$)
     */
    private static void purchaseCustomBanner(ServerPlayer player, Faction faction) {
        final int BANNER_PRICE = 50000;

        // Vérifier si déjà acheté
        if (faction.hasCustomBanner()) {
            player.sendSystemMessage(Component.translatable("erinium_faction.command.shop.banner.already_owned"));
            return;
        }

        // Vérifier le solde
        if (faction.getBankBalance() < BANNER_PRICE) {
            player.sendSystemMessage(Component.translatable("erinium_faction.command.shop.insufficient_funds", BANNER_PRICE, faction.getBankBalance()));
            return;
        }

        // Débiter
        faction.setBankBalance(faction.getBankBalance() - BANNER_PRICE);

        // Activer la fonctionnalité
        faction.setHasCustomBanner(true);

        // Sauvegarder
        FactionSavedData.get(player.server).setDirty();

        // Confirmer l'achat
        player.sendSystemMessage(Component.translatable("erinium_faction.command.shop.banner.purchased"));
        player.sendSystemMessage(Component.translatable("erinium_faction.command.shop.banner.hint"));

        // SYNCHRONISATION: Créer une bannière blanche par défaut et la synchroniser
        java.awt.image.BufferedImage defaultBanner = new java.awt.image.BufferedImage(32, 64, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        // Remplir avec du blanc
        java.awt.Graphics2D g = defaultBanner.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, 32, 64);
        g.dispose();

        // Sauvegarder la bannière par défaut
        fr.eriniumgroup.erinium_faction.features.banner.BannerManager.saveBanner(faction.getId(), defaultBanner);

        // Convertir en pixels pour la synchronisation
        int[] pixels = new int[2048];
        defaultBanner.getRGB(0, 0, 32, 64, pixels, 0, 32);

        // Synchroniser immédiatement avec tous les joueurs en ligne
        SyncBannerTexturePacket syncPacket = new SyncBannerTexturePacket(faction.getId(), pixels);
        for (var serverPlayer : player.server.getPlayerList().getPlayers()) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer, syncPacket);
        }

        // Log
        EFC.log.info("Faction {} purchased custom banner feature for ${} and received default banner", faction.getId(), BANNER_PRICE);
    }
}
