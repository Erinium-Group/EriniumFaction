package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.claim.ClaimKey;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Paquet pour claim/unclaim un chunk depuis la map
 */
public record ChunkClaimPacket(String dimension, int chunkX, int chunkZ, boolean isClaim) implements CustomPacketPayload {
    public static final Type<ChunkClaimPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "chunk_claim"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChunkClaimPacket> STREAM_CODEC = StreamCodec.of(
        (buf, msg) -> {
            buf.writeUtf(msg.dimension);
            buf.writeInt(msg.chunkX);
            buf.writeInt(msg.chunkZ);
            buf.writeBoolean(msg.isClaim);
        },
        (buf) -> {
            String dim = buf.readUtf();
            int cx = buf.readInt();
            int cz = buf.readInt();
            boolean claim = buf.readBoolean();
            return new ChunkClaimPacket(dim, cx, cz, claim);
        }
    );

    @Override
    public Type<ChunkClaimPacket> type() {
        return TYPE;
    }

    public static void handleData(final ChunkClaimPacket message, final IPayloadContext ctx) {
        if (ctx.flow() == PacketFlow.SERVERBOUND) {
            ctx.enqueueWork(() -> {
                ServerPlayer sp = (ServerPlayer) ctx.player();
                if (sp == null) return;

                Faction f = FactionManager.getFactionOf(sp.getUUID());
                if (f == null) {
                    sp.sendSystemMessage(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                    return;
                }

                ResourceKey<Level> dimKey = ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION,
                    ResourceLocation.parse(message.dimension)
                );

                ClaimKey key = ClaimKey.of(dimKey, message.chunkX, message.chunkZ);

                if (message.isClaim) {
                    // Claim - Vérifier la permission CLAIM_TERRITORY
                    if (!f.hasPermission(sp.getUUID(), fr.eriniumgroup.erinium_faction.core.faction.Permission.CLAIM_TERRITORY.getServerKey())) {
                        sp.sendSystemMessage(Component.translatable("erinium_faction.cmd.faction.no_permission"));
                        return;
                    }
                    boolean ok = FactionManager.tryClaim(key, f.getId());
                    if (!ok) {
                        sp.sendSystemMessage(Component.translatable("erinium_faction.cmd.faction.claim.limit"));
                    } else {
                        sp.sendSystemMessage(Component.translatable("erinium_faction.cmd.faction.claim.success"));
                    }
                } else {
                    // Unclaim - Vérifier la permission UNCLAIM_TERRITORY
                    if (!f.hasPermission(sp.getUUID(), fr.eriniumgroup.erinium_faction.core.faction.Permission.UNCLAIM_TERRITORY.getServerKey())) {
                        sp.sendSystemMessage(Component.translatable("erinium_faction.cmd.faction.no_permission"));
                        return;
                    }
                    String owner = FactionManager.getClaimOwner(key);
                    if (owner == null) {
                        sp.sendSystemMessage(Component.translatable("erinium_faction.cmd.faction.unclaim.not_claimed"));
                        return;
                    }
                    if (!owner.equalsIgnoreCase(f.getId())) {
                        sp.sendSystemMessage(Component.translatable("erinium_faction.cmd.faction.unclaim.not_owner"));
                        return;
                    }
                    boolean ok = FactionManager.tryUnclaim(key, f.getId());
                    if (!ok) {
                        sp.sendSystemMessage(Component.translatable("erinium_faction.cmd.faction.unclaim.fail"));
                    } else {
                        sp.sendSystemMessage(Component.translatable("erinium_faction.cmd.faction.unclaim.success"));
                    }
                }
            });
        }
    }
}
