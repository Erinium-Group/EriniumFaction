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
import java.util.List;

/**
 * Client -> Serveur: demande la liste de toutes les factions
 */
public record FactionListRequestMessage() implements CustomPacketPayload {
    public static final Type<FactionListRequestMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "faction_list_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FactionListRequestMessage> STREAM_CODEC = StreamCodec.of(
        (buf, msg) -> {
            // Pas de données à envoyer
        },
        (buf) -> new FactionListRequestMessage()
    );

    @Override
    public Type<FactionListRequestMessage> type() { return TYPE; }

    public static void handleData(final FactionListRequestMessage message, final IPayloadContext ctx) {
        if (ctx.flow() == PacketFlow.SERVERBOUND) {
            ctx.enqueueWork(() -> handleServer(message, (ServerPlayer) ctx.player())).exceptionally(e -> {
                ctx.connection().disconnect(net.minecraft.network.chat.Component.literal(e.getMessage()));
                return null;
            });
        }
    }

    private static void handleServer(FactionListRequestMessage msg, ServerPlayer sp) {
        if (sp == null) return;

        // Récupérer toutes les factions
        var allFactions = FactionManager.getAllFactions();

        List<String> factionIds = new ArrayList<>();
        List<String> factionNames = new ArrayList<>();
        List<Integer> memberCounts = new ArrayList<>();

        for (Faction faction : allFactions) {
            factionIds.add(faction.getId());
            factionNames.add(faction.getName());
            memberCounts.add(faction.getMembers().size());
        }

        // Envoyer la réponse au client
        FactionListDataMessage.sendTo(sp, factionIds, factionNames, memberCounts);
    }
}
