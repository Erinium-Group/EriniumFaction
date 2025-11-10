package fr.eriniumgroup.erinium_faction.features.bounty;

import fr.eriniumgroup.erinium_faction.features.economy.EconomyIntegration;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Gestionnaire central des bounties avec persistance via SavedData
 */
public class BountyManager extends SavedData {
    private static final String DATA_NAME = "erinium_faction_bounties";

    private final Map<UUID, Bounty> activeBounties = new ConcurrentHashMap<>(); // targetId -> Bounty

    public BountyManager() {
        super();
    }

    /**
     * Récupère l'instance du manager depuis le SavedData
     */
    public static BountyManager get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(ServerLevel.OVERWORLD);
        if (overworld == null) {
            throw new IllegalStateException("Overworld not loaded!");
        }
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(BountyManager::new, BountyManager::load, null),
                DATA_NAME
        );
    }

    /**
     * Place une prime sur un joueur
     * @return 0 = succès, 1 = montant insuffisant, 2 = pas assez d'argent, 3 = erreur
     */
    public int placeBounty(ServerPlayer issuer, UUID targetId, String targetName, double amount) {
        BountyConfig config = BountyConfig.get();

        // Vérifier le montant minimum
        if (amount < config.getMinimumBounty()) {
            return 1;
        }

        // Calculer le montant total avec commission
        double commission = amount * config.getCommissionRate();
        double totalCost = amount + commission;

        // Vérifier le solde
        double balance = EconomyIntegration.getBalance(issuer);
        if (balance < totalCost) {
            return 2;
        }

        // Retirer l'argent
        boolean success = EconomyIntegration.withdraw(issuer, totalCost);
        if (!success) {
            return 3;
        }

        // Ajouter ou créer la bounty
        Bounty bounty = activeBounties.computeIfAbsent(targetId, k -> new Bounty(targetId, targetName));
        bounty.addContribution(issuer.getUUID(), issuer.getName().getString(), amount);

        setDirty();
        return 0;
    }

    /**
     * Récupère une bounty par l'ID du joueur ciblé
     */
    public Optional<Bounty> getBounty(UUID targetId) {
        return Optional.ofNullable(activeBounties.get(targetId));
    }

    /**
     * Récupère toutes les bounties actives triées par montant décroissant
     */
    public List<Bounty> getAllBounties() {
        return activeBounties.values().stream()
                .filter(b -> !b.isExpired())
                .sorted(Comparator.comparingDouble(Bounty::getTotalAmount).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Récupère les top N bounties
     */
    public List<Bounty> getTopBounties(int limit) {
        return getAllBounties().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Récupère toutes les bounties triées par montant (pour GUI)
     */
    public List<Bounty> getBountiesSortedByAmount() {
        return getAllBounties();
    }

    /**
     * Réclame une bounty (appelé quand un joueur tue la cible)
     * @return Le montant gagné, ou 0 si pas de bounty
     */
    public double claimBounty(ServerPlayer killer, UUID victimId) {
        Bounty bounty = activeBounties.remove(victimId);
        if (bounty == null || bounty.isExpired()) {
            return 0;
        }

        double reward = bounty.getTotalAmount();

        // Donner la récompense au tueur
        EconomyIntegration.deposit(killer, reward);

        setDirty();
        return reward;
    }

    /**
     * Annule une bounty (appelé à l'expiration)
     */
    public void cancelBounty(UUID targetId) {
        activeBounties.remove(targetId);
        setDirty();
    }

    /**
     * Nettoie les bounties expirées
     */
    public void cleanupExpiredBounties() {
        List<UUID> expired = activeBounties.values().stream()
                .filter(Bounty::isExpired)
                .map(Bounty::getTargetId)
                .collect(Collectors.toList());

        if (!expired.isEmpty()) {
            expired.forEach(activeBounties::remove);
            setDirty();
        }
    }

    /**
     * Vérifie si un joueur a une bounty active
     */
    public boolean hasBounty(UUID playerId) {
        Bounty bounty = activeBounties.get(playerId);
        return bounty != null && !bounty.isExpired();
    }

    /**
     * Récupère le nombre total de bounties actives
     */
    public int getActiveBountyCount() {
        return (int) activeBounties.values().stream()
                .filter(b -> !b.isExpired())
                .count();
    }

    // Serialization ---------------------------------------------------------------

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        ListTag bountiesList = new ListTag();
        for (Bounty bounty : activeBounties.values()) {
            if (!bounty.isExpired()) {
                bountiesList.add(bounty.toNBT());
            }
        }
        tag.put("Bounties", bountiesList);
        return tag;
    }

    public static BountyManager load(CompoundTag tag, HolderLookup.Provider provider) {
        BountyManager manager = new BountyManager();

        ListTag bountiesList = tag.getList("Bounties", Tag.TAG_COMPOUND);
        for (int i = 0; i < bountiesList.size(); i++) {
            CompoundTag bountyTag = bountiesList.getCompound(i);
            Bounty bounty = Bounty.fromNBT(bountyTag);

            if (!bounty.isExpired()) {
                manager.activeBounties.put(bounty.getTargetId(), bounty);
            }
        }

        return manager;
    }

    /**
     * Nettoie toutes les données (pour debug/reset)
     */
    public void clear() {
        activeBounties.clear();
        setDirty();
    }
}
