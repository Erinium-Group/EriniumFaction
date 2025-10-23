/*
 * The code of this mod element is always locked.
 *
 * You can register new events in this class too.
 *
 * If you want to make a plain independent class, create it using
 * Project Browser -> New... and make sure to make the class
 * outside fr.eriniumgroup.eriniumfaction as this package is managed by MCreator.
 *
 * If you change workspace package, modid or prefix, you will need
 * to manually adapt this file to these changes or remake it.
 *
 * This class will be added in the mod root package.
 */
package fr.eriniumgroup.erinium_faction.gui.widgets;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.procedures.OpenFactionSettingsProcedure;
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

public record FactionGuiNetwork(int buttonID, int x, int y, int z) implements CustomPacketPayload {

    // Type et codec du paquet (GUI -> serveur)
    public static final Type<FactionGuiNetwork> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "gui_for_construct_buttons"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FactionGuiNetwork> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, FactionGuiNetwork message) -> {
        buffer.writeInt(message.buttonID);
        buffer.writeInt(message.x);
        buffer.writeInt(message.y);
        buffer.writeInt(message.z);
    }, (RegistryFriendlyByteBuf buffer) -> new FactionGuiNetwork(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt()));

    @Override
    public Type<FactionGuiNetwork> type() {
        return TYPE;
    }

    /**
     * Handler principal (côté serveur). Sécurisé et exécuté sur le bon thread.
     */
    public static void handleData(final FactionGuiNetwork message, final IPayloadContext context) {
        if (context.flow() == PacketFlow.SERVERBOUND) {
            context.enqueueWork(() -> handleButtonAction(context.player(), message.buttonID, message.x, message.y, message.z)).exceptionally(e -> {
                context.connection().disconnect(Component.literal(e.getMessage()));
                return null;
            });
        }
    }

    /**
     * Logique serveur appelée par le handler (clic bouton, etc.).
     */
    public static void handleButtonAction(Player entity, int buttonID, int x, int y, int z) {
        Level world = entity.level();
        // Sécurité: ne rien faire si le chunk n’est pas chargé
        if (!world.hasChunkAt(new BlockPos(x, y, z))) return;

        if (buttonID == 0) {

            OpenFactionSettingsProcedure.execute(world, x, y, z, entity);
        }
        // ...ajoutez ici d’autres IDs de boutons si nécessaire...
    }

    // [Supprimé] Ancien enregistrement via FMLCommonSetupEvent:
    // L’enregistrement est désormais centralisé dans PacketHandler.onRegisterPayloadHandlers(...)
}