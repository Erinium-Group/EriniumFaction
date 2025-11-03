package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record TopLuckSyncMessage(List<CategoryEntry> categories, List<BlockEntry> blocks, long total, String target) implements CustomPacketPayload {
    public static final Type<TopLuckSyncMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "topluck_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TopLuckSyncMessage> STREAM_CODEC = StreamCodec.of((buf, msg) -> {
        // Write categories
        buf.writeVarInt(msg.categories.size());
        for (CategoryEntry c : msg.categories) {
            buf.writeUtf(c.id);
            buf.writeUtf(c.label);
            buf.writeUtf(c.icon);
            buf.writeVarLong(c.count);
            buf.writeDouble(c.ratio);
            buf.writeDouble(c.weighted);
            buf.writeDouble(c.weight);
        }
        // Write blocks
        buf.writeVarInt(msg.blocks.size());
        for (BlockEntry b : msg.blocks) {
            buf.writeUtf(b.id);
            buf.writeVarLong(b.count);
            buf.writeDouble(b.ratio);
            buf.writeVarLong(b.baselineCount);
        }
        // Write total and target
        buf.writeVarLong(msg.total);
        buf.writeUtf(msg.target == null ? "" : msg.target);
    }, (buf) -> {
        // Read categories
        int csz = buf.readVarInt();
        List<CategoryEntry> cats = new ArrayList<>(csz);
        for (int i = 0; i < csz; i++) {
            String id = buf.readUtf();
            String label = buf.readUtf();
            String icon = buf.readUtf();
            long count = buf.readVarLong();
            double ratio = buf.readDouble();
            double weighted = buf.readDouble();
            double weight = buf.readDouble();
            cats.add(new CategoryEntry(id, label, icon, count, ratio, weighted, weight));
        }
        // Read blocks
        int bsz = buf.readVarInt();
        List<BlockEntry> blks = new ArrayList<>(bsz);
        for (int i = 0; i < bsz; i++) {
            String id = buf.readUtf();
            long count = buf.readVarLong();
            double ratio = buf.readDouble();
            long baselineCount = buf.readVarLong();
            blks.add(new BlockEntry(id, count, ratio, baselineCount));
        }
        long total = buf.readVarLong();
        String target = buf.readUtf();
        return new TopLuckSyncMessage(cats, blks, total, target);
    });

    @Override
    public Type<TopLuckSyncMessage> type() { return TYPE; }

    public static void handleData(final TopLuckSyncMessage msg, final IPayloadContext ctx) {
        if (ctx.flow() == PacketFlow.CLIENTBOUND) {
            // Appeler le handler client via une classe séparée pour éviter de charger Screen sur le serveur
            if (FMLEnvironment.dist == Dist.CLIENT) {
                ctx.enqueueWork(() -> ClientHandler.handle(msg));
            }
        }
    }

    // Classe interne statique qui ne sera chargée que côté client
    private static class ClientHandler {
        static void handle(TopLuckSyncMessage msg) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            mc.setScreen(new fr.eriniumgroup.erinium_faction.gui.screens.TopLuckScreen(msg));
        }
    }

    public static void sendTo(net.minecraft.server.level.ServerPlayer sp, List<CategoryEntry> categories, List<BlockEntry> blocks, long total, String target) {
        PacketDistributor.sendToPlayer(sp, new TopLuckSyncMessage(categories, blocks, total, target));
    }

    public record CategoryEntry(String id, String label, String icon, long count, double ratio, double weighted, double weight) {}

    public record BlockEntry(String id, long count, double ratio, long baselineCount) {}
}
