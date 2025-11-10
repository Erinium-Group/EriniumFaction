package fr.eriniumgroup.erinium_faction.common.item.armor;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Armure Erinium avec effets spéciaux
 * - Helmet: Night Vision
 * - Chestplate: Haste II
 * - Leggings: Speed II
 * - Boots: Aucun effet
 */
public class EriniumArmorItem extends ArmorItem {

    public EriniumArmorItem(ArmorItem.Type type, Properties properties) {
        super(Holder.direct(EriniumArmorMaterial.ERINIUM), type, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide() && entity instanceof Player player) {
            // Vérifier que l'item est équipé
            ItemStack equippedItem = player.getItemBySlot(this.getType().getSlot());
            if (equippedItem == stack) {
                applyArmorEffect(player);
            }
        }
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    /**
     * Applique l'effet correspondant à la pièce d'armure
     */
    private void applyArmorEffect(Player player) {
        // Appliquer l'effet pendant 10 secondes (200 ticks) avec une vérification toutes les 100 ticks
        // Cela évite de spam les effets
        if (player.tickCount % 100 == 0) {
            switch (this.getType()) {
                case HELMET:
                    // Night Vision
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 240, 0, false, false, true));
                    break;
                case CHESTPLATE:
                    // Haste II
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 240, 1, false, false, true));
                    break;
                case LEGGINGS:
                    // Speed II
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 1, false, false, true));
                    break;
                case BOOTS:
                    // Pas d'effet pour les bottes
                    break;
                default:
                    break;
            }
        }
    }
}
