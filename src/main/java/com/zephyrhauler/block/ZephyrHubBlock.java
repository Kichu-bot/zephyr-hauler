package com.zephyrhauler.block;

import com.mojang.serialization.MapCodec;
import com.zephyrhauler.block.entity.ZephyrDockBlockEntity;
import com.zephyrhauler.block.entity.ZephyrHubBlockEntity;
import com.zephyrhauler.client.ZephyrClientHooks;
import com.zephyrhauler.entity.ZephyrHaulerEntity;
import com.zephyrhauler.network.HubMonitorPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ZephyrHubBlock extends BaseEntityBlock {
    public static final MapCodec<ZephyrHubBlock> CODEC = simpleCodec(ZephyrHubBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ZephyrHubBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
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
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ZephyrHubBlockEntity(pos, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide && !state.is(oldState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ZephyrHubBlockEntity hubBE) {
                hubBE.scanNetwork();
            }
            triggerNetworkUpdate(level, pos);
        }
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            triggerNetworkUpdate(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void triggerNetworkUpdate(Level level, BlockPos pos) {
        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-32, -32, -32), pos.offset(32, 32, 32))) {
            if (level.getBlockEntity(checkPos) instanceof ZephyrHubBlockEntity hubBE && !checkPos.equals(pos)) {
                hubBE.scanNetwork();
            }
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.isClientSide() && placer instanceof Player) {
            BlockEntity be = level.getBlockEntity(pos);
            String currentName = (be instanceof ZephyrHubBlockEntity hubBE) ? hubBE.getCustomName() : "";
            ZephyrClientHooks.openHubNaming(pos, currentName);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ZephyrHubBlockEntity hubBE) {

            if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {
                if (!level.isClientSide) {
                    List<HubMonitorPayload.DockInfo> dockList = new ArrayList<>();

                    for (BlockPos dPos : hubBE.getConnectedDocks()) {

                        if (ZephyrHaulerEntity.isAirspaceObstructed(level, dPos)) {
                            dockList.add(new HubMonitorPayload.DockInfo(dPos, "Zephyr Dock", 3));
                            continue;
                        }

                        BlockState dState = level.getBlockState(dPos);
                        if (dState.getBlock() instanceof ZephyrDockBlock) {
                            BlockEntity dockBe = level.getBlockEntity(dPos);
                            if (dockBe instanceof ZephyrDockBlockEntity dockEntity) {

                                boolean physicallyOccupied = !level.getEntitiesOfClass(
                                        ZephyrHaulerEntity.class,
                                        new AABB(dPos.above()).inflate(0.1)
                                ).isEmpty();

                                int status = 0;
                                if (dockEntity.isOccupied() || physicallyOccupied) {
                                    status = 2;
                                } else if (dState.hasProperty(ZephyrDockBlock.RESERVED) && dState.getValue(ZephyrDockBlock.RESERVED)) {
                                    status = 1;
                                }

                                String dName = dockEntity.getCustomName().isEmpty() ? "Zephyr Dock" : dockEntity.getCustomName();
                                dockList.add(new HubMonitorPayload.DockInfo(dPos, dName, status));
                            }
                        } else {
                            dockList.add(new HubMonitorPayload.DockInfo(dPos, "Zephyr Dock", 3));
                        }
                    }

                    PacketDistributor.sendToPlayer(
                            (ServerPlayer) player,
                            new HubMonitorPayload(pos, hubBE.getCustomName(), dockList)
                    );
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            else if (!player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {
                if (level.isClientSide) {
                    String currentName = hubBE.getCustomName();
                    ZephyrClientHooks.openHubNaming(pos, currentName);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }
}