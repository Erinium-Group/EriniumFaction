package fr.eriniumgroup.erinium_faction.common.network.packets;

import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.faction.FactionSnapshot;
import fr.eriniumgroup.erinium_faction.gui.screens.FactionClientData;
import fr.eriniumgroup.erinium_faction.gui.screens.FactionMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
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
        context.enqueueWork(() -> {
            // Stocker les données dans le système client
            FactionClientData.setFactionData(packet.snapshot());

            // Si le GUI de faction est ouvert, le mettre à jour
            var minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof FactionMenuScreen screen) {
                screen.updateFactionData(packet.snapshot());
            }
        });
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
                FactionSnapshot snapshot = FactionSnapshot.of(faction);
                PacketDistributor.sendToPlayer(player, new FactionDataPacket(snapshot));
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
            FactionSnapshot snapshot = FactionSnapshot.of(faction);
            var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                for (var uuid : faction.getMembers().keySet()) {
                    ServerPlayer player = server.getPlayerList().getPlayer(uuid);
                    if (player != null) {
                        PacketDistributor.sendToPlayer(player, new FactionDataPacket(snapshot));
                    }
                }
            }
        }
    }
}
