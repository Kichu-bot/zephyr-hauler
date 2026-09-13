package com.zephyrhauler.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ZephyrCableBlock extends Block implements SimpleWaterloggedBlock {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN; // NUEVO: Para detectar el suelo
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape CORE_WITH_POLE = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 16.0D, 10.0D);
    private static final VoxelShape CORE_FLOATING = Block.box(6.0D, 12.0D, 6.0D, 10.0D, 16.0D, 10.0D); // Hitbox más corta

    private static final VoxelShape NORTH_SHAPE = Block.box(6.0D, 11.0D, 0.0D, 10.0D, 16.0D, 6.0D);
    private static final VoxelShape SOUTH_SHAPE = Block.box(6.0D, 11.0D, 10.0D, 10.0D, 16.0D, 16.0D);
    private static final VoxelShape WEST_SHAPE = Block.box(0.0D, 11.0D, 6.0D, 6.0D, 16.0D, 10.0D);
    private static final VoxelShape EAST_SHAPE = Block.box(10.0D, 11.0D, 6.0D, 16.0D, 16.0D, 10.0D);

    public ZephyrCableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false).setValue(EAST, false)
                .setValue(SOUTH, false).setValue(WEST, false)
                .setValue(DOWN, true).setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, DOWN, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = state.getValue(DOWN) ? CORE_WITH_POLE : CORE_FLOATING;

        if (state.getValue(NORTH)) shape = Shapes.or(shape, NORTH_SHAPE);
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, SOUTH_SHAPE);
        if (state.getValue(WEST)) shape = Shapes.or(shape, WEST_SHAPE);
        if (state.getValue(EAST)) shape = Shapes.or(shape, EAST_SHAPE);
        return shape;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        net.minecraft.world.level.Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        FluidState fluidstate = level.getFluidState(pos);

        return this.defaultBlockState()
                .setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER)
                .setValue(DOWN, this.canSupportCenter(level, pos.below()))
                .setValue(NORTH, this.canConnectTo(level, pos.north(), Direction.NORTH))
                .setValue(EAST, this.canConnectTo(level, pos.east(), Direction.EAST))
                .setValue(SOUTH, this.canConnectTo(level, pos.south(), Direction.SOUTH))
                .setValue(WEST, this.canConnectTo(level, pos.west(), Direction.WEST));
    }

    private boolean canSupportCenter(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        return block instanceof ZephyrCableBlock ||
                block instanceof ZephyrDockBlock ||
                block instanceof ZephyrHubBlock ||
                state.isFaceSturdy(level, pos, Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        if (direction == Direction.DOWN) {
            return state.setValue(DOWN, this.canSupportCenter(level, neighborPos));
        }

        if (direction.getAxis().isHorizontal()) {
            boolean canConnect = this.canConnectTo(level, neighborPos, direction);
            return switch (direction) {
                case NORTH -> state.setValue(NORTH, canConnect);
                case EAST -> state.setValue(EAST, canConnect);
                case SOUTH -> state.setValue(SOUTH, canConnect);
                case WEST -> state.setValue(WEST, canConnect);
                default -> state;
            };
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    private boolean canSupportCenter(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        return block instanceof ZephyrCableBlock ||
                block instanceof ZephyrDockBlock ||
                block instanceof ZephyrHubBlock ||
                state.isFaceSturdy(level, pos, Direction.UP);
    }

    private boolean canConnectTo(BlockGetter level, BlockPos pos, Direction dir) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        return block instanceof ZephyrCableBlock || block instanceof ZephyrDockBlock || block instanceof ZephyrHubBlock;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public void onPlace(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide && !state.is(oldState.getBlock())) {
            triggerHubScan(level, pos);
        }
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    public void onRemove(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            triggerHubScan(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void triggerHubScan(net.minecraft.world.level.Level level, BlockPos pos) {
        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-32, -32, -32), pos.offset(32, 32, 32))) {
            if (level.getBlockEntity(checkPos) instanceof com.zephyrhauler.block.entity.ZephyrHubBlockEntity hubBE) {
                hubBE.scanNetwork();
            }
        }
    }
}