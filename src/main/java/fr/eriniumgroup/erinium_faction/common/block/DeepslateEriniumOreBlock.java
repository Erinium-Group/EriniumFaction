package fr.eriniumgroup.erinium_faction.common.block;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Minerai d'Erinium deepslate
 */
public class DeepslateEriniumOreBlock extends DropExperienceBlock {
    public DeepslateEriniumOreBlock() {
        super(UniformInt.of(3, 7), BlockBehaviour.Properties.of()
            .strength(4.5F, 3.0F)
            .requiresCorrectToolForDrops()
            .sound(SoundType.DEEPSLATE));
    }
}
