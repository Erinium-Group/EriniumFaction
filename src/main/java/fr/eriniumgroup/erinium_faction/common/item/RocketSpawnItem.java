package fr.eriniumgroup.erinium_faction.common.item;

import fr.eriniumgroup.erinium_faction.common.entity.RocketEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Item pour invoquer une fusée
 */
public class RocketSpawnItem extends Item {
    public RocketSpawnItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockPos spawnPos = pos.relative(direction);

        // Créer et spawner la fusée
        RocketEntity rocket = new RocketEntity(level, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        rocket.setYRot(context.getPlayer() != null ? context.getPlayer().getYRot() : 0.0F);

        if (level.addFreshEntity(rocket)) {
            // Réduire la stack si le joueur n'est pas en créatif
            if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
            return InteractionResult.CONSUME;
        }

        return InteractionResult.FAIL;
    }
}
