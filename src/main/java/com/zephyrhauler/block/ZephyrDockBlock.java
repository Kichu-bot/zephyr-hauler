package com.zephyrhauler.block;

import com.zephyrhauler.block.entity.ZephyrDockBlockEntity;
import com.zephyrhauler.block.entity.ZephyrHubBlockEntity;
import com.zephyrhauler.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class ZephyrDockBlock extends BaseEntityBlock {

    public static final MapCodec<ZephyrDockBlock> CODEC = simpleCodec(ZephyrDockBlock::new);

    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public static final BooleanProperty HUB_MODE = BooleanProperty.create("hub_mode");
    public static final BooleanProperty RESERVED = BooleanProperty.create("reserved");

    public ZephyrDockBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HUB_MODE, false)
                .setValue(RESERVED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HUB_MODE, RESERVED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ZephyrDockBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.ZEPHYR_DOCK_BE.get(),
                (lvl, pos, st, blockEntity) -> blockEntity.tick(lvl, pos, st));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide && !state.is(oldState.getBlock())) {
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
            if (level.getBlockEntity(checkPos) instanceof ZephyrHubBlockEntity hubBE) {
                hubBE.scanNetwork();
            }
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable net.minecraft.world.entity.LivingEntity placer, net.minecraft.world.item.ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        BlockState currentState = level.getBlockState(pos);
        if (currentState.getValue(HUB_MODE)) {
            return;
        }

        if (level.isClientSide() && placer instanceof net.minecraft.client.player.LocalPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            String currentName = (be instanceof ZephyrDockBlockEntity dockBE) ? dockBE.getCustomName() : "";
            openNamingScreen(pos, currentName);
        }
    }

    @net.neoforged.api.distmarker.OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
    private void openNamingScreen(BlockPos pos, String currentName) {
        net.minecraft.client.Minecraft.getInstance().setScreen(new com.zephyrhauler.client.gui.screen.ZephyrDockNamingScreen(pos, currentName));
    }

    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ZephyrDockBlockEntity dockBE) {

            if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {

                if (state.getValue(HUB_MODE)) {
                    if (!level.isClientSide) {
                        player.displayClientMessage(
                                Component.literal("Este muelle es controlado por un Hub y no puede ser reseteado manualmente.")
                                        .withStyle(ChatFormatting.RED),
                                true
                        );
                    }
                    return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
                }

                if (!level.isClientSide) {
                    dockBE.setOccupied(false);
                    dockBE.setPendingDeliveryData(null);
                    dockBE.setLinkId(null);

                    level.setBlock(pos, state.setValue(RESERVED, false), 3);

                    player.displayClientMessage(
                            Component.translatable("message.zephyr_hauler.dock_reset").withStyle(ChatFormatting.GREEN),
                            true
                    );
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
            }
            else if (!player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {

                if (state.getValue(HUB_MODE)) {
                    if (!level.isClientSide) {
                        player.displayClientMessage(
                                Component.literal("Este muelle pertenece a una red logística y no puede ser renombrado.")
                                        .withStyle(ChatFormatting.RED),
                                true
                        );
                    }
                    return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
                }

                if (level.isClientSide) {
                    String currentName = dockBE.getCustomName();
                    openNamingScreen(pos, currentName);
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return net.minecraft.world.InteractionResult.PASS;
    }
}