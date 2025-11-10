package fr.eriniumgroup.erinium_faction.client.gui.bounty;

import fr.eriniumgroup.erinium_faction.common.network.packets.BountyDataPacket;

import java.util.ArrayList;
import java.util.List;

/**
 * Stocke les données de bounty côté client pour les GUI
 */
public class BountyClientData {
    private static List<BountyDataPacket.BountyEntry> bounties = new ArrayList<>();

    public static void setBounties(List<BountyDataPacket.BountyEntry> newBounties) {
        bounties = new ArrayList<>(newBounties);
    }

    public static List<BountyDataPacket.BountyEntry> getBounties() {
        return new ArrayList<>(bounties);
    }

    public static void clear() {
        bounties.clear();
    }
}
