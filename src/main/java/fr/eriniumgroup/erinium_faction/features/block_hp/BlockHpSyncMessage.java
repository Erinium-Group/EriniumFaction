package fr.eriniumgroup.erinium_faction.features.block_hp;

import fr.eriniumgroup.erinium_faction.client.renderer.BlockHpRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BlockHpSyncMessage(BlockPos pos, int current, int base) implements CustomPacketPayload {

    // Type du paquet (serveur -> client)
    public static final Type<BlockHpSyncMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("erinium_faction", "block_hp_sync"));

    // Codec Stream (RegistryFriendlyByteBuf)
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockHpSyncMessage> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, BlockHpSyncMessage::pos, StreamCodec.of((buf, val) -> buf.writeInt(val), buf -> buf.readInt()), BlockHpSyncMessage::current, StreamCodec.of((buf, val) -> buf.writeInt(val), buf -> buf.readInt()), BlockHpSyncMessage::base, BlockHpSyncMessage::new);

    @Override
    public Type<BlockHpSyncMessage> type() {
        return TYPE;
    }

    /**
     * Handler client: met à jour l'overlay HP pour le bloc.
     */
    public static void handleData(final BlockHpSyncMessage message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            BlockHpRenderer.updateBlockHp(message.pos, message.current, message.base);
        });
    }

    // Enregistrement déplacé dans PacketHandler.onRegisterPayloadHandlers(...)
}
