package fr.eriniumgroup.erinium_faction.player.level;

import fr.eriniumgroup.erinium_faction.EriniumFaction;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Gestionnaire du système de niveau des joueurs
 */
public class PlayerLevelManager {

    private static final ResourceLocation HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(EriniumFaction.MODID, "base_health");
    private static final ResourceLocation ARMOR_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(EriniumFaction.MODID, "level_armor");
    private static final ResourceLocation SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(EriniumFaction.MODID, "level_speed");
    private static final ResourceLocation ATTACK_DAMAGE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(EriniumFaction.MODID, "level_strength");
    private static final ResourceLocation LUCK_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(EriniumFaction.MODID, "level_luck");

    /**
     * Obtient les données de niveau d'un joueur
     */
    public static PlayerLevelData getLevelData(ServerPlayer player) {
        return player.getData(PlayerLevelAttachments.PLAYER_LEVEL_DATA);
    }

    /**
     * Définit le niveau d'un joueur (système sans XP)
     */
    public static void setLevel(ServerPlayer player, int newLevel) {
        PlayerLevelData data = getLevelData(player);
        int oldLevel = data.getLevel();

        if (newLevel < 1) newLevel = 1;
        if (newLevel > PlayerLevelConfig.MAX_LEVEL.get()) newLevel = PlayerLevelConfig.MAX_LEVEL.get();

        data.setLevel(newLevel);

        // Ajouter des points d'attributs pour chaque niveau gagné
        if (newLevel > oldLevel) {
            int levelsGained = newLevel - oldLevel;
            data.setAvailablePoints(data.getAvailablePoints() + (levelsGained * PlayerLevelConfig.POINTS_PER_LEVEL.get()));

            // Notifier le joueur
            player.sendSystemMessage(Component.translatable("player_level.level_up", data.getLevel())
                .withStyle(style -> style.withColor(0xFFAA00).withBold(true)));
            player.sendSystemMessage(Component.translatable("player_level.points_gained", levelsGained * PlayerLevelConfig.POINTS_PER_LEVEL.get())
                .withStyle(style -> style.withColor(0x55FF55)));

            EFC.log.info("Player " + player.getName().getString() + " leveled up to " + data.getLevel());
        }

        // Mettre à jour les attributs en fonction du nouveau niveau
        updatePlayerAttributes(player, data);
    }


    /**
     * Distribue un point d'attribut
     */
    public static boolean distributePoint(ServerPlayer player, AttributeType attributeType) {
        PlayerLevelData data = getLevelData(player);

        if (data.getAvailablePoints() <= 0) {
            player.sendSystemMessage(Component.translatable("player_level.no_points")
                .withStyle(style -> style.withColor(0xFF5555)));
            return false;
        }

        // Ajouter le point à l'attribut sélectionné
        switch (attributeType) {
            case HEALTH -> data.setHealthPoints(data.getHealthPoints() + 1);
            case ARMOR -> data.setArmorPoints(data.getArmorPoints() + 1);
            case SPEED -> data.setSpeedPoints(data.getSpeedPoints() + 1);
            case INTELLIGENCE -> data.setIntelligencePoints(data.getIntelligencePoints() + 1);
            case STRENGTH -> data.setStrengthPoints(data.getStrengthPoints() + 1);
            case LUCK -> data.setLuckPoints(data.getLuckPoints() + 1);
        }

        data.setAvailablePoints(data.getAvailablePoints() - 1);

        // Mettre à jour les attributs du joueur
        updatePlayerAttributes(player, data);

        player.sendSystemMessage(Component.translatable("player_level.command.distribute.success",
            Component.translatable("player_level.attribute." + attributeType.name().toLowerCase()))
            .withStyle(style -> style.withColor(0x55FF55)));

        return true;
    }

    /**
     * Réinitialise les points d'attributs (nécessite un token)
     */
    public static boolean resetAttributes(ServerPlayer player) {
        // Vérifier si le joueur a un token de réinitialisation
        if (!hasResetToken(player)) {
            player.sendSystemMessage(Component.translatable("player_level.reset.need_token")
                .withStyle(style -> style.withColor(0xFF5555)));
            return false;
        }

        PlayerLevelData data = getLevelData(player);

        int totalPoints = data.getTotalPointsSpent();

        data.setHealthPoints(0);
        data.setArmorPoints(0);
        data.setSpeedPoints(0);
        data.setIntelligencePoints(0);
        data.setStrengthPoints(0);
        data.setLuckPoints(0);
        data.setAvailablePoints(data.getAvailablePoints() + totalPoints);

        // Mettre à jour les attributs
        updatePlayerAttributes(player, data);

        player.sendSystemMessage(Component.translatable("player_level.attributes_reset")
            .withStyle(style -> style.withColor(0xFFFF55)));

        return true;
    }

