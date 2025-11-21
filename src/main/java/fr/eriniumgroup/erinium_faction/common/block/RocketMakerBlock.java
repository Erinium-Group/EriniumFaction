package fr.eriniumgroup.erinium_faction.common.block;

import com.mojang.serialization.MapCodec;
import fr.eriniumgroup.erinium_faction.common.blockentity.RocketMakerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RocketMakerBlock extends BaseEntityBlock {
    public static final MapCodec<RocketMakerBlock> CODEC = simpleCodec(RocketMakerBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    // Hitbox simple 3x3x1
    private static final VoxelShape SHAPE = Shapes.box(-1, 0, -1, 2, 1, 2);

    @Override
    public MapCodec<RocketMakerBlock> codec() {
        return CODEC;
    }

    public RocketMakerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public RocketMakerBlock() {
        this(Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL)
                .strength(5.0F, 6.0F)
                .requiresCorrectToolForDrops());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        // Get player's horizontal direction
        Direction playerDirection = ctx.getHorizontalDirection();

        // Invert NORTH/SOUTH and EAST/WEST as requested by user
        Direction finalDirection = switch (playerDirection) {
            case NORTH -> Direction.SOUTH;
            case SOUTH -> Direction.NORTH;
            case EAST -> Direction.WEST;
            case WEST -> Direction.EAST;
            default -> playerDirection;
        };

        // Set opposite of player direction (like furnace behavior)
        return this.defaultBlockState().setValue(FACING, finalDirection.getOpposite());
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        Direction dir = state.getValue(FACING);
        Rotation rot = mirror.getRotation(dir);
        return state.setValue(FACING, rot.rotate(dir));
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new RocketMakerBlockEntity(pos, state);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof RocketMakerBlockEntity rocketMakerBE && player instanceof ServerPlayer serverPlayer) {
                // Utiliser openMenu sans vérification de distance car la GUI est très grande
                serverPlayer.openMenu(rocketMakerBE, buf -> buf.writeBlockPos(pos));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected boolean triggerEvent(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        BlockEntity blockentity = level.getBlockEntity(pos);
        return blockentity != null && blockentity.triggerEvent(id, param);
    }

    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof RocketMakerBlockEntity rocketMakerBE) {
                NonNullList<ItemStack> items = NonNullList.create();
                // Drop tous les slots de craft (0-62), pas le slot output (63)
                for (int i = 0; i < 63; i++) {
                    ItemStack stack = rocketMakerBE.getInventory().getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        items.add(stack);
                    }
                }
                net.minecraft.world.Containers.dropContents(level, pos, items);
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }
}
