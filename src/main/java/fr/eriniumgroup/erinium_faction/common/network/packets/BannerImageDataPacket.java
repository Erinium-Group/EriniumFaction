package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.EriniumFaction;
import fr.eriniumgroup.erinium_faction.common.network.codecs.IntArrayCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Paquet pour envoyer les données d'image de bannière du serveur au client
 * Format: tableau de pixels ARGB (32x64 = 2048 pixels)
 */
public record BannerImageDataPacket(
    int[] pixels // 32x64 = 2048 pixels en format ARGB
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BannerImageDataPacket> TYPE = new CustomPacketPayload.Type<>(
        ResourceLocation.fromNamespaceAndPath(EriniumFaction.MODID, "banner_image_data")
    );

    // StreamCodec pour tableau d'int
    public static final StreamCodec<ByteBuf, BannerImageDataPacket> STREAM_CODEC = StreamCodec.composite(
        IntArrayCodec.INT_ARRAY,
        BannerImageDataPacket::pixels,
        BannerImageDataPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handler côté client
     */
    public static void handleData(BannerImageDataPacket packet, IPayloadContext context) {
        // Vérifier qu'on est bien côté client
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            context.enqueueWork(() -> {
                // Utiliser la classe de handler client via reflection pour éviter le chargement direct
                try {
                    Class<?> handlerClass = Class.forName("fr.eriniumgroup.erinium_faction.client.network.ClientBannerPacketHandler");
                    handlerClass.getMethod("storeBannerImage", int[].class).invoke(null, (Object) packet.pixels);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to store banner image", e);
                }
            });
        }
    }
}
