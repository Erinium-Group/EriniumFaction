package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.init.EFMenus;
import fr.eriniumgroup.erinium_faction.init.EFScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MenuStateUpdateMessage(int elementType, String name, Object elementState) implements CustomPacketPayload {

    // Type du paquet (synchro d’état de widgets)
    public static final Type<MenuStateUpdateMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "guistate_update"));

    // Codec (RegistryFriendlyByteBuf)
    public static final StreamCodec<RegistryFriendlyByteBuf, MenuStateUpdateMessage> STREAM_CODEC = StreamCodec.of(MenuStateUpdateMessage::write, MenuStateUpdateMessage::read);

    // Écriture compacte selon elementType
    public static void write(RegistryFriendlyByteBuf buffer, MenuStateUpdateMessage message) {
        buffer.writeInt(message.elementType);
        buffer.writeUtf(message.name);
        if (message.elementType == 0) {
            buffer.writeUtf((String) message.elementState);
        } else if (message.elementType == 1) {
            buffer.writeBoolean((boolean) message.elementState);
        }
    }

    // Lecture correspondante
    public static MenuStateUpdateMessage read(RegistryFriendlyByteBuf buffer) {
        int elementType = buffer.readInt();
        String name = buffer.readUtf();
        Object elementState = null;
        if (elementType == 0) {
            elementState = buffer.readUtf();
        } else if (elementType == 1) {
            elementState = buffer.readBoolean();
        }
        return new MenuStateUpdateMessage(elementType, name, elementState);
    }

    @Override
    public Type<MenuStateUpdateMessage> type() {
        return TYPE;
    }

    /**
     * Handler unique (clientbound et serverbound).
     * - Met à jour l’état du menu côté serveur
     * - Si côté client, notifie l’écran actif pour refléter l’état
     */
    public static void handleMenuState(final MenuStateUpdateMessage message, final IPayloadContext context) {
        if (message.name.length() > 256 || message.elementState instanceof String string && string.length() > 8192)
            return;
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof EFMenus.MenuAccessor menu) {
                menu.getMenuState().put(message.elementType + ":" + message.name, message.elementState);
                if (context.flow() == PacketFlow.CLIENTBOUND && Minecraft.getInstance().screen instanceof EFScreens.ScreenAccessor accessor) {
                    accessor.updateMenuState(message.elementType, message.name, message.elementState);
                }
            }
        }).exceptionally(e -> {
            context.connection().disconnect(Component.literal(e.getMessage()));
            return null;
        });
    }
}