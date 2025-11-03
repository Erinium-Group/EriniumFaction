package fr.eriniumgroup.erinium_faction.common.block;

import fr.eriniumgroup.erinium_faction.common.block.entity.TitaniumCreativeBatteryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

public class TitaniumCreativeBatteryBlock extends Block implements EntityBlock {
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 4);

    public TitaniumCreativeBatteryBlock() {
        super(Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(3.0F, 6.0F).requiresCorrectToolForDrops());
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 4));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(LEVEL); }
    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new TitaniumCreativeBatteryBlockEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) { return (l,p,s,be) -> { if (be instanceof TitaniumCreativeBatteryBlockEntity batt) batt.onTick(); }; }
}
