package fr.eriniumgroup.erinium_faction.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class TitaniumBlockBlock extends Block {
    public TitaniumBlockBlock() {
        super(Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(5.0F, 6.0F).requiresCorrectToolForDrops());
    }
}

