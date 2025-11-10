package fr.eriniumgroup.erinium_faction.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Bloc d'Erinium - Bloc de stockage
 */
public class EriniumBlockBlock extends Block {
    public EriniumBlockBlock() {
        super(BlockBehaviour.Properties.of()
            .strength(50.0F, 1200.0F) // Résistance similaire à la netherite
            .requiresCorrectToolForDrops()
            .sound(SoundType.NETHERITE_BLOCK));
    }
}
