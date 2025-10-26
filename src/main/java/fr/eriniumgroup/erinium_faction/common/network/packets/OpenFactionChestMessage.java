package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.gui.menus.FactionChestMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenFactionChestMessage() implements CustomPacketPayload {
    public static final Type<OpenFactionChestMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "open_faction_chest"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenFactionChestMessage> STREAM_CODEC = StreamCodec.of(
        (buf, msg) -> {},
        (buf) -> new OpenFactionChestMessage()
    );

    @Override
    public Type<OpenFactionChestMessage> type() {
        return TYPE;
    }

    public static void handleData(final OpenFactionChestMessage message, final IPayloadContext context) {
        if (context.flow() == PacketFlow.SERVERBOUND) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer sp) {
                    String fid = FactionManager.getPlayerFaction(sp.getUUID());
                    if (fid != null) {
                        Faction f = FactionManager.getFaction(fid);
                        if (f != null) {
                            // Vérifier la permission
                            if (!f.hasPermission(sp.getUUID(), "faction.chest.access")) {
                                sp.sendSystemMessage(Component.translatable("erinium_faction.chest.no_permission"));
                                return;
                            }

                            // Ouvrir le coffre
                            sp.openMenu(new SimpleMenuProvider(
                                (id, inv, player) -> new FactionChestMenu(id, inv, fid),
                                Component.translatable("erinium_faction.chest.title", f.getName())
                            ), buf -> buf.writeUtf(fid));
                        }
                    }
                }
            });
        }
    }
}

