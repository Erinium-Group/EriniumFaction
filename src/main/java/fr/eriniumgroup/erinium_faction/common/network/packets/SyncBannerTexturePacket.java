package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.EriniumFaction;
import fr.eriniumgroup.erinium_faction.common.network.codecs.IntArrayCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Paquet pour synchroniser une texture de bannière du serveur vers le client
 * Envoyé quand un joueur obtient une bannière custom
 */
public record SyncBannerTexturePacket(
    String factionId,
    int[] pixels // 32x64 = 2048 pixels en format ARGB
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncBannerTexturePacket> TYPE = new CustomPacketPayload.Type<>(
        ResourceLocation.fromNamespaceAndPath(EriniumFaction.MODID, "sync_banner_texture")
    );

    public static final StreamCodec<ByteBuf, SyncBannerTexturePacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        SyncBannerTexturePacket::factionId,
        IntArrayCodec.INT_ARRAY,
        SyncBannerTexturePacket::pixels,
        SyncBannerTexturePacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handler côté client
     */
    public static void handleData(SyncBannerTexturePacket packet, IPayloadContext context) {
        // Vérifier qu'on est bien côté client
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            context.enqueueWork(() -> {
                // Utiliser la classe de handler client via reflection pour éviter le chargement direct
                try {
                    Class<?> handlerClass = Class.forName("fr.eriniumgroup.erinium_faction.client.network.ClientBannerPacketHandler");
                    handlerClass.getMethod("syncBannerTexture", String.class, int[].class)
                        .invoke(null, packet.factionId, packet.pixels);
                } catch (Exception e) {
                    fr.eriniumgroup.erinium_faction.core.EFC.log.error("Failed to sync banner texture", e);
                }
            });
        }
    }
}
