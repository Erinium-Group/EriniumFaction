package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.EriniumFaction;
import fr.eriniumgroup.erinium_faction.features.banner.BannerManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Paquet pour synchroniser toutes les textures de bannière au login
 */
public record SyncAllBannersPacket(
    Map<String, int[]> banners // factionId -> pixels
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncAllBannersPacket> TYPE = new CustomPacketPayload.Type<>(
        ResourceLocation.fromNamespaceAndPath(EriniumFaction.MODID, "sync_all_banners")
    );

    public static final StreamCodec<ByteBuf, SyncAllBannersPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SyncAllBannersPacket decode(ByteBuf buf) {
            int count = buf.readInt();
            Map<String, int[]> banners = new HashMap<>();

            for (int i = 0; i < count; i++) {
                String factionId = ByteBufCodecs.STRING_UTF8.decode(buf);
                int[] pixels = new int[2048];
                for (int j = 0; j < 2048; j++) {
                    pixels[j] = buf.readInt();
                }
                banners.put(factionId, pixels);
            }

            return new SyncAllBannersPacket(banners);
        }

        @Override
        public void encode(ByteBuf buf, SyncAllBannersPacket packet) {
            buf.writeInt(packet.banners.size());

            for (Map.Entry<String, int[]> entry : packet.banners.entrySet()) {
                ByteBufCodecs.STRING_UTF8.encode(buf, entry.getKey());
                int[] pixels = entry.getValue();
                for (int pixel : pixels) {
                    buf.writeInt(pixel);
                }
            }
        }
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Crée un packet avec toutes les bannières du serveur
     */
    public static SyncAllBannersPacket create() {
        Map<String, int[]> banners = new HashMap<>();

        // Lister tous les fichiers PNG dans le dossier des bannières
        File bannerDir = BannerManager.getBannerDirectory().toFile();
        if (bannerDir.exists() && bannerDir.isDirectory()) {
            File[] files = bannerDir.listFiles((dir, name) -> name.endsWith(".png"));
            if (files != null) {
                for (File file : files) {
                    String factionId = file.getName().replace(".png", "");
                    BufferedImage image = BannerManager.loadBanner(factionId);
                    if (image != null && image.getWidth() == 32 && image.getHeight() == 64) {
                        int[] pixels = new int[2048];
                        image.getRGB(0, 0, 32, 64, pixels, 0, 32);
                        banners.put(factionId, pixels);
                    }
                }
            }
        }

        return new SyncAllBannersPacket(banners);
    }

    /**
     * Handler côté client
     */
    public static void handleData(SyncAllBannersPacket packet, IPayloadContext context) {
        // Vérifier qu'on est bien côté client
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            context.enqueueWork(() -> {
                // Utiliser la classe de handler client via reflection
                try {
                    Class<?> handlerClass = Class.forName("fr.eriniumgroup.erinium_faction.client.network.ClientBannerPacketHandler");
                    java.lang.reflect.Method method = handlerClass.getMethod("syncAllBanners", Map.class);
                    method.invoke(null, packet.banners);
                } catch (Exception e) {
                    fr.eriniumgroup.erinium_faction.core.EFC.log.error("Failed to sync all banners", e);
                }
            });
        }
    }
}
