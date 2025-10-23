package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.eriniumfaction.procedures.TempCommandProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GuiForConstructButtonMessage(int buttonID, int x, int y, int z) implements CustomPacketPayload {

    public static final Type<GuiForConstructButtonMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "gui_for_construct_buttons"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GuiForConstructButtonMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, GuiForConstructButtonMessage message) -> {
        buffer.writeInt(message.buttonID);
        buffer.writeInt(message.x);
        buffer.writeInt(message.y);
        buffer.writeInt(message.z);
    }, (RegistryFriendlyByteBuf buffer) -> new GuiForConstructButtonMessage(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt()));

    @Override
    public Type<GuiForConstructButtonMessage> type() {
        return TYPE;
    }

    /**
     * Handler principal (côté serveur).
     */
    public static void handleData(final GuiForConstructButtonMessage message, final IPayloadContext context) {
        if (context.flow() == PacketFlow.SERVERBOUND) {
            context.enqueueWork(() -> handleButtonAction(context.player(), message.buttonID, message.x, message.y, message.z)).exceptionally(e -> {
                context.connection().disconnect(Component.literal(e.getMessage()));
                return null;
            });
        }
    }

    /**
     * Logique serveur: actions liées aux boutons GUI.
     */
    public static void handleButtonAction(Player entity, int buttonID, int x, int y, int z) {
        Level world = entity.level();
        // Sécurité: ne pas générer de chunk arbitraire
        if (!world.hasChunkAt(new BlockPos(x, y, z))) return;

        if (buttonID == 0) {

            TempCommandProcedure.execute(world, x, y, z, entity);
        }
    }
}