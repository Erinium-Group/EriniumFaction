package fr.eriniumgroup.erinium_faction.common.network;

import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class EFVariables {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, EFC.MODID);
    public static final Supplier<AttachmentType<PlayerVariables>> PLAYER_VARIABLES = ATTACHMENT_TYPES.register("player_variables", () -> AttachmentType.serializable(PlayerVariables::new).build());

    @EventBusSubscriber
    public static class EventBusVariableHandlers {
        @SubscribeEvent
        public static void onPlayerLoggedInSyncPlayerVariables(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                // Assigner le rank "default" si le joueur n'en a pas
                if (fr.eriniumgroup.erinium_faction.core.rank.EFRManager.get().getPlayerRankId(player.getUUID()) == null) {
                    fr.eriniumgroup.erinium_faction.core.rank.EFRManager.get().setPlayerRank(player.getUUID(), "default");
                }
                // mettre à jour les variables depuis le serveur avant sync
                fr.eriniumgroup.erinium_faction.core.faction.FactionManager.populatePlayerVariables(player, player.getData(PLAYER_VARIABLES));
                player.getData(PLAYER_VARIABLES).syncPlayerVariables(event.getEntity());

                // Synchroniser la liste des membres pour tous les membres de la faction
                String factionName = fr.eriniumgroup.erinium_faction.core.faction.FactionManager.getPlayerFaction(player.getUUID());
                if (factionName != null && !factionName.isEmpty()) {
                    fr.eriniumgroup.erinium_faction.common.network.packets.FactionDataPacketHandler.sendFactionDataToAllMembers(factionName);
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerLoggedOutSync(PlayerEvent.PlayerLoggedOutEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                // Synchroniser la liste des membres pour tous les membres de la faction
                String factionName = fr.eriniumgroup.erinium_faction.core.faction.FactionManager.getPlayerFaction(player.getUUID());
                if (factionName != null && !factionName.isEmpty()) {
                    fr.eriniumgroup.erinium_faction.common.network.packets.FactionDataPacketHandler.sendFactionDataToAllMembers(factionName);
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerRespawnedSyncPlayerVariables(PlayerEvent.PlayerRespawnEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                fr.eriniumgroup.erinium_faction.core.faction.FactionManager.populatePlayerVariables(player, player.getData(PLAYER_VARIABLES));
                player.getData(PLAYER_VARIABLES).syncPlayerVariables(event.getEntity());
            }
        }

        @SubscribeEvent
        public static void onPlayerChangedDimensionSyncPlayerVariables(PlayerEvent.PlayerChangedDimensionEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                fr.eriniumgroup.erinium_faction.core.faction.FactionManager.populatePlayerVariables(player, player.getData(PLAYER_VARIABLES));
                player.getData(PLAYER_VARIABLES).syncPlayerVariables(event.getEntity());
            }
        }

        @SubscribeEvent
        public static void clonePlayer(PlayerEvent.Clone event) {
            PlayerVariables original = event.getOriginal().getData(PLAYER_VARIABLES);
            PlayerVariables clone = new PlayerVariables();
            clone.lastChunk = original.lastChunk;
            clone.factionInChunk = original.factionInChunk;
            clone.lastRegion = original.lastRegion;
            // nouveaux champs
            clone.factionId = original.factionId;
            clone.factionName = original.factionName;
            clone.factionPower = original.factionPower;
            clone.factionMaxPower = original.factionMaxPower;
            clone.factionLevel = original.factionLevel;
            clone.factionXp = original.factionXp;
            clone.serverRankId = original.serverRankId;
            clone.playerPower = original.playerPower;
            clone.playerMaxPower = original.playerMaxPower;// money
            clone.money = original.money;
            event.getEntity().setData(PLAYER_VARIABLES, clone);
        }
    }

    public static class PlayerVariables implements INBTSerializable<CompoundTag> {
        public String lastChunk = "";
        public String factionInChunk = "";
        public String lastRegion = "";

        // affichage client faction
        public String factionId = "";
        public String factionName = "";
        public double factionPower = 0;
        public double factionMaxPower = 0;
        public int factionLevel = 0;
        public int factionXp = 0;
        public String serverRankId = ""; // rank global (VIP, etc.)
        public double playerPower = 0;
        public double playerMaxPower = 0;
        // économie joueur
        public double money = 0;

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider lookupProvider) {
            CompoundTag nbt = new CompoundTag();
            nbt.putString("lastChunk", lastChunk);
            nbt.putString("factionInChunk", factionInChunk);
            nbt.putString("lastRegion", lastRegion);

            nbt.putString("factionId", factionId);
            nbt.putString("factionName", factionName);
            nbt.putDouble("factionPower", factionPower);
            nbt.putDouble("factionMaxPower", factionMaxPower);
            nbt.putInt("factionLevel", factionLevel);
            nbt.putInt("factionXp", factionXp);
            nbt.putString("serverRankId", serverRankId);
            nbt.putDouble("playerPower", playerPower);
            nbt.putDouble("playerMaxPower", playerMaxPower);
            nbt.putDouble("money", money);
            return nbt;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider lookupProvider, CompoundTag nbt) {
            lastChunk = nbt.getString("lastChunk");
            factionInChunk = nbt.getString("factionInChunk");
            lastRegion = nbt.getString("lastRegion");

            factionId = nbt.getString("factionId");
            factionName = nbt.getString("factionName");
            factionPower = nbt.getDouble("factionPower");
            factionMaxPower = nbt.getDouble("factionMaxPower");
            factionLevel = nbt.getInt("factionLevel");
            factionXp = nbt.getInt("factionXp");
            serverRankId = nbt.getString("serverRankId");
            playerPower = nbt.getDouble("playerPower");
            playerMaxPower = nbt.getDouble("playerMaxPower");
            money = nbt.contains("money") ? nbt.getDouble("money") : 0;
        }

        public void syncPlayerVariables(Entity entity) {
            if (entity instanceof ServerPlayer serverPlayer)
                PacketDistributor.sendToPlayer(serverPlayer, new PlayerVariablesSyncMessage(this));
        }
    }

    public record PlayerVariablesSyncMessage(PlayerVariables data) implements CustomPacketPayload {
        public static final Type<PlayerVariablesSyncMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "player_variables_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, PlayerVariablesSyncMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, PlayerVariablesSyncMessage message) -> buffer.writeNbt(message.data().serializeNBT(buffer.registryAccess())), (RegistryFriendlyByteBuf buffer) -> {
            PlayerVariablesSyncMessage message = new PlayerVariablesSyncMessage(new PlayerVariables());
            message.data.deserializeNBT(buffer.registryAccess(), buffer.readNbt());
            return message;
        });

        @Override
        public Type<PlayerVariablesSyncMessage> type() {
            return TYPE;
        }

        public static void handleData(final PlayerVariablesSyncMessage message, final IPayloadContext context) {
            if (context.flow() == PacketFlow.CLIENTBOUND && message.data != null) {
                context.enqueueWork(() -> context.player().getData(PLAYER_VARIABLES).deserializeNBT(context.player().registryAccess(), message.data.serializeNBT(context.player().registryAccess()))).exceptionally(e -> {
                    context.connection().disconnect(Component.literal(e.getMessage()));
                    return null;
                });
            }
        }
    }
}

