package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.EriniumFaction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Paquet pour synchroniser les données de nameplate (faction + niveau) d'un joueur
 * Envoyé à tous les clients proches pour afficher sur les nameplates
 */
public record SyncPlayerNameplateDataPacket(UUID playerUUID, String factionName, int level) implements CustomPacketPayload {

    public static final Type<SyncPlayerNameplateDataPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(EriniumFaction.MODID, "sync_player_nameplate_data")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPlayerNameplateDataPacket> STREAM_CODEC = StreamCodec.of(
        (buf, msg) -> {
            buf.writeUUID(msg.playerUUID);
            buf.writeUtf(msg.factionName, 256);
            buf.writeVarInt(msg.level);
        },
        buf -> new SyncPlayerNameplateDataPacket(
            buf.readUUID(),
            buf.readUtf(256),
            buf.readVarInt()
        )
    );

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