    /**
     * Vérifie si le joueur a un token de réinitialisation dans son inventaire
     */
    public static boolean hasResetToken(ServerPlayer player) {
        return player.getInventory().contains(new net.minecraft.world.item.ItemStack(
            fr.eriniumgroup.erinium_faction.init.EFItems.STATS_RESET_TOKEN.get()));
    }

    /**
     * Met à jour tous les attributs du joueur
     */
    public static void updatePlayerAttributes(ServerPlayer player, PlayerLevelData data) {
        // Santé de base + santé par niveau + santé par points
        double baseHealth = PlayerLevelConfig.BASE_HEALTH.get();
        double levelHearts = ((double)data.getLevel() / PlayerLevelConfig.LEVELS_BETWEEN_HEARTS.get()) * PlayerLevelConfig.HEARTS_PER_LEVEL_GAIN.get();
        double pointHealth = data.getHealthPoints() * PlayerLevelConfig.HEALTH_BONUS_PER_POINT.get();
        double totalHealth = baseHealth + levelHearts + pointHealth;

        // Retirer les anciens modificateurs
        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(HEALTH_MODIFIER_ID);
            healthAttr.addPermanentModifier(new AttributeModifier(HEALTH_MODIFIER_ID, totalHealth - 20.0, // 20 est la santé par défaut
                    AttributeModifier.Operation.ADD_VALUE));
        }

        // Armure
        double armorBonus = data.getArmorPoints() * PlayerLevelConfig.ARMOR_BONUS_PER_POINT.get();
        var armorAttr = player.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) {
            armorAttr.removeModifier(ARMOR_MODIFIER_ID);
            if (armorBonus > 0) {
                armorAttr.addPermanentModifier(new AttributeModifier(ARMOR_MODIFIER_ID, armorBonus, AttributeModifier.Operation.ADD_VALUE));
            }
        }

        // Vitesse
        double speedBonus = data.getSpeedPoints() * PlayerLevelConfig.SPEED_BONUS_PER_POINT.get();
        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(SPEED_MODIFIER_ID);
            if (speedBonus > 0) {
                speedAttr.addPermanentModifier(new AttributeModifier(SPEED_MODIFIER_ID, speedBonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }

        // Force (dégâts d'attaque)
        double strengthBonus = data.getStrengthPoints() * PlayerLevelConfig.STRENGTH_BONUS_PER_POINT.get();
        var attackAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null) {
            attackAttr.removeModifier(ATTACK_DAMAGE_MODIFIER_ID);
            if (strengthBonus > 0) {
                attackAttr.addPermanentModifier(new AttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, strengthBonus, AttributeModifier.Operation.ADD_VALUE));
            }
        }

        // Chance
        double luckBonus = data.getLuckPoints() * PlayerLevelConfig.LUCK_BONUS_PER_POINT.get();
        var luckAttr = player.getAttribute(Attributes.LUCK);
        if (luckAttr != null) {
            luckAttr.removeModifier(LUCK_MODIFIER_ID);
            if (luckBonus > 0) {
                luckAttr.addPermanentModifier(new AttributeModifier(LUCK_MODIFIER_ID, luckBonus, AttributeModifier.Operation.ADD_VALUE));
            }
        }

        // Guérir le joueur pour qu'il ne meure pas si la santé max diminue
        player.setHealth(player.getMaxHealth());
    }

    /**
     * Initialise les attributs d'un nouveau joueur
     */
    public static void initializePlayer(ServerPlayer player) {
        PlayerLevelData data = getLevelData(player);

        // Appliquer les attributs de base
        updatePlayerAttributes(player, data);
    }

    /**
     * Types d'attributs disponibles
     */
    public enum AttributeType {
        HEALTH("Vie"), ARMOR("Armure"), SPEED("Vitesse"), INTELLIGENCE("Intelligence"), STRENGTH("Force"), LUCK("Chance");

        private final String displayName;

        AttributeType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}

