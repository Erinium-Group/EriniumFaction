package fr.eriniumgroup.eriniumfaction;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = "erinium_faction")
public record BlockHpSyncMessage(BlockPos pos, int current, int base) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<BlockHpSyncMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("erinium_faction", "block_hp_sync"));

	public static final StreamCodec<FriendlyByteBuf, BlockHpSyncMessage> STREAM_CODEC = StreamCodec.composite(
		BlockPos.STREAM_CODEC,
		BlockHpSyncMessage::pos,
		StreamCodec.of((buf, val) -> buf.writeInt(val), FriendlyByteBuf::readInt),
		BlockHpSyncMessage::current,
		StreamCodec.of((buf, val) -> buf.writeInt(val), FriendlyByteBuf::readInt),
		BlockHpSyncMessage::base,
		BlockHpSyncMessage::new
	);

	@Override
	public CustomPacketPayload.Type<BlockHpSyncMessage> type() {
		return TYPE;
	}

	public static void handleData(final BlockHpSyncMessage message, final IPayloadContext context) {
		context.enqueueWork(() -> {
			BlockHpRenderer.updateBlockHp(message.pos, message.current, message.base);
		});
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		EriniumFactionMod.addNetworkMessage(BlockHpSyncMessage.TYPE, BlockHpSyncMessage.STREAM_CODEC, BlockHpSyncMessage::handleData);
	}
}
