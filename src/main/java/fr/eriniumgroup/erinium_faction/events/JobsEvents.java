package fr.eriniumgroup.erinium_faction.events;

import fr.eriniumgroup.erinium_faction.common.config.JobConfig;
import fr.eriniumgroup.erinium_faction.common.config.JobsConfigManager;
import fr.eriniumgroup.erinium_faction.common.network.packets.JobsPacketHandler;
import fr.eriniumgroup.erinium_faction.features.jobs.JobsManager;
import fr.eriniumgroup.erinium_faction.features.jobs.type.ActionType;
import fr.eriniumgroup.erinium_faction.features.jobs.type.JobType;
import fr.eriniumgroup.erinium_faction.features.jobs.type.XpEarningEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Gestionnaire d'événements pour le système de métiers
 * Gère l'attribution d'XP pour toutes les actions possibles
 * Un même événement peut donner de l'XP à plusieurs métiers différents
 */
@EventBusSubscriber
public class JobsEvents {

    // Track des blocs placés par les joueurs (par monde)
    private static final Map<ServerLevel, Set<BlockPos>> PLAYER_PLACED_BLOCKS = new WeakHashMap<>();

    /**
     * Synchronise les données de métiers quand un joueur se connecte
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            JobsPacketHandler.syncJobsData(player);
            JobsPacketHandler.syncJobsConfig(player); // Sync config aussi
        }
    }

    /**
     * Synchronise les données de métiers quand un joueur change de dimension
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            JobsPacketHandler.syncJobsData(player);
        }
    }

    /**
     * Synchronise les données de métiers quand un joueur respawn
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            JobsPacketHandler.syncJobsData(player);
        }
    }

    /**
     * Gère l'XP quand un joueur casse un bloc
     * Vérifie tous les métiers pour voir si l'action donne de l'XP
     * Ne donne pas d'XP si le bloc a été placé par un joueur
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        BlockPos pos = event.getPos();
        ServerLevel level = (ServerLevel) player.level();
        Block block = event.getState().getBlock();
        BlockState state = event.getState();
        String blockId = BuiltInRegistries.BLOCK.getKey(block).toString();
        String blockName = block.getName().getString();

        // Vérifier si le bloc a été placé par un joueur
        Set<BlockPos> placedBlocks = PLAYER_PLACED_BLOCKS.get(level);
        if (placedBlocks != null && placedBlocks.contains(pos)) {
            // Retirer de la liste et ne pas donner d'XP
            placedBlocks.remove(pos);
            return;
        }

        // Pour les crops, vérifier l'âge
        if (block instanceof CropBlock cropBlock) {
            // Utiliser isMaxAge() qui est public
            if (!cropBlock.isMaxAge(state)) {
                // La plante n'est pas mature, pas d'XP
                return;
            }
        }

        // Vérifier tous les métiers
        for (JobType jobType : JobType.values()) {
            JobConfig config = JobsConfigManager.getConfig(jobType);
            int playerLevel = JobsManager.getJobLevel(player, jobType);

            // Chercher une entrée XP correspondante
            for (XpEarningEntry entry : config.getXpEarning()) {
                if (entry.getActionType() == ActionType.BREAK && entry.getTargetId().equals(blockId) && isLevelInRange(playerLevel, entry.getMinLevel(), entry.getMaxLevel())) {

                    // Ajouter l'XP pour ce métier avec description
                    JobsManager.addJobExperience(player, jobType, entry.getXpEarned(), "Mined " + blockName);
                }
            }
        }
    }

    /**
     * Gère l'XP quand un joueur place un bloc
     * Track le bloc placé pour éviter l'XP infini
     * SAUF pour les crops qui peuvent être récoltés
     */
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        BlockPos pos = event.getPos();
        ServerLevel level = (ServerLevel) player.level();
        Block block = event.getPlacedBlock().getBlock();
        String blockId = BuiltInRegistries.BLOCK.getKey(block).toString();
        String blockName = block.getName().getString();

        // Ajouter le bloc à la liste des blocs placés par un joueur
        // SAUF si c'est un crop (qui peut être récolté)
        if (!(block instanceof CropBlock)) {
            PLAYER_PLACED_BLOCKS.computeIfAbsent(level, k -> new HashSet<>()).add(pos);
        }

