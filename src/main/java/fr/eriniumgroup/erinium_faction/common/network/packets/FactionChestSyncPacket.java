package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.gui.menus.FactionMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet pour synchroniser un slot du chest de faction entre tous les joueurs
 */
public record FactionChestSyncPacket(int slot, ItemStack stack) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<FactionChestSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "faction_chest_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FactionChestSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            FactionChestSyncPacket::slot,
            ItemStack.OPTIONAL_STREAM_CODEC,
            FactionChestSyncPacket::stack,
            FactionChestSyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(FactionChestSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Côté client uniquement
            if (context.flow().isClientbound()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && mc.player.containerMenu instanceof FactionMenu menu) {
                    // Mettre à jour le slot dans l'inventaire local
                    if (packet.slot >= 0 && packet.slot < FactionMenu.FACTION_CHEST_SLOTS) {
                        try {
                            // Accéder à l'ItemStackHandler interne via les slots
                            var slot = menu.slots.get(packet.slot);
                            if (slot != null) {
                                slot.set(packet.stack.copy());
                                System.out.println("[FactionChestSync] Client received slot update: slot=" + packet.slot + ", stack=" + packet.stack);
                            }
                        } catch (Exception e) {
                            EFC.log.error("ChestSync", "Failed to sync slot {}: {}", packet.slot, e.getMessage());
                        }
                    }
                }
            }
        });
    }
}
