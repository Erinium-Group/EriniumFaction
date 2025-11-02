package fr.eriniumgroup.erinium_faction.common.block;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class SilverOreBlock extends DropExperienceBlock {
    public SilverOreBlock() {
        super(UniformInt.of(0, 2), BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).strength(3.0F, 3.0F).requiresCorrectToolForDrops());
    }
}
