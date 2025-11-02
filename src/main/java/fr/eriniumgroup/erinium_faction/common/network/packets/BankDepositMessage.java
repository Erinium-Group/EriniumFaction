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
 * Client -> Serveur: Déposer de l'argent dans la banque de faction
 */
public record BankDepositMessage(long amount) implements CustomPacketPayload {
    public static final Type<BankDepositMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MODID, "bank_deposit"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BankDepositMessage> STREAM_CODEC = StreamCodec.of(
        (buf, msg) -> buf.writeVarLong(msg.amount),
        (buf) -> new BankDepositMessage(buf.readVarLong())
    );

    @Override
    public Type<BankDepositMessage> type() {
        return TYPE;
    }

    public static void handleData(final BankDepositMessage message, final IPayloadContext ctx) {
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

                // Vérifier que le joueur a assez d'argent
                double playerBalance = EconomyIntegration.getBalance(sp);
                if (playerBalance < amount) {
                    sp.sendSystemMessage(Component.translatable("erinium_faction.cmd.faction.bank.player_not_enough"));
                    return;
                }

                // Retirer l'argent du joueur
                if (!EconomyIntegration.withdraw(sp, amount)) {
                    sp.sendSystemMessage(Component.translatable("erinium_faction.cmd.economy.not_enough"));
                    return;
                }

                // Ajouter l'argent à la faction
                faction.deposit(amount);

                // Ajouter à l'historique
                faction.getTransactionHistory().addTransaction(
                    fr.eriniumgroup.erinium_faction.core.faction.TransactionHistory.TransactionType.DEPOSIT,
                    sp.getName().getString(),
                    amount
                );

                // Sauvegarder
                var server = sp.getServer();
                if (server != null) {
                    FactionManager.save(server);
                }

                // Message de confirmation
                sp.sendSystemMessage(Component.translatable("erinium_faction.cmd.faction.bank.deposit", amount));

                // Synchroniser les données de faction avec TOUS les membres
                FactionDataPacketHandler.sendFactionDataToAllMembers(factionId);

                EFC.log.info("§6Bank", "§a{} deposited §e{} coins §ato faction {}", sp.getName().getString(), amount, faction.getName());
            });
        }
    }
}
