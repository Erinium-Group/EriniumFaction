package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Serveur -> Client: synchro de l'état des paramètres de faction pour l'écran Settings.
 */
public record FactionSettingsStateMessage(boolean isOpen, boolean isPublicMode, boolean isSafezone) implements CustomPacketPayload {
    public static final Type<FactionSettingsStateMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "faction_settings_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FactionSettingsStateMessage> STREAM_CODEC = StreamCodec.of((buf, msg) -> {
        buf.writeBoolean(msg.isOpen);
        buf.writeBoolean(msg.isPublicMode);
        buf.writeBoolean(msg.isSafezone);
    }, (buf) -> new FactionSettingsStateMessage(buf.readBoolean(), buf.readBoolean(), buf.readBoolean()));

    @Override
    public Type<FactionSettingsStateMessage> type() { return TYPE; }

    public static void handleData(final FactionSettingsStateMessage message, final IPayloadContext ctx) {
        if (ctx.flow() == PacketFlow.CLIENTBOUND) {
            ctx.enqueueWork(() -> fr.eriniumgroup.erinium_faction.gui.screens.FactionMenuSettingsScreen.onSettingsState(message));
        }
    }
}

