package fr.eriniumgroup.erinium_faction.common.block;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;

public class DeepslateSilverOreBlock extends Block {
    public DeepslateSilverOreBlock() {
        super(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .sound(SoundType.DEEPSLATE)
                .strength(4.5F, 3.0F)
                .requiresCorrectToolForDrops()
        );
    }
}

