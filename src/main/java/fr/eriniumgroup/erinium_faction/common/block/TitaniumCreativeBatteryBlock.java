package fr.eriniumgroup.erinium_faction.common.block;

import fr.eriniumgroup.erinium_faction.common.block.entity.TitaniumCreativeBatteryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
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
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) { return RenderShape.MODEL; }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TitaniumCreativeBatteryBlockEntity battery && player instanceof ServerPlayer sp) {
            sp.openMenu(battery, buf -> buf.writeBlockPos(pos));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new TitaniumCreativeBatteryBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return level.isClientSide ? null : (l, p, s, be) -> {
            if (be instanceof TitaniumCreativeBatteryBlockEntity batt) batt.onTick();
        };
    }
}
