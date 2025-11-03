package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.faction.FactionSnapshot;
import fr.eriniumgroup.erinium_faction.features.economy.EconomyIntegration;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Gestionnaire des paquets pour la synchronisation des données de faction
 */
public class FactionDataPacketHandler {

    /**
     * Handler côté client pour recevoir les données de faction
     */
    public static void handleFactionData(FactionDataPacket packet, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            context.enqueueWork(() -> ClientHandler.handle(packet));
        }
    }

    // Classe interne statique qui ne sera chargée que côté client
    private static class ClientHandler {
        static void handle(FactionDataPacket packet) {
            // Stocker les données dans le système client
            fr.eriniumgroup.erinium_faction.gui.screens.FactionClientData.setFactionData(packet.snapshot());

            // Si le GUI de faction est ouvert, le mettre à jour
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.screen instanceof fr.eriniumgroup.erinium_faction.gui.screens.FactionMenuScreen screen) {
                screen.updateFactionData(packet.snapshot());
            }
        }
    }

    /**
     * Envoie les données de faction d'un joueur vers son client
     * @param player Le joueur cible
     */
    public static void sendFactionDataToPlayer(ServerPlayer player) {
        String factionName = FactionManager.getPlayerFaction(player.getUUID());
        if (factionName != null && !factionName.isEmpty()) {
            Faction faction = FactionManager.getFaction(factionName);
            if (faction != null) {
                FactionSnapshot snapshot = FactionSnapshot.of(faction, player);
                PacketDistributor.sendToPlayer(player, new FactionDataPacket(snapshot));

                // Envoyer aussi l'historique des transactions
                SyncTransactionHistoryMessage.sendTo(player, faction.getTransactionHistory());

                // Envoyer le solde du joueur
                SyncPlayerBalanceMessage.sendTo(player, EconomyIntegration.getBalance(player));
            }
        }
    }

    /**
     * Envoie les données de faction à tous les membres de la faction
     * @param factionName Le nom de la faction
     */
    public static void sendFactionDataToAllMembers(String factionName) {
        Faction faction = FactionManager.getFaction(factionName);
        if (faction != null) {
            var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                for (var uuid : faction.getMembers().keySet()) {
                    ServerPlayer player = server.getPlayerList().getPlayer(uuid);
                    if (player != null) {
                        // Créer un snapshot personnalisé pour chaque joueur avec son propre power
                        FactionSnapshot snapshot = FactionSnapshot.of(faction, player);
                        PacketDistributor.sendToPlayer(player, new FactionDataPacket(snapshot));

                        // Envoyer aussi l'historique des transactions
                        SyncTransactionHistoryMessage.sendTo(player, faction.getTransactionHistory());

                        // Envoyer le solde du joueur
                        SyncPlayerBalanceMessage.sendTo(player, EconomyIntegration.getBalance(player));
                    }
                }
            }
        }
    }
}
