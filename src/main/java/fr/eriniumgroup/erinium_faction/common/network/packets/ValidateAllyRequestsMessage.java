package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Client -> Serveur: demande la validation des demandes d'alliance
 * Nettoie les demandes de factions qui n'existent plus
 */
public record ValidateAllyRequestsMessage() implements CustomPacketPayload {
    public static final Type<ValidateAllyRequestsMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "validate_ally_requests"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ValidateAllyRequestsMessage> STREAM_CODEC = StreamCodec.of(
        (buf, msg) -> {
            // Pas de données à envoyer
        },
        (buf) -> new ValidateAllyRequestsMessage()
    );

    @Override
    public Type<ValidateAllyRequestsMessage> type() { return TYPE; }

    public static void handleData(final ValidateAllyRequestsMessage message, final IPayloadContext ctx) {
        if (ctx.flow() == PacketFlow.SERVERBOUND) {
            ctx.enqueueWork(() -> handleServer(message, (ServerPlayer) ctx.player())).exceptionally(e -> {
                ctx.connection().disconnect(net.minecraft.network.chat.Component.literal(e.getMessage()));
                return null;
            });
        }
    }

    private static void handleServer(ValidateAllyRequestsMessage msg, ServerPlayer sp) {
        if (sp == null) return;

        // Récupérer la faction du joueur
        Faction playerFaction = FactionManager.getFactionOf(sp.getUUID());
        if (playerFaction == null) return;

        // Valider et nettoyer les demandes
        List<String> validIds = new ArrayList<>();
        List<String> validNames = new ArrayList<>();

        var allyRequests = playerFaction.getAllyRequests();
        Iterator<String> iterator = allyRequests.iterator();
        while (iterator.hasNext()) {
            String requestId = iterator.next();
            Faction requestingFaction = FactionManager.getById(requestId);

            if (requestingFaction == null) {
                // La faction n'existe plus, supprimer la demande
                iterator.remove();
                EFC.log.info("§6Alliance", "§cRemoved invalid alliance request from non-existent faction: {}", requestId);
            } else {
                // La faction existe, ajouter à la liste
                validIds.add(requestingFaction.getId());
                validNames.add(requestingFaction.getName());
            }
        }

        // Marquer comme modifié si on a supprimé des demandes
        if (validIds.size() < allyRequests.size()) {
            FactionManager.save(sp.server);
        }

        // Envoyer la réponse au client
        AllyRequestsDataMessage.sendTo(sp, validIds, validNames);
    }
}
