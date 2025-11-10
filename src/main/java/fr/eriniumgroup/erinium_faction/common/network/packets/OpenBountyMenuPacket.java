package fr.eriniumgroup.erinium_faction.common.network.packets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Packet pour ouvrir le menu principal du bounty
 */
public record OpenBountyMenuPacket() implements CustomPacketPayload {
    public static final Type<OpenBountyMenuPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("erinium_faction", "open_bounty_menu"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenBountyMenuPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> {},
            buf -> new OpenBountyMenuPacket()
    );

    public static void handleData(final OpenBountyMenuPacket msg, final IPayloadContext ctx) {
        if (ctx.flow() != PacketFlow.CLIENTBOUND) return;
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ctx.enqueueWork(() -> ClientHandler.openBountyMenu());
        }
    }

    private static class ClientHandler {
        static void openBountyMenu() {
            net.minecraft.client.Minecraft.getInstance().setScreen(
                    new fr.eriniumgroup.erinium_faction.client.gui.bounty.BountyMainMenuScreen()
            );
        }
    }
}
