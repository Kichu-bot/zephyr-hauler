package com.zephyrhauler.block;

import com.mojang.serialization.MapCodec;
import com.zephyrhauler.block.entity.ZephyrLauncherBlockEntity;
import com.zephyrhauler.entity.ZephyrHaulerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ZephyrLauncherBlock extends BaseEntityBlock {
    public static final MapCodec<ZephyrLauncherBlock> CODEC = simpleCodec(ZephyrLauncherBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public ZephyrLauncherBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ZephyrLauncherBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof ZephyrLauncherBlockEntity launcherBE) {
                player.openMenu(launcherBE, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean hasSignal = level.hasNeighborSignal(pos);
            if (hasSignal && !state.getValue(POWERED)) {
                triggerLaunch(level, pos);
                level.setBlock(pos, state.setValue(POWERED, true), 3);
            } else if (!hasSignal && state.getValue(POWERED)) {
                level.setBlock(pos, state.setValue(POWERED, false), 3);
            }
        }
    }

    private void triggerLaunch(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ZephyrLauncherBlockEntity launcherBE)) return;

        ItemStack fuelStack = launcherBE.inventory.getStackInSlot(0);
        float speedMult = 0.5f;
        int fuelTier = 0;

        if (!fuelStack.isEmpty()) {
            if (fuelStack.is(Items.COAL) || fuelStack.is(Items.CHARCOAL) || fuelStack.is(Items.DRIED_KELP_BLOCK)) {
                speedMult = 1.0f; fuelTier = 1;
            } else if (fuelStack.is(Items.BLAZE_POWDER) || fuelStack.is(Items.MAGMA_CREAM) || fuelStack.is(Items.COAL_BLOCK)) {
                speedMult = 1.5f; fuelTier = 2;
            } else if (fuelStack.is(Items.SOUL_SAND) || fuelStack.is(Items.SOUL_SOIL) || fuelStack.is(Items.SOUL_CAMPFIRE)) {
                speedMult = 2.0f; fuelTier = 3;
            } else if (fuelStack.is(Items.WIND_CHARGE) || fuelStack.is(Items.GUNPOWDER)) {
                speedMult = 3.0f; fuelTier = 4;
            }
        }

        AABB searchBox = new AABB(pos.above());
        List<ZephyrHaulerEntity> haulers = level.getEntitiesOfClass(ZephyrHaulerEntity.class, searchBox);

        for (ZephyrHaulerEntity hauler : haulers) {
            if (hauler.triggerRedstoneLaunch(fuelTier, speedMult)) {
                if (fuelTier > 0) {
                    launcherBE.inventory.extractItem(0, 1, false);
                }
            }
        }
    }
}