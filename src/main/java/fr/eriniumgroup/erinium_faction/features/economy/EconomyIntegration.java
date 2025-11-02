package fr.eriniumgroup.erinium_faction.features.economy;

import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
 * API d’intégration Économie du mod.
 * <p>
 * - Stocke le solde des joueurs via un Attachment NeoForge (persisté dans players.dat).
 * - Expose des méthodes utilitaires (get/set/deposit/withdraw) avec arrondi à 2 décimales.
 * - Synchronise un registre global (SavedData) pour permettre un classement hors-ligne.
 *
 * @author dragclover@gmail.com (structure initiale), Blaackknight <dragclover@gmail.com>
 */
public class EconomyIntegration {
    /**
     * Registre des types d’attachments du mod.
     */
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, EFC.MODID);
    /**
     * Attachment contenant le solde du joueur (persisté dans players.dat).
     */
    public static final Supplier<AttachmentType<PlayerEconomy>> PLAYER_ECONOMY = ATTACHMENTS.register("player_economy", () -> AttachmentType.serializable(PlayerEconomy::new).build());

    /**
     * Données d’économie stockées sur chaque joueur (attachment).
     *
     * @author Blaackknight <dragclover@gmail.com>
     */
    public static class PlayerEconomy implements INBTSerializable<CompoundTag> {
        private double balance = 0.0;

        /**
         * Retourne le solde courant.
         * @return solde en unités monétaires.
         */
        public double getBalance() { return balance; }

        /**
         * Définit le solde (valeur négative clampée à 0) et applique un arrondi à 2 décimales (DOWN).
         * @param v nouvelle valeur
         */
        public void setBalance(double v) { balance = Math.max(0.0, round2(v)); }

        /**
         * Ajoute un montant positif au solde (valeurs <= 0 ignorées) avec arrondi.
         * @param v montant à ajouter
         */
        public void add(double v) { if (v <= 0) return; balance = round2(balance + v); }

        /**
         * Tente de retirer un montant (rejet si <=0 ou fonds insuffisants, tolérance 1e-9), avec arrondi.
         * @param v montant à retirer
         * @return true si succès
         */
        public boolean take(double v) {
            if (v <= 0) return false;
            if (balance + 1e-9 < v) return false;
            balance = round2(balance - v);
            return true;
        }

        /**
         * Sérialisation NBT de l’attachment.
         */
        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("bal", balance);
            return tag;
        }

        /**
         * Désérialisation NBT de l’attachment.
         */
        @Override
        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
            balance = nbt.getDouble("bal");
        }
    }

    // API utilitaire ---------------------------------------------------------

    /**
     * Récupère l’attachment économie du joueur.
     * @param player joueur cible
     * @return PlayerEconomy (jamais null)
     */
    public static PlayerEconomy get(ServerPlayer player) {
        return player.getData(PLAYER_ECONOMY);
    }

    /**
     * Retourne le solde actuel d’un joueur.
     * @param player joueur
     * @return solde courant
     */
    public static double getBalance(ServerPlayer player) {
        return get(player).getBalance();
    }

    /**
     * Définit le solde d’un joueur et synchronise le registre global (classement).
     * @param player joueur
     * @param value valeur à définir
     */
    public static void setBalance(ServerPlayer player, double value) {
        get(player).setBalance(value);
        syncLedger(player);
    }

    /**
     * Dépose un montant sur le joueur et met à jour le registre global.
     * @param player joueur
     * @param value montant positif
     */
    public static void deposit(ServerPlayer player, double value) {
        get(player).add(value);
        syncLedger(player);
    }

    /**
     * Retire un montant si possible, et synchronise le registre global si succès.
     * @param player joueur
     * @param value montant à retirer
     * @return true si succès
     */
    public static boolean withdraw(ServerPlayer player, double value) {
        boolean ok = get(player).take(value);
        if (ok) syncLedger(player);
        return ok;
    }

    /**
     * Arrondi en 2 décimales (RoundingMode.DOWN).
     * @param v valeur
     * @return valeur arrondie
     */
    public static double round2(double v) {
        return new BigDecimal(v).setScale(2, RoundingMode.DOWN).doubleValue();
    }

    // Ledger global ----------------------------------------------------------

    /**
     * Synchronise l’entrée du joueur dans le registre global persistant (classement offline).
     * @param player joueur
     */
    private static void syncLedger(ServerPlayer player) {
        MinecraftServer server = player.server;
        EconomySavedData data = EconomySavedData.get(server);
        data.update(player.getUUID(), player.getGameProfile().getName(), getBalance(player));
    }

    /**
     * Retourne la liste triée des entrées (tous joueurs connus, online/offline) par solde décroissant.
     * @param server serveur
     * @return liste triée (copie immuable)
     */
    public static List<EconomySavedData.PlayerEntry> getTop(MinecraftServer server) {
        var list = List.copyOf(EconomySavedData.get(server).entries());
        return list.stream().sorted(Comparator.comparingDouble((EconomySavedData.PlayerEntry e) -> e.balance).reversed()).toList();
    }
}
