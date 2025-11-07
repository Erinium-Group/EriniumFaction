package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.common.block.entity.FaceMode;
import fr.eriniumgroup.erinium_faction.common.block.entity.IConfigurableMachine;
import fr.eriniumgroup.erinium_faction.core.EFC;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet pour synchroniser la configuration des faces d'une machine
 */
public record FaceConfigPacket(BlockPos pos, ConfigAction action, Direction face,
                               FaceMode mode) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<FaceConfigPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "face_config"));

    public static final StreamCodec<ByteBuf, FaceConfigPacket> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, FaceConfigPacket::pos, ByteBufCodecs.VAR_INT.map(ConfigAction::fromId, ConfigAction::getId), FaceConfigPacket::action, ByteBufCodecs.VAR_INT.map(id -> Direction.from3DDataValue(id), Direction::get3DDataValue), FaceConfigPacket::face, ByteBufCodecs.VAR_INT.map(FaceMode::fromOrdinal, FaceMode::ordinal), FaceConfigPacket::mode, FaceConfigPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum ConfigAction {
        SET_FACE_MODE(0), TOGGLE_AUTO_INPUT(1), TOGGLE_AUTO_OUTPUT(2);

        private final int id;

        ConfigAction(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static ConfigAction fromId(int id) {
            for (ConfigAction action : values()) {
                if (action.id == id) return action;
            }
            return SET_FACE_MODE;
        }
    }

    /**
     * Gère le packet côté serveur
     */
    public static void handleData(FaceConfigPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            // Vérifier que le joueur est assez proche du bloc
            if (player.distanceToSqr(packet.pos.getX() + 0.5, packet.pos.getY() + 0.5, packet.pos.getZ() + 0.5) > 64.0) {
                return;
            }

            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (!(be instanceof IConfigurableMachine machine)) {
                return;
            }

            switch (packet.action) {
                case SET_FACE_MODE:
                    machine.setFaceMode(packet.face, packet.mode);
                    machine.onConfigurationChanged();
                    break;

                case TOGGLE_AUTO_INPUT:
                    machine.setAutoInput(!machine.isAutoInput());
                    machine.onConfigurationChanged();
                    break;

                case TOGGLE_AUTO_OUTPUT:
                    machine.setAutoOutput(!machine.isAutoOutput());
                    machine.onConfigurationChanged();
                    break;
            }
        });
    }
}

