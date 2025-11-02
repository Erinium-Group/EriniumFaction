package fr.eriniumgroup.erinium_faction.common.item;

import fr.eriniumgroup.erinium_faction.features.level.PlayerLevelManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Item consommable pour réinitialiser les attributs du joueur
 */
public class StatsResetTokenItem extends Item {

    public StatsResetTokenItem(Properties properties) {
        super(properties);
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            // Réinitialiser les attributs (le token sera consommé dans la méthode resetAttributes)
            boolean success = PlayerLevelManager.resetAttributes(serverPlayer);

            if (success) {
                return InteractionResultHolder.success(stack);
            } else {
                return InteractionResultHolder.fail(stack);
            }
        }

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nonnull TooltipContext context, @Nonnull List<Component> tooltipComponents, @Nonnull TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.erinium_faction.stats_reset_token.tooltip").withStyle(style -> style.withColor(0xFFAA00)));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}