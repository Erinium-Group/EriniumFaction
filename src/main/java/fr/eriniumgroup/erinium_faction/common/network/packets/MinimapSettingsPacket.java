package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.gui.screens.MinimapOverlaySettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet pour ouvrir le GUI de settings de la minimap overlay côté client
 */
public record MinimapSettingsPacket() implements CustomPacketPayload {
    public static final Type<MinimapSettingsPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "minimap_settings"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MinimapSettingsPacket> STREAM_CODEC = StreamCodec.of(
        (buf, msg) -> {},
        (buf) -> new MinimapSettingsPacket()
    );

    @Override
    public Type<MinimapSettingsPacket> type() {
        return TYPE;
    }

    public static void handleData(final MinimapSettingsPacket message, final IPayloadContext ctx) {
        if (ctx.flow() == PacketFlow.CLIENTBOUND) {
            ctx.enqueueWork(() -> {
                Minecraft.getInstance().setScreen(new MinimapOverlaySettingsScreen());
            });
        }
    }
}
