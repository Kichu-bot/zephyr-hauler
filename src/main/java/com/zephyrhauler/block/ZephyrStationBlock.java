package com.zephyrhauler.block;

import com.mojang.serialization.MapCodec;
import com.zephyrhauler.block.entity.ZephyrStationBlockEntity;
import com.zephyrhauler.component.ZephyrDataComponents;
import com.zephyrhauler.item.ZephyrHaulerItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction; // <-- NUEVA IMPORTACIÓN
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext; // <-- NUEVA IMPORTACIÓN
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror; // <-- NUEVA IMPORTACIÓN
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation; // <-- NUEVA IMPORTACIÓN
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition; // <-- NUEVA IMPORTACIÓN
import net.minecraft.world.level.block.state.properties.BlockStateProperties; // <-- NUEVA IMPORTACIÓN
import net.minecraft.world.level.block.state.properties.DirectionProperty; // <-- NUEVA IMPORTACIÓN
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ZephyrStationBlock extends BaseEntityBlock {

    public static final MapCodec<ZephyrStationBlock> CODEC = simpleCodec(ZephyrStationBlock::new);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public ZephyrStationBlock(Properties properties) {
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
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ZephyrStationBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof ZephyrStationBlockEntity be) {
            ItemStack stored = be.getStoredHauler();

            if (stored.isEmpty()) {
                if (stack.getItem() instanceof ZephyrHaulerItem) {
                    be.setStoredHauler(stack.copy());

                    if (!level.isClientSide) {
                        stack.shrink(1);
                        level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }

            String damageType = stored.getOrDefault(ZephyrDataComponents.HAULER_DAMAGE.get(), "unknown");

            java.util.List<String> upgrades = stored.getOrDefault(ZephyrDataComponents.HAULER_UPGRADES.get(), new java.util.ArrayList<>());
            int maxDur = upgrades.contains("reinforced") ? 12 : 8;
            int durability = stored.getOrDefault(ZephyrDataComponents.HAULER_DURABILITY.get(), maxDur);

            if (durability <= 0 && isRepairMaterial(damageType, stack, stored)) {
                if (!level.isClientSide) {
                    if (!player.isCreative()) { stack.shrink(1); }

                    stored.set(ZephyrDataComponents.HAULER_DURABILITY.get(), maxDur);
                    stored.remove(ZephyrDataComponents.HAULER_DAMAGE.get());
                    be.setStoredHauler(stored);

                    playRepairSound(damageType, level, pos);

                    player.displayClientMessage(Component.translatable("message.zephyr_hauler.station_repaired").withStyle(ChatFormatting.GREEN), true);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof ZephyrStationBlockEntity be) {
            ItemStack stored = be.getStoredHauler();

            if (!stored.isEmpty()) {
                if (player.isShiftKeyDown()) {
                    if (!level.isClientSide) {
                        Block.popResource(level, pos.above(), stored.copy());
                        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5f, 1.0f);
                    }
                    be.setStoredHauler(ItemStack.EMPTY);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }

                if (!level.isClientSide) {
                    java.util.List<String> upgrades = stored.getOrDefault(ZephyrDataComponents.HAULER_UPGRADES.get(), new java.util.ArrayList<>());
                    int maxDur = upgrades.contains("reinforced") ? 12 : 8;
                    int durability = stored.getOrDefault(ZephyrDataComponents.HAULER_DURABILITY.get(), maxDur);

                    if (durability > 0) {
                        player.displayClientMessage(Component.translatable("message.zephyr_hauler.station_good_condition", durability, maxDur).withStyle(ChatFormatting.GREEN), true);
                    } else {
                        String damageType = stored.getOrDefault(ZephyrDataComponents.HAULER_DAMAGE.get(), "unknown");
                        String reqKey = getRequirementKey(damageType);

                        player.displayClientMessage(
                                Component.translatable("message.zephyr_hauler.station_damaged",
                                        Component.translatable(reqKey).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.RED),
                                true);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof ZephyrStationBlockEntity be) {
                ItemStack stored = be.getStoredHauler();
                if (!stored.isEmpty()) {
                    Block.popResource(level, pos, stored);
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    private boolean isRepairMaterial(String damageType, ItemStack stack, ItemStack haulerStack) {
        return switch (damageType) {
            case "torn_fabric" -> {
                String balloonColor = haulerStack.getOrDefault(ZephyrDataComponents.HAULER_COLOR.get(), "white");
                yield stack.is(getWoolFromColor(balloonColor));
            }
            case "frayed_seams" -> stack.is(Items.STRING);
            case "air_leak" -> stack.is(Items.LEATHER);
            case "clogged_burner" -> stack.is(Items.IRON_INGOT);
            case "stuck_valve" -> stack.is(Items.COPPER_INGOT);
            case "broken_straps" -> stack.is(Items.LEATHER) || stack.is(Items.LEAD);
            case "soot_buildup" -> stack.is(Items.SPONGE) || stack.is(Items.SLIME_BALL);
            case "deformed_ring" -> stack.is(Items.IRON_NUGGET);
            default -> false;
        };
    }

    private Item getWoolFromColor(String color) {
        return switch (color) {
            case "red" -> Items.RED_WOOL; case "blue" -> Items.BLUE_WOOL; case "green" -> Items.GREEN_WOOL;
            case "black" -> Items.BLACK_WOOL; case "orange" -> Items.ORANGE_WOOL; case "magenta" -> Items.MAGENTA_WOOL;
            case "light_blue" -> Items.LIGHT_BLUE_WOOL; case "yellow" -> Items.YELLOW_WOOL; case "lime" -> Items.LIME_WOOL;
            case "pink" -> Items.PINK_WOOL; case "gray" -> Items.GRAY_WOOL; case "light_gray" -> Items.LIGHT_GRAY_WOOL;
            case "cyan" -> Items.CYAN_WOOL; case "purple" -> Items.PURPLE_WOOL; case "brown" -> Items.BROWN_WOOL;
            default -> Items.WHITE_WOOL;
        };
    }

    private String getRequirementKey(String damageType) {
        return switch (damageType) {
            case "torn_fabric" -> "material.zephyr_hauler.matching_wool";
            case "frayed_seams" -> "material.zephyr_hauler.string";
            case "air_leak" -> "material.zephyr_hauler.leather";
            case "clogged_burner" -> "material.zephyr_hauler.iron";
            case "stuck_valve" -> "material.zephyr_hauler.copper";
            case "broken_straps" -> "material.zephyr_hauler.leather_or_lead";
            case "soot_buildup" -> "material.zephyr_hauler.sponge_or_slime";
            case "deformed_ring" -> "material.zephyr_hauler.iron_nugget";
            default -> "material.zephyr_hauler.unknown";
        };
    }

    private void playRepairSound(String damageType, Level level, BlockPos pos) {
        switch (damageType) {
            case "torn_fabric" -> level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0f, 1.0f);
            case "frayed_seams", "air_leak" -> level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
            case "clogged_burner", "deformed_ring" -> level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.5f, 1.5f);
            case "stuck_valve" -> level.playSound(null, pos, SoundEvents.COPPER_BREAK, SoundSource.BLOCKS, 1.0f, 1.5f);
            case "broken_straps" -> level.playSound(null, pos, SoundEvents.LEASH_KNOT_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
            case "soot_buildup" -> level.playSound(null, pos, SoundEvents.HONEY_BLOCK_FALL, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }
}