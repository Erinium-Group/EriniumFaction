package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet pour activer/désactiver la minimap overlay côté client
 */
public record MinimapTogglePacket(boolean enabled) implements CustomPacketPayload {
    public static final Type<MinimapTogglePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "minimap_toggle"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MinimapTogglePacket> STREAM_CODEC = StreamCodec.of(
        (buf, msg) -> buf.writeBoolean(msg.enabled),
        (buf) -> new MinimapTogglePacket(buf.readBoolean())
    );

    @Override
    public Type<MinimapTogglePacket> type() {
        return TYPE;
    }

    public static void handleData(final MinimapTogglePacket message, final IPayloadContext ctx) {
        if (ctx.flow() != PacketFlow.CLIENTBOUND) {
            return;
        }

        // Vérifier qu'on est côté client
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }

        ctx.enqueueWork(() -> {
            // Déléguer à une classe client-only
            ClientMinimapScreenHandler.toggleMinimap(message.enabled);
        });
    }
}
