package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.EFC;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.BlockPos;

import fr.eriniumgroup.eriniumfaction.procedures.ToggleOpenFactionProcedure;

public record FactionMenuSettingsButtonMessage(int buttonID, int x, int y, int z) implements CustomPacketPayload {

	public static final Type<FactionMenuSettingsButtonMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "faction_menu_settings_buttons"));
	public static final StreamCodec<RegistryFriendlyByteBuf, FactionMenuSettingsButtonMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, FactionMenuSettingsButtonMessage message) -> {
		buffer.writeInt(message.buttonID);
		buffer.writeInt(message.x);
		buffer.writeInt(message.y);
		buffer.writeInt(message.z);
	}, (RegistryFriendlyByteBuf buffer) -> new FactionMenuSettingsButtonMessage(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt()));

	@Override
	public Type<FactionMenuSettingsButtonMessage> type() {
		return TYPE;
	}

	/**
	 * Handler principal (côté serveur).
	 */
	public static void handleData(final FactionMenuSettingsButtonMessage message, final IPayloadContext context) {
		if (context.flow() == PacketFlow.SERVERBOUND) {
			context.enqueueWork(() -> handleButtonAction(context.player(), message.buttonID, message.x, message.y, message.z)).exceptionally(e -> {
				context.connection().disconnect(Component.literal(e.getMessage()));
				return null;
			});
		}
	}

	/**
	 * Logique serveur: actions liées aux boutons du menu Faction.
	 */
	public static void handleButtonAction(Player entity, int buttonID, int x, int y, int z) {
		Level world = entity.level();
		// Sécurité: ne pas générer de chunk arbitrairement
		if (!world.hasChunkAt(new BlockPos(x, y, z)))
			return;
		if (buttonID == 0) {

			ToggleOpenFactionProcedure.execute(entity);
		}
	}
}