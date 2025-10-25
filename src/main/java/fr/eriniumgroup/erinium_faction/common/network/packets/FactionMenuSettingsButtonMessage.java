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
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import fr.eriniumgroup.erinium_faction.gui.menus.FactionMenuSettingsMenu;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.permissions.EFPerms;
import net.minecraft.server.level.ServerPlayer;

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

	private static void syncSettingsTo(ServerPlayer sp) {
		if (sp == null) return;
		String fid = FactionManager.getPlayerFaction(sp.getUUID());
		if (fid != null) {
			Faction f = FactionManager.getFaction(fid);
			if (f != null) {
				boolean isOpen = f.isOpenFaction();
				boolean isPublic = (f.getMode() == Faction.Mode.PUBLIC);
				boolean isSafe = f.isSafezone();
				sp.connection.send(new net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket(new FactionSettingsStateMessage(isOpen, isPublic, isSafe)));
			}
		}
	}

	/**
	 * Handler principal (côté serveur).
	 */
	public static void handleData(final FactionMenuSettingsButtonMessage message, final IPayloadContext context) {
		if (context.flow() == PacketFlow.SERVERBOUND) {
			context.enqueueWork(() -> handleButtonAction(context.player(), message.buttonID(), message.x(), message.y(), message.z())).exceptionally(e -> {
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
		if (!world.isLoaded(new BlockPos(x, y, z)))
			return;

		ServerPlayer sp = (entity instanceof ServerPlayer) ? (ServerPlayer) entity : null;

		if (buttonID == 0) {
			// Toggle open/close faction settings
			if (sp != null && !EFPerms.has(sp, "ef.faction.settings.open")) {
				entity.sendSystemMessage(Component.translatable("erinium_faction.common.no_permission"));
				syncSettingsTo(sp);
				return;
			}
			String fid = FactionManager.getPlayerFaction(entity.getUUID());
			if (fid != null) {
				Faction f = FactionManager.getFaction(fid);
				if (f != null) {
					f.setOpenFaction(!f.isOpenFaction());
					FactionManager.markDirty();
					syncSettingsTo(sp);
				}
			}
		} else if (buttonID == 1) {
			// Ouvrir le menu de réglages de faction (container + screen côté client)
			MenuProvider provider = new SimpleMenuProvider((id, inv, player) -> new FactionMenuSettingsMenu(id, inv, null), Component.translatable("erinium_faction.faction.menu.settings"));
			entity.openMenu(provider, buf -> buf.writeBlockPos(new BlockPos(x, y, z)));
			// Envoyer l’état initial au client
			syncSettingsTo(sp);
		} else if (buttonID == 2) {
			// Toggle mode PUBLIC/INVITE_ONLY de la faction du joueur
			if (sp != null && !EFPerms.has(sp, "ef.faction.settings.mode")) {
				entity.sendSystemMessage(Component.translatable("erinium_faction.common.no_permission"));
				syncSettingsTo(sp);
				return;
			}
			String fid = FactionManager.getPlayerFaction(entity.getUUID());
			if (fid != null) {
				Faction f = FactionManager.getFaction(fid);
				if (f != null) {
					f.setMode(f.getMode() == Faction.Mode.PUBLIC ? Faction.Mode.INVITE_ONLY : Faction.Mode.PUBLIC);
					FactionManager.markDirty();
					syncSettingsTo(sp);
				}
			}
		} else if (buttonID == 3) {
			// Toggle safezone
			if (sp != null && !EFPerms.has(sp, "ef.faction.settings.safezone")) {
				entity.sendSystemMessage(Component.translatable("erinium_faction.common.no_permission"));
				syncSettingsTo(sp);
				return;
			}
			String fid = FactionManager.getPlayerFaction(entity.getUUID());
			if (fid != null) {
				Faction f = FactionManager.getFaction(fid);
				if (f != null) {
					f.setSafezone(!f.isSafezone());
					FactionManager.markDirty();
					syncSettingsTo(sp);
				}
			}
		}
	}
}