package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.features.economy.EconomyIntegration;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client -> Serveur: Retirer de l'argent de la banque de faction
 */
public record BankWithdrawMessage(long amount) implements CustomPacketPayload {
    public static final Type<BankWithdrawMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "bank_withdraw"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BankWithdrawMessage> STREAM_CODEC = StreamCodec.of(
        (buf, msg) -> buf.writeVarLong(msg.amount),
        (buf) -> new BankWithdrawMessage(buf.readVarLong())
    );

    @Override
    public Type<BankWithdrawMessage> type() {
        return TYPE;
    }

    public static void handleData(final BankWithdrawMessage message, final IPayloadContext ctx) {
        if (ctx.flow() == PacketFlow.SERVERBOUND && ctx.player() instanceof ServerPlayer sp) {
            ctx.enqueueWork(() -> {
                long amount = message.amount;

                // Validation
                if (amount <= 0) {
                    sp.sendSystemMessage(Component.translatable("erinium_faction.cmd.economy.invalid_amount"));
                    return;
                }

                // Vérifier que le joueur est dans une faction
                String factionId = FactionManager.getPlayerFaction(sp.getUUID());
                if (factionId == null) {
                    sp.sendSystemMessage(Component.translatable("erinium_faction.cmd.faction.not_in_faction"));
                    return;
                }

                var faction = FactionManager.getFaction(factionId);
                if (faction == null) {
                    sp.sendSystemMessage(Component.translatable("erinium_faction.cmd.faction.not_found"));
                    return;
                }

                // Vérifier que la faction a assez d'argent
                if (faction.getBankBalance() < amount) {
                    sp.sendSystemMessage(Component.translatable("erinium_faction.cmd.faction.bank.not_enough"));
                    return;
                }

                // TODO: Vérifier les permissions (MANAGE_ECONOMY)
                // Pour l'instant, on autorise tout le monde

                // Retirer l'argent de la faction
                if (!faction.withdraw(amount)) {
                    sp.sendSystemMessage(Component.translatable("erinium_faction.cmd.faction.bank.not_enough"));
                    return;
                }

                // Ajouter l'argent au joueur
                EconomyIntegration.deposit(sp, amount);

                // Ajouter à l'historique
                faction.getTransactionHistory().addTransaction(
                    fr.eriniumgroup.erinium_faction.core.faction.TransactionHistory.TransactionType.WITHDRAW,
                    sp.getName().getString(),
                    amount
                );

                // Sauvegarder
                var server = sp.getServer();
                if (server != null) {
                    FactionManager.save(server);
                }

                // Message de confirmation
                sp.sendSystemMessage(Component.translatable("erinium_faction.cmd.faction.bank.withdraw", amount));

                // Synchroniser les données de faction avec TOUS les membres
                FactionDataPacketHandler.sendFactionDataToAllMembers(factionId);

                EFC.log.info("§6Bank", "§a{} withdrew §e{} coins §afrom faction {}", sp.getName().getString(), amount, faction.getName());
            });
        }
    }
}
