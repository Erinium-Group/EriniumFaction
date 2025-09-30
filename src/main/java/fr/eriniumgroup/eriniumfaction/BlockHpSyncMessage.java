package fr.eriniumgroup.eriniumfaction;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = "erinium_faction")
public class BlockHpSyncMessage {
	private final BlockPos pos;
	private final int current;
	private final int base;

	public BlockHpSyncMessage(FriendlyByteBuf buffer) {
		this.pos = buffer.readBlockPos();
		this.current = buffer.readInt();
		this.base = buffer.readInt();
	}

	public BlockHpSyncMessage(BlockPos pos, int current, int base) {
		this.pos = pos;
		this.current = current;
		this.base = base;
	}

	public static void buffer(BlockHpSyncMessage message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeInt(message.current);
		buffer.writeInt(message.base);
	}

	public static void handler(BlockHpSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			BlockHpRenderer.updateBlockHp(message.pos, message.current, message.base);
		});
		context.setPacketHandled(true);
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		EriniumFactionMod.addNetworkMessage(BlockHpSyncMessage.class,
				BlockHpSyncMessage::buffer,
				BlockHpSyncMessage::new,
				BlockHpSyncMessage::handler);
	}
}