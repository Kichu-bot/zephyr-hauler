package com.zephyrhauler.block;

import com.mojang.serialization.MapCodec;
import com.zephyrhauler.block.entity.WindSensorBlockEntity;
import com.zephyrhauler.util.ZephyrWindSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.component.DataComponents;
import org.jetbrains.annotations.Nullable;

public class WindSensorBlock extends BaseEntityBlock {
    public static final MapCodec<WindSensorBlock> CODEC = simpleCodec(WindSensorBlock::new);
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public WindSensorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, 0).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WindSensorBlockEntity(pos, state);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWER);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof WindSensorBlockEntity sensorBE) {
                CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                CompoundTag tag = customData.copyTag();

                if (tag.contains("LinkedDockPos")) {
                    long dockPosLong = tag.getLong("LinkedDockPos");
                    String dockName = tag.getString("LinkedDockName");
                    String dockDim = tag.getString("LinkedDockDim");

                    sensorBE.linkToDock(BlockPos.of(dockPosLong), dockName, dockDim);

                    if (placer instanceof Player player) {
                        player.displayClientMessage(
                                Component.translatable("message.zephyr_hauler.sensor.placed_linked")
                                        .withStyle(ChatFormatting.AQUA), true
                        );
                    }
                }
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof WindSensorBlockEntity sensorBE) {
                if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty() && sensorBE.isLinked()) {
                    sensorBE.unlink();
                    player.displayClientMessage(Component.translatable("message.zephyr_hauler.sensor_unlinked").withStyle(ChatFormatting.YELLOW), true);
                    return InteractionResult.SUCCESS;
                }

                player.openMenu(sensorBE, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, 20);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof WindSensorBlockEntity sensorBE) {

            ZephyrWindSystem.WindInfo wind = ZephyrWindSystem.getCurrentWind(level);
            int currentPower = state.getValue(POWER);
            int newPower = 0;

            if (wind.direction() == sensorBE.getTargetDirection()) {
                newPower = sensorBE.getConfiguredPower();
            }

            if (currentPower != newPower) {
                level.setBlock(pos, state.setValue(POWER, newPower), 3);
                level.updateNeighborsAt(pos, this);
            }
        }
        level.scheduleTick(pos, this, 20);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, com.zephyrhauler.registry.ModBlockEntities.WIND_SENSOR_BE.get(),
                (lvl, pos, st, be) -> be.tick());
    }
}