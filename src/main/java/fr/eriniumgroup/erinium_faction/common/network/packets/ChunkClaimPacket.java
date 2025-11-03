package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.claim.ClaimKey;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Packet client → server pour claim/unclaim des chunks via la minimap
 */
public record ChunkClaimPacket(
    Action action,
    String dimension,
    List<ChunkPosition> chunks
) implements CustomPacketPayload {

    public static final Type<ChunkClaimPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "chunk_claim"));

    public static final StreamCodec<ByteBuf, ChunkClaimPacket> STREAM_CODEC = StreamCodec.composite(
        Action.STREAM_CODEC,
        ChunkClaimPacket::action,
        ByteBufCodecs.STRING_UTF8,
        ChunkClaimPacket::dimension,
        ChunkPosition.STREAM_CODEC.apply(ByteBufCodecs.list()),
        ChunkClaimPacket::chunks,
        ChunkClaimPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ChunkClaimPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sp) {
                handleServerSide(packet, sp);
            }
        });
    }

    private static void handleServerSide(ChunkClaimPacket packet, ServerPlayer sp) {
        Faction faction = FactionManager.getFactionOf(sp.getUUID());

        if (faction == null) {
            sp.sendSystemMessage(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (ChunkPosition chunkPos : packet.chunks) {
            ClaimKey key = ClaimKey.of(
                sp.level().dimension(),
                chunkPos.x(),
                chunkPos.z()
            );

            boolean success = false;

            if (packet.action == Action.CLAIM) {
                success = FactionManager.tryClaim(key, faction.getId());
            } else if (packet.action == Action.UNCLAIM) {
                String owner = FactionManager.getClaimOwner(key);

                // Vérifier que le chunk appartient à la faction
                if (owner != null && owner.equalsIgnoreCase(faction.getId())) {
                    success = FactionManager.tryUnclaim(key, faction.getId());
                }
            }

            if (success) {
                successCount++;
            } else {
                failCount++;
            }
        }

        // Envoyer le résultat au joueur
        if (successCount > 0) {
            if (packet.action == Action.CLAIM) {
                sp.sendSystemMessage(Component.translatable("erinium_faction.minimap.claim.success", successCount));
            } else {
                sp.sendSystemMessage(Component.translatable("erinium_faction.minimap.unclaim.success", successCount));
            }
        }

        if (failCount > 0) {
            if (packet.action == Action.CLAIM) {
                sp.sendSystemMessage(Component.translatable("erinium_faction.minimap.claim.fail", failCount));
            } else {
                sp.sendSystemMessage(Component.translatable("erinium_faction.minimap.unclaim.fail", failCount));
            }
        }

        // IMPORTANT: Envoyer immédiatement les claims mis à jour au client
        int centerCx = sp.blockPosition().getX() >> 4;
        int centerCz = sp.blockPosition().getZ() >> 4;
        int radius = 10; // Radius de 10 chunks

        // Récupérer tous les claims dans le rayon
        List<java.util.Map.Entry<ClaimKey, String>> claims = new ArrayList<>();
        for (int cx = centerCx - radius; cx <= centerCx + radius; cx++) {
            for (int cz = centerCz - radius; cz <= centerCz + radius; cz++) {
                ClaimKey key = ClaimKey.of(sp.level().dimension(), cx, cz);
                String owner = FactionManager.getClaimOwner(key);
                // N'ajouter que les vrais claims (pas wilderness, pas vide)
                if (owner != null && !owner.isEmpty() && !owner.equalsIgnoreCase("wilderness")) {
                    claims.add(new java.util.AbstractMap.SimpleEntry<>(key, owner));
                }
            }
        }

        // Envoyer au client
        ClaimsMapDataMessage.sendTo(sp, packet.dimension, centerCx, centerCz, radius, claims);
    }

    public enum Action implements net.minecraft.util.StringRepresentable {
        CLAIM("claim"),
        UNCLAIM("unclaim");

        private final String name;

        Action(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        public static final StreamCodec<ByteBuf, Action> STREAM_CODEC =
            ByteBufCodecs.fromCodec(net.minecraft.util.StringRepresentable.fromEnum(Action::values));
    }

    public record ChunkPosition(int x, int z) {
        public static final StreamCodec<ByteBuf, ChunkPosition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ChunkPosition::x,
            ByteBufCodecs.VAR_INT,
            ChunkPosition::z,
            ChunkPosition::new
        );
    }
}
