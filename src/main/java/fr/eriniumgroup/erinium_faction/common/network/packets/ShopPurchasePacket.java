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
                player.sendSystemMessage(Component.literal("§cVous devez être dans une faction pour acheter dans le shop!"));
                return;
            }

            Faction faction = FactionManager.getFaction(factionId);
            if (faction == null) {
                player.sendSystemMessage(Component.literal("§cFaction introuvable!"));
                return;
            }

            // Vérifier les permissions d'achat
            if (!faction.hasPermission(player.getUUID(), "faction.shop.buy")) {
                player.sendSystemMessage(Component.literal("§cVous n'avez pas la permission d'acheter dans le shop!"));
                return;
            }

            // Traiter l'achat selon l'item
            switch (packet.itemId) {
                case "custom_banner" -> purchaseCustomBanner(player, faction);
                // Ajouter d'autres items ici
                default -> {
                    player.sendSystemMessage(Component.literal("§cItem inconnu: " + packet.itemId));
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
            player.sendSystemMessage(Component.literal("§cVotre faction possède déjà la fonctionnalité de bannière custom!"));
            return;
        }

        // Vérifier le solde
        if (faction.getBankBalance() < BANNER_PRICE) {
            player.sendSystemMessage(Component.literal("§cSolde insuffisant! Requis: §e" + BANNER_PRICE + "$ §c(Disponible: §e" + faction.getBankBalance() + "$§c)"));
            return;
        }

        // Débiter
        faction.setBankBalance(faction.getBankBalance() - BANNER_PRICE);

        // Activer la fonctionnalité
        faction.setHasCustomBanner(true);

        // Sauvegarder
        FactionSavedData.get(player.server).setDirty();

        // Confirmer l'achat
        player.sendSystemMessage(Component.literal("§a✓ Fonctionnalité Bannière Custom achetée avec succès!"));
        player.sendSystemMessage(Component.literal("§7Utilisez §e/f banner edit §7pour créer votre bannière"));

        // Log
        EFC.log.info("Faction {} purchased custom banner feature for ${}", faction.getId(), BANNER_PRICE);
    }
}
