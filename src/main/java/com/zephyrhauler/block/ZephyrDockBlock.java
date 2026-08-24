package com.zephyrhauler.block;

import com.zephyrhauler.block.entity.ZephyrDockBlockEntity;
import com.zephyrhauler.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter; // <-- Añadido
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block; // <-- Añadido
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext; // <-- Añadido
import net.minecraft.world.phys.shapes.VoxelShape; // <-- Añadido
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class ZephyrDockBlock extends BaseEntityBlock {

    public static final MapCodec<ZephyrDockBlock> CODEC = simpleCodec(ZephyrDockBlock::new);

    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public ZephyrDockBlock(Properties properties) {
        super(properties);
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
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable net.minecraft.world.entity.LivingEntity placer, net.minecraft.world.item.ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.isClientSide() && placer instanceof net.minecraft.client.player.LocalPlayer) {
            openNamingScreen(pos);
        }
    }

    @net.neoforged.api.distmarker.OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
    private void openNamingScreen(BlockPos pos) {
        net.minecraft.client.Minecraft.getInstance().setScreen(new com.zephyrhauler.client.gui.screen.ZephyrDockNamingScreen(pos));
    }

    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ZephyrDockBlockEntity dockBE) {

            if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {
                if (!level.isClientSide) {
                    dockBE.setOccupied(false);
                    dockBE.setPendingDeliveryData(null);
                    dockBE.setLinkId(null);

                    player.displayClientMessage(
                            Component.translatable("message.zephyr_hauler.dock_reset").withStyle(ChatFormatting.GREEN),
                            true
                    );
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
            }

            else if (!player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {
                if (level.isClientSide) {
                    openNamingScreen(pos);
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return net.minecraft.world.InteractionResult.PASS;
    }
}