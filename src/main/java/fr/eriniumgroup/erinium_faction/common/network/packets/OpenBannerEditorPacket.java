package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.EriniumFaction;
import fr.eriniumgroup.erinium_faction.common.network.codecs.IntArrayCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Paquet pour ouvrir l'éditeur de bannière côté client
 * Peut contenir des pixels existants (si la faction a déjà une bannière)
 */
public record OpenBannerEditorPacket(
    int[] pixels // Peut être null ou vide si pas de bannière existante
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenBannerEditorPacket> TYPE = new CustomPacketPayload.Type<>(
        ResourceLocation.fromNamespaceAndPath(EriniumFaction.MODID, "open_banner_editor")
    );

    public static final StreamCodec<ByteBuf, OpenBannerEditorPacket> STREAM_CODEC = StreamCodec.composite(
        IntArrayCodec.INT_ARRAY,
        OpenBannerEditorPacket::pixels,
        OpenBannerEditorPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handler côté client
     */
    public static void handleData(OpenBannerEditorPacket packet, IPayloadContext context) {
        // Vérifier qu'on est bien côté client
        if (FMLEnvironment.dist.isClient()) {
            context.enqueueWork(() -> {
                // Utiliser la classe de handler client via reflection pour éviter le chargement direct
                try {
                    Class<?> handlerClass = Class.forName("fr.eriniumgroup.erinium_faction.client.network.ClientBannerPacketHandler");
                    handlerClass.getMethod("openBannerEditor", int[].class).invoke(null, (Object) packet.pixels);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to open banner editor", e);
                }
            });
        }
    }
}