        // Vérifier tous les métiers
        for (JobType jobType : JobType.values()) {
            JobConfig config = JobsConfigManager.getConfig(jobType);
            int playerLevel = JobsManager.getJobLevel(player, jobType);

            for (XpEarningEntry entry : config.getXpEarning()) {
                if (entry.getActionType() == ActionType.PLACE && entry.getTargetId().equals(blockId) && isLevelInRange(playerLevel, entry.getMinLevel(), entry.getMaxLevel())) {

                    JobsManager.addJobExperience(player, jobType, entry.getXpEarned(), "Placed " + blockName);
                }
            }
        }
    }

    /**
     * Gère l'XP quand un joueur tue une entité
     */
    @SubscribeEvent
    public static void onEntityKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        LivingEntity killed = event.getEntity();
        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(killed.getType()).toString();
        String entityName = killed.getName().getString();

        // Vérifier tous les métiers
        for (JobType jobType : JobType.values()) {
            JobConfig config = JobsConfigManager.getConfig(jobType);
            int playerLevel = JobsManager.getJobLevel(player, jobType);

            for (XpEarningEntry entry : config.getXpEarning()) {
                if (entry.getActionType() == ActionType.KILL && entry.getTargetId().equals(entityId) && isLevelInRange(playerLevel, entry.getMinLevel(), entry.getMaxLevel())) {

                    JobsManager.addJobExperience(player, jobType, entry.getXpEarned(), "Killed " + entityName);
                }
            }
        }
    }

    /**
     * Gère l'XP quand un joueur pêche un item
     */
    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Parcourir tous les items pêchés
        for (ItemStack stack : event.getDrops()) {
            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            String itemName = stack.getHoverName().getString();

            // Vérifier tous les métiers
            for (JobType jobType : JobType.values()) {
                JobConfig config = JobsConfigManager.getConfig(jobType);
                int playerLevel = JobsManager.getJobLevel(player, jobType);

                for (XpEarningEntry entry : config.getXpEarning()) {
                    if (entry.getActionType() == ActionType.FISHING && entry.getTargetId().equals(itemId) && isLevelInRange(playerLevel, entry.getMinLevel(), entry.getMaxLevel())) {

                        JobsManager.addJobExperience(player, jobType, entry.getXpEarned() * stack.getCount(), "Caught " + itemName);
                    }
                }
            }
        }
    }

    /**
     * Gère l'XP quand un joueur craft un item
     */
    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack crafted = event.getCrafting();
        String itemId = BuiltInRegistries.ITEM.getKey(crafted.getItem()).toString();
        String itemName = crafted.getHoverName().getString();

        // Vérifier tous les métiers
        for (JobType jobType : JobType.values()) {
            JobConfig config = JobsConfigManager.getConfig(jobType);
            int playerLevel = JobsManager.getJobLevel(player, jobType);

            for (XpEarningEntry entry : config.getXpEarning()) {
                if (entry.getActionType() == ActionType.CRAFT && entry.getTargetId().equals(itemId) && isLevelInRange(playerLevel, entry.getMinLevel(), entry.getMaxLevel())) {

                    JobsManager.addJobExperience(player, jobType, entry.getXpEarned() * crafted.getCount(), "Crafted " + itemName);
                }
            }
        }
    }

    /**
     * Gère l'XP quand un joueur smelt un item
     */
    @SubscribeEvent
    public static void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack smelted = event.getSmelting();
        String itemId = BuiltInRegistries.ITEM.getKey(smelted.getItem()).toString();
        String itemName = smelted.getHoverName().getString();

        // Vérifier tous les métiers
        for (JobType jobType : JobType.values()) {
            JobConfig config = JobsConfigManager.getConfig(jobType);
            int playerLevel = JobsManager.getJobLevel(player, jobType);

            for (XpEarningEntry entry : config.getXpEarning()) {
                if (entry.getActionType() == ActionType.SMELT && entry.getTargetId().equals(itemId) && isLevelInRange(playerLevel, entry.getMinLevel(), entry.getMaxLevel())) {

                    JobsManager.addJobExperience(player, jobType, entry.getXpEarned() * smelted.getCount(), "Smelted " + itemName);
                }
            }
        }
    }

    /**
     * Gère l'XP quand un joueur finit d'utiliser un item (manger, boire, utiliser)
     */
    @SubscribeEvent
    public static void onItemUsed(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack used = event.getItem();
        String itemId = BuiltInRegistries.ITEM.getKey(used.getItem()).toString();
        String itemName = used.getHoverName().getString();

        // Déterminer le type d'action basé sur les propriétés de l'item
        ActionType actionType = determineItemActionType(used);
        String actionVerb = switch (actionType) {
            case EAT -> "Ate";
            case DRINK -> "Drank";
            default -> "Used";
        };

        // Vérifier tous les métiers
        for (JobType jobType : JobType.values()) {
            JobConfig config = JobsConfigManager.getConfig(jobType);
            int playerLevel = JobsManager.getJobLevel(player, jobType);

            for (XpEarningEntry entry : config.getXpEarning()) {
                // Vérifier si l'item correspond à EAT, DRINK, USE ou OTHER
                if ((entry.getActionType() == actionType || entry.getActionType() == ActionType.OTHER) && entry.getTargetId().equals(itemId) && isLevelInRange(playerLevel, entry.getMinLevel(), entry.getMaxLevel())) {

                    JobsManager.addJobExperience(player, jobType, entry.getXpEarned(), actionVerb + " " + itemName);
                }
            }
        }
    }

    /**
     * Gère l'XP quand un joueur lance un projectile (bouteille d'XP, potion, etc.)
     */
    @SubscribeEvent
    public static void onProjectileThrown(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack thrownItem = event.getItemStack();

        // Vérifier si c'est une bouteille d'XP ou une potion splash
        if (thrownItem.getItem() == Items.EXPERIENCE_BOTTLE || thrownItem.getItem().toString().contains("splash_potion") || thrownItem.getItem().toString().contains("lingering_potion")) {

            String itemId = BuiltInRegistries.ITEM.getKey(thrownItem.getItem()).toString();
            String itemName = thrownItem.getHoverName().getString();

            // Vérifier tous les métiers pour l'action THROW
            for (JobType jobType : JobType.values()) {
                JobConfig config = JobsConfigManager.getConfig(jobType);
                int playerLevel = JobsManager.getJobLevel(player, jobType);

                for (XpEarningEntry entry : config.getXpEarning()) {
                    if (entry.getActionType() == ActionType.THROW && entry.getTargetId().equals(itemId) && isLevelInRange(playerLevel, entry.getMinLevel(), entry.getMaxLevel())) {

                        JobsManager.addJobExperience(player, jobType, entry.getXpEarned(), "Threw " + itemName);
                    }
                }
            }
        }
    }

    /**
     * Détermine le type d'action basé sur l'item utilisé
     */
    private static ActionType determineItemActionType(ItemStack stack) {
        // Vérifier si c'est un item comestible
        if (stack.getFoodProperties(null) != null) {
            // Dans Minecraft, les potions utilisent DRINK, les aliments utilisent EAT
            String itemName = stack.getItem().toString();
            if (itemName.contains("potion") || itemName.contains("bottle") || itemName.contains("milk")) {
                return ActionType.DRINK;
            }
            return ActionType.EAT;
        }

        // Sinon c'est un item à utiliser (bouclier, arc, etc.)
        return ActionType.USE;
    }

    /**
     * Vérifie si le niveau du joueur est dans la plage requise
     *
     * @param playerLevel Niveau actuel du joueur
     * @param minLevel    Niveau minimum (-1 = pas de minimum)
     * @param maxLevel    Niveau maximum (-1 = pas de maximum)
     * @return true si le niveau est dans la plage
     */
    private static boolean isLevelInRange(int playerLevel, int minLevel, int maxLevel) {
        if (minLevel != -1 && playerLevel < minLevel) {
            return false;
        }
        if (maxLevel != -1 && playerLevel > maxLevel) {
            return false;
        }
        return true;
    }
}
