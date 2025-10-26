package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.*;

/**
 * Serveur -> Client: synchro de l'état des paramètres de faction pour l'écran Settings.
 */
public record FactionSettingsStateMessage(
    boolean isOpen,
    boolean isPublicMode,
    boolean isSafezone,
    String displayName,
    String description,
    List<RankInfo> ranks
) implements CustomPacketPayload {

    public static class RankInfo {
        public String id;
        public String display;
        public int priority;
        public Set<String> perms;

        public RankInfo(String id, String display, int priority, Set<String> perms) {
            this.id = id;
            this.display = display;
            this.priority = priority;
            this.perms = perms;
        }
    }

    public static final Type<FactionSettingsStateMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "faction_settings_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FactionSettingsStateMessage> STREAM_CODEC = StreamCodec.of((buf, msg) -> {
        buf.writeBoolean(msg.isOpen);
        buf.writeBoolean(msg.isPublicMode);
        buf.writeBoolean(msg.isSafezone);
        buf.writeUtf(msg.displayName != null ? msg.displayName : "");
        buf.writeUtf(msg.description != null ? msg.description : "");

        // Rangs
        buf.writeInt(msg.ranks != null ? msg.ranks.size() : 0);
        if (msg.ranks != null) {
            for (RankInfo rank : msg.ranks) {
                buf.writeUtf(rank.id);
                buf.writeUtf(rank.display);
                buf.writeInt(rank.priority);
                buf.writeInt(rank.perms.size());
                for (String perm : rank.perms) {
                    buf.writeUtf(perm);
                }
            }
        }
    }, (buf) -> {
        boolean isOpen = buf.readBoolean();
        boolean isPublicMode = buf.readBoolean();
        boolean isSafezone = buf.readBoolean();
        String displayName = buf.readUtf();
        String description = buf.readUtf();

        // Rangs
        int rankCount = buf.readInt();
        List<RankInfo> ranks = new ArrayList<>();
        for (int i = 0; i < rankCount; i++) {
            String id = buf.readUtf();
            String display = buf.readUtf();
            int priority = buf.readInt();
            int permCount = buf.readInt();
            Set<String> perms = new HashSet<>();
            for (int j = 0; j < permCount; j++) {
                perms.add(buf.readUtf());
            }
            ranks.add(new RankInfo(id, display, priority, perms));
        }

        return new FactionSettingsStateMessage(isOpen, isPublicMode, isSafezone, displayName, description, ranks);
    });

    @Override
    public Type<FactionSettingsStateMessage> type() { return TYPE; }

    public static void handleData(final FactionSettingsStateMessage message, final IPayloadContext ctx) {
        if (ctx.flow() == PacketFlow.CLIENTBOUND) {
            ctx.enqueueWork(() -> fr.eriniumgroup.erinium_faction.gui.screens.FactionMenuSettingsScreen.onSettingsState(message));
        }
    }
}

