package com.zephyrhauler.block;

import com.mojang.serialization.MapCodec;
import com.zephyrhauler.block.entity.ZephyrStationBlockEntity;
import com.zephyrhauler.component.ZephyrDataComponents;
import com.zephyrhauler.item.ZephyrHaulerItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class ZephyrStationBlock extends BaseEntityBlock {

    public static final MapCodec<ZephyrStationBlock> CODEC = simpleCodec(ZephyrStationBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public ZephyrStationBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) { return state.setValue(FACING, rot.rotate(state.getValue(FACING))); }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) { return state.rotate(mirrorIn.getRotation(state.getValue(FACING))); }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ZephyrStationBlockEntity(pos, state); }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof ZephyrStationBlockEntity be) {
            ItemStack stored = be.getStoredHauler();

            if (stored.isEmpty()) {
                if (stack.getItem() instanceof ZephyrHaulerItem) {
                    ItemStack haulerToStore = stack.copy();
                    haulerToStore.setCount(1);

                    List<String> upgrades = haulerToStore.getOrDefault(ZephyrDataComponents.HAULER_UPGRADES.get(), new java.util.ArrayList<>());
                    int maxDur = upgrades.contains("netherite_plating") ? 32 : (upgrades.contains("reinforced") ? 12 : 8);
                    int durability = haulerToStore.getOrDefault(ZephyrDataComponents.HAULER_DURABILITY.get(), maxDur);

                    if (durability <= 0 && haulerToStore.getOrDefault(ZephyrDataComponents.HAULER_DAMAGE.get(), "unknown").equals("unknown")) {
                        String[] fallas = {"broken_straps", "torn_fabric", "clogged_burner", "deformed_ring", "frayed_seams"};
                        haulerToStore.set(ZephyrDataComponents.HAULER_DAMAGE.get(), fallas[level.random.nextInt(fallas.length)]);
                    }
                    else if (durability > 0 && durability < maxDur) {
                        if (!haulerToStore.has(ZephyrDataComponents.PREVENTIVE_REQ.get())) {
                            haulerToStore.set(ZephyrDataComponents.PREVENTIVE_REQ.get(), rollPreventiveRequirement(upgrades, level.random));
                        }
                    }

                    be.setStoredHauler(haulerToStore);
                    if (!level.isClientSide) {
                        stack.shrink(1);
                        level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }

            List<String> upgrades = stored.getOrDefault(ZephyrDataComponents.HAULER_UPGRADES.get(), new java.util.ArrayList<>());
            int maxDur = upgrades.contains("netherite_plating") ? 32 : (upgrades.contains("reinforced") ? 12 : 8);
            int durability = stored.getOrDefault(ZephyrDataComponents.HAULER_DURABILITY.get(), maxDur);
            String damageType = stored.getOrDefault(ZephyrDataComponents.HAULER_DAMAGE.get(), "unknown");
            String preventiveReq = stored.getOrDefault(ZephyrDataComponents.PREVENTIVE_REQ.get(), "wool");

            if (durability <= 0 && stored.has(ZephyrDataComponents.HAULER_DAMAGE.get())) {
                if (isRepairMaterial(damageType, stack)) {
                    if (!level.isClientSide) {
                        if (!player.isCreative()) stack.shrink(1);
                        stored.set(ZephyrDataComponents.HAULER_DURABILITY.get(), 1);
                        stored.remove(ZephyrDataComponents.HAULER_DAMAGE.get());
                        stored.set(ZephyrDataComponents.PREVENTIVE_REQ.get(), rollPreventiveRequirement(upgrades, level.random));
                        be.setStoredHauler(stored);

                        playRepairEffects(damageType, (ServerLevel) level, pos);
                        player.displayClientMessage(Component.translatable("message.zephyr_hauler.station_critical_repaired").withStyle(ChatFormatting.YELLOW), true);
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                } else if (!level.isClientSide) {
                    String reqKey = getRequirementKey(damageType);
                    player.displayClientMessage(
                            Component.translatable("message.zephyr_hauler.station_wrong_material",
                                    Component.translatable(reqKey).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.RED),
                            true);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }

            if (durability > 0 && durability < maxDur) {
                if (isPreventiveMaterial(preventiveReq, stack)) {
                    if (!level.isClientSide) {
                        if (!player.isCreative()) stack.shrink(1);

                        int healAmount = Math.max(2, maxDur / 4);
                        int newDurability = Math.min(maxDur, durability + healAmount);
                        stored.set(ZephyrDataComponents.HAULER_DURABILITY.get(), newDurability);

                        playPreventiveEffects(preventiveReq, (ServerLevel) level, pos);

                        if (newDurability == maxDur) {
                            stored.remove(ZephyrDataComponents.PREVENTIVE_REQ.get());
                            player.displayClientMessage(Component.translatable("message.zephyr_hauler.station_fully_repaired").withStyle(ChatFormatting.GREEN), true);
                        } else {
                            stored.set(ZephyrDataComponents.PREVENTIVE_REQ.get(), rollPreventiveRequirement(upgrades, level.random));
                            int pct = (newDurability * 100) / maxDur;
                            player.displayClientMessage(Component.translatable("message.zephyr_hauler.station_partial_repair", pct).withStyle(ChatFormatting.AQUA), true);
                        }
                        be.setStoredHauler(stored);
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                } else if (!level.isClientSide) {
                    String reqKey = getRequirementKey(preventiveReq);
                    player.displayClientMessage(
                            Component.translatable("message.zephyr_hauler.station_wrong_material",
                                    Component.translatable(reqKey).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.RED),
                            true);
                }
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
                        ItemStack stackToGive = stored.copy();
                        if (!player.getInventory().add(stackToGive)) {
                            Block.popResource(level, pos.above(), stackToGive);
                        } else {
                            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5f, 1.0f);
                        }
                    }
                    be.setStoredHauler(ItemStack.EMPTY);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }

                if (!level.isClientSide) {
                    List<String> upgrades = stored.getOrDefault(ZephyrDataComponents.HAULER_UPGRADES.get(), new java.util.ArrayList<>());
                    int maxDur = upgrades.contains("netherite_plating") ? 32 : (upgrades.contains("reinforced") ? 12 : 8);
                    int durability = stored.getOrDefault(ZephyrDataComponents.HAULER_DURABILITY.get(), maxDur);
                    int percentage = (durability * 100) / maxDur;

                    if (durability <= 0 && stored.has(ZephyrDataComponents.HAULER_DAMAGE.get())) {
                        String damageType = stored.getOrDefault(ZephyrDataComponents.HAULER_DAMAGE.get(), "unknown");
                        player.displayClientMessage(Component.translatable("message.zephyr_hauler.station_critical_status",
                                Component.translatable("damage.zephyr_hauler." + damageType).withStyle(ChatFormatting.RED),
                                Component.translatable(getRequirementKey(damageType)).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.DARK_RED), false);
                    } else {
                        String feedbackKey = getOrganicFeedback(percentage, level.random);
                        player.displayClientMessage(Component.translatable(feedbackKey).withStyle(getFormattingForHealth(percentage)), false);

                        if (percentage < 100) {
                            String req = stored.getOrDefault(ZephyrDataComponents.PREVENTIVE_REQ.get(), "wool");
                            player.displayClientMessage(Component.translatable("message.zephyr_hauler.station_preventive_info",
                                    Component.translatable(getRequirementKey(req)).withStyle(ChatFormatting.YELLOW), percentage).withStyle(ChatFormatting.GRAY), false);
                        }

                        net.minecraft.world.item.enchantment.ItemEnchantments enchants = stored.get(DataComponents.ENCHANTMENTS);
                        if (enchants != null && enchants.keySet().stream().anyMatch(e -> e.is(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING))) {
                            player.displayClientMessage(Component.literal("✦ Estructura impregnada con magia residual.").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC), false);
                        }
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    private String rollPreventiveRequirement(List<String> upgrades, net.minecraft.util.RandomSource random) {
        java.util.List<String> pool = new java.util.ArrayList<>(java.util.List.of("wool", "string", "leather", "flint", "coal"));

        if (upgrades.contains("reinforced") || upgrades.contains("netherite_plating")) {
            pool.addAll(java.util.List.of("iron", "copper", "iron_nugget", "chain", "redstone"));
        }
        if (upgrades.contains("netherite_plating")) {
            pool.addAll(java.util.List.of("netherite", "blaze_powder", "magma_cream"));
        }

        return pool.get(random.nextInt(pool.size()));
    }

    private boolean isPreventiveMaterial(String requirement, ItemStack stack) {
        return switch (requirement) {
            case "netherite" -> stack.is(Items.NETHERITE_SCRAP);
            case "blaze_powder" -> stack.is(Items.BLAZE_POWDER);
            case "magma_cream" -> stack.is(Items.MAGMA_CREAM);
            case "iron" -> stack.is(Items.IRON_INGOT);
            case "copper" -> stack.is(Items.COPPER_INGOT);
            case "iron_nugget" -> stack.is(Items.IRON_NUGGET);
            case "chain" -> stack.is(Items.CHAIN);
            case "redstone" -> stack.is(Items.REDSTONE);
            case "string" -> stack.is(Items.STRING);
            case "leather" -> stack.is(Items.LEATHER);
            case "flint" -> stack.is(Items.FLINT);
            case "coal" -> stack.is(Items.COAL);
            case "wool" -> isAnyWool(stack);
            default -> false;
        };
    }

    private boolean isRepairMaterial(String damageType, ItemStack stack) {
        return switch (damageType) {
            case "torn_fabric" -> isAnyWool(stack);
            case "frayed_seams" -> stack.is(Items.STRING);
            case "air_leak" -> stack.is(Items.LEATHER) || stack.is(Items.PHANTOM_MEMBRANE);
            case "clogged_burner", "stuck_valve" -> stack.is(Items.COPPER_INGOT);
            case "broken_straps" -> stack.is(Items.LEATHER) || stack.is(Items.LEAD);
            case "soot_buildup" -> stack.is(Items.SPONGE) || stack.is(Items.SLIME_BALL);
            case "deformed_ring" -> stack.is(Items.IRON_INGOT);
            default -> false;
        };
    }

    private boolean isAnyWool(ItemStack stack) {
        Item i = stack.getItem();
        return i == Items.WHITE_WOOL || i == Items.ORANGE_WOOL || i == Items.MAGENTA_WOOL || i == Items.LIGHT_BLUE_WOOL ||
                i == Items.YELLOW_WOOL || i == Items.LIME_WOOL || i == Items.PINK_WOOL || i == Items.GRAY_WOOL ||
                i == Items.LIGHT_GRAY_WOOL || i == Items.CYAN_WOOL || i == Items.PURPLE_WOOL || i == Items.BLUE_WOOL ||
                i == Items.BROWN_WOOL || i == Items.GREEN_WOOL || i == Items.RED_WOOL || i == Items.BLACK_WOOL;
    }

    private String getRequirementKey(String req) {
        return switch (req) {
            case "torn_fabric", "wool" -> "material.zephyr_hauler.any_wool";
            case "frayed_seams", "string" -> "material.zephyr_hauler.string";
            case "air_leak", "leather" -> "material.zephyr_hauler.leather";
            case "clogged_burner", "stuck_valve", "copper" -> "material.zephyr_hauler.copper";
            case "broken_straps" -> "material.zephyr_hauler.leather_or_lead";
            case "soot_buildup" -> "material.zephyr_hauler.sponge_or_slime";
            case "deformed_ring", "iron" -> "material.zephyr_hauler.iron";
            case "iron_nugget" -> "material.zephyr_hauler.iron_nugget";
            case "netherite" -> "material.zephyr_hauler.netherite_scrap";
            case "flint" -> "material.zephyr_hauler.flint";
            case "coal" -> "material.zephyr_hauler.coal";
            case "chain" -> "material.zephyr_hauler.chain";
            case "redstone" -> "material.zephyr_hauler.redstone";
            case "blaze_powder" -> "material.zephyr_hauler.blaze_powder";
            case "magma_cream" -> "material.zephyr_hauler.magma_cream";
            default -> "material.zephyr_hauler.unknown";
        };
    }

    private String getOrganicFeedback(int percentage, net.minecraft.util.RandomSource random) {
        if (percentage == 100) { String[] p = {"feedback.zephyr_hauler.perfect_1", "feedback.zephyr_hauler.perfect_2", "feedback.zephyr_hauler.perfect_3"}; return p[random.nextInt(p.length)]; }
        else if (percentage >= 70) { String[] p = {"feedback.zephyr_hauler.good_1", "feedback.zephyr_hauler.good_2", "feedback.zephyr_hauler.good_3"}; return p[random.nextInt(p.length)]; }
        else if (percentage >= 35) { String[] p = {"feedback.zephyr_hauler.warn_1", "feedback.zephyr_hauler.warn_2", "feedback.zephyr_hauler.warn_3"}; return p[random.nextInt(p.length)]; }
        else { String[] p = {"feedback.zephyr_hauler.critical_1", "feedback.zephyr_hauler.critical_2", "feedback.zephyr_hauler.critical_3"}; return p[random.nextInt(p.length)]; }
    }

    private ChatFormatting getFormattingForHealth(int percentage) { return percentage >= 70 ? ChatFormatting.GREEN : (percentage >= 35 ? ChatFormatting.YELLOW : ChatFormatting.RED); }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof ZephyrStationBlockEntity be) {
                ItemStack stored = be.getStoredHauler();
                if (!stored.isEmpty()) Block.popResource(level, pos, stored);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    private void playPreventiveEffects(String materialType, ServerLevel level, BlockPos pos) {
        double px = pos.getX() + 0.5; double py = pos.getY() + 1.2; double pz = pos.getZ() + 0.5;
        if (materialType.equals("wool") || materialType.equals("string") || materialType.equals("leather")) {
            level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
            level.sendParticles(ParticleTypes.CLOUD, px, py, pz, 5, 0.2, 0.2, 0.2, 0.05);
        } else {
            level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.5f, 2.0f);
            level.sendParticles(ParticleTypes.CRIT, px, py, pz, 10, 0.2, 0.2, 0.2, 0.1);
        }
    }

    private void playRepairEffects(String damageType, ServerLevel level, BlockPos pos) {
        double px = pos.getX() + 0.5; double py = pos.getY() + 1.2; double pz = pos.getZ() + 0.5;
        switch (damageType) {
            case "torn_fabric" -> { level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0f, 1.0f); level.sendParticles(ParticleTypes.CLOUD, px, py, pz, 10, 0.2, 0.2, 0.2, 0.05); }
            case "frayed_seams", "air_leak" -> { level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.BLOCKS, 1.0f, 1.0f); level.sendParticles(ParticleTypes.POOF, px, py, pz, 5, 0.1, 0.1, 0.1, 0.02); }
            case "clogged_burner", "deformed_ring" -> { level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.5f, 1.5f); level.sendParticles(ParticleTypes.CRIT, px, py, pz, 15, 0.2, 0.2, 0.2, 0.1); }
            case "stuck_valve" -> { level.playSound(null, pos, SoundEvents.COPPER_BREAK, SoundSource.BLOCKS, 1.0f, 1.5f); level.sendParticles(ParticleTypes.SCRAPE, px, py, pz, 10, 0.2, 0.2, 0.2, 0.1); }
            case "broken_straps" -> { level.playSound(null, pos, SoundEvents.LEASH_KNOT_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f); level.sendParticles(ParticleTypes.ITEM_SLIME, px, py, pz, 5, 0.2, 0.2, 0.2, 0.1); }
            case "soot_buildup" -> { level.playSound(null, pos, SoundEvents.HONEY_BLOCK_FALL, SoundSource.BLOCKS, 1.0f, 1.0f); level.sendParticles(ParticleTypes.SPLASH, px, py, pz, 20, 0.3, 0.1, 0.3, 0.1); }
        }
    }
}