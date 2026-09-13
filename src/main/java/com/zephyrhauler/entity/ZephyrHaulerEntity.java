package com.zephyrhauler.entity;

import com.zephyrhauler.block.entity.ZephyrDockBlockEntity;
import com.zephyrhauler.block.entity.ZephyrHubBlockEntity;
import com.zephyrhauler.component.ZephyrDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ZephyrHaulerEntity extends Entity implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<BlockState> CAPTURED_BLOCK = SynchedEntityData.defineId(ZephyrHaulerEntity.class, EntityDataSerializers.BLOCK_STATE);
    private static final EntityDataAccessor<CompoundTag> CAPTURED_BLOCK_NBT = SynchedEntityData.defineId(ZephyrHaulerEntity.class, EntityDataSerializers.COMPOUND_TAG);

    private static final EntityDataAccessor<Boolean> IS_DESCENDING = SynchedEntityData.defineId(ZephyrHaulerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PATH_BLOCKED = SynchedEntityData.defineId(ZephyrHaulerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> WAITING_FOR_LAUNCH = SynchedEntityData.defineId(ZephyrHaulerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> WAITING_AT_DOCK = SynchedEntityData.defineId(ZephyrHaulerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> COLOR = SynchedEntityData.defineId(ZephyrHaulerEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<Float> SPEED_MULTIPLIER = SynchedEntityData.defineId(ZephyrHaulerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> FUEL_TIER = SynchedEntityData.defineId(ZephyrHaulerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SYNCED_DURABILITY = SynchedEntityData.defineId(ZephyrHaulerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WEIGHT_TIER = SynchedEntityData.defineId(ZephyrHaulerEntity.class, EntityDataSerializers.INT);

    private ItemStack cachedDisplayItem = ItemStack.EMPTY;
    private ItemStack haulerItem = ItemStack.EMPTY;
    private double originY;

    public ZephyrHaulerEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = false;
    }

    private void restoreHubLinkIfNecessary() {
        if (this.haulerItem.has(DataComponents.CUSTOM_DATA)) {
            CustomData customData = this.haulerItem.get(DataComponents.CUSTOM_DATA);
            CompoundTag tag = customData.copyTag();
            if (tag.contains("OriginalHubPos")) {
                BlockPos hubPos = BlockPos.of(tag.getLong("OriginalHubPos"));
                net.minecraft.resources.ResourceKey<Level> dim = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, net.minecraft.resources.ResourceLocation.parse(tag.getString("OriginalHubDim")));

                this.haulerItem.set(ZephyrDataComponents.TARGET_POS.get(), GlobalPos.of(dim, hubPos));

                tag.remove("OriginalHubPos");
                tag.remove("OriginalHubDim");
                if (tag.isEmpty()) this.haulerItem.remove(DataComponents.CUSTOM_DATA);
                else this.haulerItem.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
        }
    }

    public static boolean isAirspaceObstructed(Level level, BlockPos basePos) {
        if (!level.isLoaded(basePos)) return true;
        for (int y = 1; y <= 60; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos scanPos = basePos.offset(x, y, z);
                    if (!level.getBlockState(scanPos).getCollisionShape(level, scanPos).isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private long countItemsRecursively(CompoundTag tag) {
        long total = 0;
        String[] possibleListNames = {"Items", "Inventory", "inventory", "items"};
        for (String listName : possibleListNames) {
            if (tag.contains(listName, 9)) {
                ListTag list = tag.getList(listName, 10);
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag itemTag = list.getCompound(i);
                    if (itemTag.contains("count")) total += itemTag.getInt("count");
                    else if (itemTag.contains("Count")) total += itemTag.getInt("Count");
                }
            }
        }
        for (String key : tag.getAllKeys()) {
            net.minecraft.nbt.Tag child = tag.get(key);
            if (child instanceof CompoundTag childCompound) {
                total += countItemsRecursively(childCompound);
            }
        }
        return total;
    }

    public void setCapturedData(BlockState state, CompoundTag blockEntityData, ItemStack haulerItem) {
        this.entityData.set(CAPTURED_BLOCK, state);
        this.entityData.set(CAPTURED_BLOCK_NBT, blockEntityData);
        this.haulerItem = haulerItem;
        this.originY = this.getY();

        this.setColor(haulerItem.getOrDefault(ZephyrDataComponents.HAULER_COLOR.get(), "white"));
        long totalItemCount = countItemsRecursively(blockEntityData);

        int calculatedWeight = 0;
        if (totalItemCount > 3456) calculatedWeight = 4;
        else if (totalItemCount > 1728) calculatedWeight = 3;
        else if (totalItemCount > 960) calculatedWeight = 2;
        else if (totalItemCount > 320) calculatedWeight = 1;

        java.util.List<String> upgrades = haulerItem.getOrDefault(ZephyrDataComponents.HAULER_UPGRADES.get(), new java.util.ArrayList<>());
        if (upgrades.contains("shulker_buoyancy")) calculatedWeight = Math.max(0, calculatedWeight - 1);
        this.entityData.set(WEIGHT_TIER, calculatedWeight);

        int maxDurability = upgrades.contains("netherite_plating") ? 32 : (upgrades.contains("reinforced") ? 12 : 8);
        this.entityData.set(SYNCED_DURABILITY, haulerItem.getOrDefault(ZephyrDataComponents.HAULER_DURABILITY.get(), maxDurability));
    }

    public void setDescending(boolean descending) { this.entityData.set(IS_DESCENDING, descending); }
    public void setWaitingForLaunch(boolean wait) { this.entityData.set(WAITING_FOR_LAUNCH, wait); }
    public void setWaitingAtDock(boolean wait) { this.entityData.set(WAITING_AT_DOCK, wait); }
    public int getWeightTier() { return this.entityData.get(WEIGHT_TIER); }
    public String getColor() { return this.entityData.get(COLOR); }
    public void setColor(String color) { this.entityData.set(COLOR, color); }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(CAPTURED_BLOCK, Blocks.AIR.defaultBlockState());
        builder.define(CAPTURED_BLOCK_NBT, new CompoundTag());
        builder.define(IS_DESCENDING, false);
        builder.define(PATH_BLOCKED, false);
        builder.define(WAITING_FOR_LAUNCH, false);
        builder.define(WAITING_AT_DOCK, false);
        builder.define(COLOR, "white");
        builder.define(SPEED_MULTIPLIER, 1.0f);
        builder.define(FUEL_TIER, 0);
        builder.define(SYNCED_DURABILITY, 8);
        builder.define(WEIGHT_TIER, 0);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            ItemStack heldItem = player.getItemInHand(hand);

            if (this.entityData.get(WAITING_AT_DOCK)) {
                if (!this.level().isClientSide()) {
                    if (player.isShiftKeyDown() && heldItem.isEmpty()) {
                        GlobalPos targetPos = this.haulerItem.get(ZephyrDataComponents.TARGET_POS.get());
                        if (targetPos != null) attemptLanding(targetPos.pos(), player);
                    } else {
                        player.displayClientMessage(Component.translatable("message.zephyr_hauler.entity.waiting_dock").withStyle(ChatFormatting.YELLOW), true);
                    }
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }

            if (this.entityData.get(WAITING_FOR_LAUNCH)) {
                if (player.isShiftKeyDown() && heldItem.isEmpty()) {
                    if (!this.level().isClientSide()) abortLaunch(player);
                    return InteractionResult.sidedSuccess(this.level().isClientSide());
                }
                else if (!player.isShiftKeyDown()) {
                    if (!this.level().isClientSide()) {
                        float speedMult = 0.5f; int fuelTier = 0; boolean isFuel = false;

                        if (heldItem.isEmpty()) isFuel = true;
                        else if (heldItem.is(Items.COAL) || heldItem.is(Items.CHARCOAL) || heldItem.is(Items.DRIED_KELP_BLOCK)) { speedMult = 1.0f; fuelTier = 1; isFuel = true; }
                        else if (heldItem.is(Items.BLAZE_POWDER) || heldItem.is(Items.MAGMA_CREAM) || heldItem.is(Items.COAL_BLOCK)) { speedMult = 1.5f; fuelTier = 2; isFuel = true; }
                        else if (heldItem.is(Items.SOUL_SAND) || heldItem.is(Items.SOUL_SOIL) || heldItem.is(Items.SOUL_CAMPFIRE)) { speedMult = 2.0f; fuelTier = 3; isFuel = true; }
                        else if (heldItem.is(Items.WIND_CHARGE) || heldItem.is(Items.GUNPOWDER)) { speedMult = 3.0f; fuelTier = 4; isFuel = true; }

                        if (!isFuel) {
                            player.displayClientMessage(Component.translatable("message.zephyr_hauler.entity.need_fuel").withStyle(ChatFormatting.RED), true);
                            this.level().playSound(null, this.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
                            return InteractionResult.sidedSuccess(this.level().isClientSide());
                        }

                        java.util.List<String> upgrades = this.haulerItem.getOrDefault(ZephyrDataComponents.HAULER_UPGRADES.get(), new java.util.ArrayList<>());
                        if (upgrades.contains("supercharged_burner") && fuelTier > 0) {
                            if (fuelTier == 1) { fuelTier = 2; speedMult = 1.5f; }
                            else if (fuelTier == 2) { fuelTier = 3; speedMult = 2.0f; }
                            else if (fuelTier == 3) { fuelTier = 4; speedMult = 3.0f; }
                            else if (fuelTier == 4) { fuelTier = 5; speedMult = 5.0f; }
                        }
                        if (!player.isCreative() && fuelTier > 0) heldItem.shrink(1);

                        GlobalPos targetGlobalPos = this.haulerItem.get(ZephyrDataComponents.TARGET_POS.get());

                        if (targetGlobalPos != null && this.haulerItem.has(ZephyrDataComponents.LINK_ID.get())) {
                            java.util.UUID entityLinkId = this.haulerItem.get(ZephyrDataComponents.LINK_ID.get());
                            ServerLevel targetLevel = this.getServer().getLevel(targetGlobalPos.dimension());

                            if (targetLevel != null) {
                                targetLevel.getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.POST_TELEPORT, new ChunkPos(targetGlobalPos.pos()), 3, this.getId());
                                BlockEntity be = targetLevel.getBlockEntity(targetGlobalPos.pos());

                                if (be instanceof ZephyrHubBlockEntity hubBE) {
                                    if (hubBE.getHubId() == null || !hubBE.getHubId().equals(entityLinkId)) {
                                        this.triggerAnim("controller", "deny_action");
                                        player.displayClientMessage(Component.translatable("message.zephyr_hauler.entity.link_broken").withStyle(ChatFormatting.RED), true);
                                        return InteractionResult.sidedSuccess(this.level().isClientSide());
                                    }
                                    BlockPos assignedDock = hubBE.requestAvailableDock();
                                    if (assignedDock != null) {
                                        CustomData customData = this.haulerItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                                        CompoundTag tag = customData.copyTag();
                                        tag.putLong("OriginalHubPos", targetGlobalPos.pos().asLong());
                                        tag.putString("OriginalHubDim", targetGlobalPos.dimension().location().toString());
                                        this.haulerItem.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                                        this.haulerItem.set(ZephyrDataComponents.TARGET_POS.get(), GlobalPos.of(targetGlobalPos.dimension(), assignedDock));
                                        targetGlobalPos = this.haulerItem.get(ZephyrDataComponents.TARGET_POS.get());

                                        BlockEntity assignedBE = targetLevel.getBlockEntity(assignedDock);
                                        if (assignedBE instanceof ZephyrDockBlockEntity newDockBE) newDockBE.setLinkId(entityLinkId);
                                    } else {
                                        this.triggerAnim("controller", "deny_action");
                                        player.displayClientMessage(Component.translatable("message.zephyr_hauler.hub.no_space").withStyle(ChatFormatting.RED), true);
                                        this.level().playSound(null, this.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
                                        return InteractionResult.sidedSuccess(this.level().isClientSide());
                                    }
                                }
                                else if (be instanceof ZephyrDockBlockEntity dockBE) {
                                    BlockState dState = targetLevel.getBlockState(targetGlobalPos.pos());
                                    boolean isReserved = dState.hasProperty(com.zephyrhauler.block.ZephyrDockBlock.RESERVED) && dState.getValue(com.zephyrhauler.block.ZephyrDockBlock.RESERVED);

                                    if (dockBE.getLinkId() == null || !dockBE.getLinkId().equals(entityLinkId)) {
                                        this.triggerAnim("controller", "deny_action");
                                        player.displayClientMessage(Component.translatable("message.zephyr_hauler.entity.link_broken").withStyle(ChatFormatting.RED), true);
                                        this.level().playSound(null, this.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
                                        return InteractionResult.sidedSuccess(this.level().isClientSide());
                                    }

                                    if (dockBE.isOccupied() || isReserved) {
                                        this.triggerAnim("controller", "deny_action");
                                        player.displayClientMessage(Component.translatable("message.zephyr_hauler.entity.dock_occupied").withStyle(ChatFormatting.RED), true);
                                        this.level().playSound(null, this.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
                                        return InteractionResult.sidedSuccess(this.level().isClientSide());
                                    }

                                    if (dState.hasProperty(com.zephyrhauler.block.ZephyrDockBlock.RESERVED)) {
                                        targetLevel.setBlock(targetGlobalPos.pos(), dState.setValue(com.zephyrhauler.block.ZephyrDockBlock.RESERVED, true), 3);
                                    }
                                }
                            }
                        }

                        boolean escapeBlocked = isAirspaceObstructed(this.level(), this.blockPosition());
                        boolean landingBlocked = false;

                        if (targetGlobalPos != null && !escapeBlocked) {
                            ServerLevel targetLvl = this.getServer().getLevel(targetGlobalPos.dimension());
                            if (targetLvl != null) landingBlocked = isAirspaceObstructed(targetLvl, targetGlobalPos.pos());
                        }

                        if (escapeBlocked || landingBlocked) {
                            if (targetGlobalPos != null) {
                                ServerLevel targetLvl = this.getServer().getLevel(targetGlobalPos.dimension());
                                if (targetLvl != null) {
                                    BlockEntity be = targetLvl.getBlockEntity(targetGlobalPos.pos());
                                    if (be instanceof ZephyrDockBlockEntity dockBE) {
                                        dockBE.setOccupied(false);
                                        BlockState targetState = targetLvl.getBlockState(targetGlobalPos.pos());
                                        if (targetState.hasProperty(com.zephyrhauler.block.ZephyrDockBlock.RESERVED)) {
                                            targetLvl.setBlock(targetGlobalPos.pos(), targetState.setValue(com.zephyrhauler.block.ZephyrDockBlock.RESERVED, false), 3);
                                        }
                                    }
                                }
                            }
                            this.entityData.set(PATH_BLOCKED, true);
                            this.triggerAnim("controller", "deny_action");
                            player.displayClientMessage(Component.translatable(escapeBlocked ? "message.zephyr_hauler.entity.path_blocked" : "message.zephyr_hauler.entity.landing_blocked").withStyle(ChatFormatting.RED), true);
                            this.level().playSound(null, this.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
                        } else {
                            if (upgrades.contains("aerodynamic")) speedMult *= 1.25f;
                            int wTier = this.entityData.get(WEIGHT_TIER);

                            if (targetGlobalPos != null) {
                                BlockPos originPos = this.blockPosition();
                                double dx = targetGlobalPos.pos().getX() - originPos.getX();
                                double dz = targetGlobalPos.pos().getZ() - originPos.getZ();

                                if (dx != 0 || dz != 0) {
                                    com.zephyrhauler.util.ZephyrWindSystem.WindInfo wind = com.zephyrhauler.util.ZephyrWindSystem.getCurrentWind(this.level());
                                    double travelLength = Math.sqrt(dx * dx + dz * dz);
                                    double dirX = dx / travelLength;
                                    double dirZ = dz / travelLength;
                                    double windLength = Math.sqrt(wind.direction().x * wind.direction().x + wind.direction().z * wind.direction().z);

                                    if (windLength > 0) {
                                        double dotProduct = (dirX * (wind.direction().x / windLength)) + (dirZ * (wind.direction().z / windLength));
                                        float altitudeMultiplier = this.getY() > 128 ? 1.5f : (this.getY() < 60 ? 0.5f : 1.0f);
                                        float windInertiaNerf = 1.0f - (wTier * 0.15f);
                                        speedMult *= Math.max(0.2f, 1.0f + (float)(dotProduct * wind.speedBonus() * altitudeMultiplier * Math.max(0.2f, windInertiaNerf)));
                                    }
                                }
                            }
                            this.entityData.set(SPEED_MULTIPLIER, speedMult);
                            this.entityData.set(FUEL_TIER, fuelTier);
                            this.setWaitingForLaunch(false);
                            this.entityData.set(PATH_BLOCKED, false);

                            if (fuelTier == 1) this.level().playSound(null, this.blockPosition(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.NEUTRAL, 1.0F, 1.0F);
                            else if (fuelTier == 2) this.level().playSound(null, this.blockPosition(), SoundEvents.GHAST_WARN, SoundSource.NEUTRAL, 0.5F, 1.0F);
                            else if (fuelTier == 3) this.level().playSound(null, this.blockPosition(), SoundEvents.GHAST_SHOOT, SoundSource.NEUTRAL, 0.5F, 1.5F);
                            else if (fuelTier == 4) this.level().playSound(null, this.blockPosition(), SoundEvents.WIND_CHARGE_THROW, SoundSource.NEUTRAL, 0.5F, 2.0F);
                            else if (fuelTier == 5) this.level().playSound(null, this.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.NEUTRAL, 1.0F, 1.5F);

                            player.displayClientMessage(Component.translatable("message.zephyr_hauler.entity.launch_info", wTier, fuelTier).withStyle(ChatFormatting.AQUA), true);
                        }
                    }
                    return InteractionResult.sidedSuccess(this.level().isClientSide());
                }
            }
        }
        return super.interact(player, hand);
    }

    public boolean triggerRedstoneLaunch(int fuelTier, float speedMult) {
        if (!this.level().isClientSide() && this.entityData.get(WAITING_FOR_LAUNCH)) {
            GlobalPos targetGlobalPos = null;

            if (this.haulerItem.has(ZephyrDataComponents.TARGET_POS.get()) && this.haulerItem.has(ZephyrDataComponents.LINK_ID.get())) {
                targetGlobalPos = this.haulerItem.get(ZephyrDataComponents.TARGET_POS.get());
                java.util.UUID entityLinkId = this.haulerItem.get(ZephyrDataComponents.LINK_ID.get());
                ServerLevel targetLevel = this.getServer().getLevel(targetGlobalPos.dimension());

                if (targetLevel != null) {
                    targetLevel.getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.POST_TELEPORT, new ChunkPos(targetGlobalPos.pos()), 3, this.getId());
                    BlockEntity be = targetLevel.getBlockEntity(targetGlobalPos.pos());

                    if (be instanceof ZephyrHubBlockEntity hubBE) {
                        if (hubBE.getHubId() == null || !hubBE.getHubId().equals(entityLinkId)) {
                            this.triggerAnim("controller", "deny_action");
                            this.level().playSound(null, this.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
                            return false;
                        }
                        BlockPos assignedDock = hubBE.requestAvailableDock();
                        if (assignedDock != null) {
                            CustomData customData = this.haulerItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                            CompoundTag tag = customData.copyTag();
                            tag.putLong("OriginalHubPos", targetGlobalPos.pos().asLong());
                            tag.putString("OriginalHubDim", targetGlobalPos.dimension().location().toString());
                            this.haulerItem.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                            this.haulerItem.set(ZephyrDataComponents.TARGET_POS.get(), GlobalPos.of(targetGlobalPos.dimension(), assignedDock));
                            targetGlobalPos = this.haulerItem.get(ZephyrDataComponents.TARGET_POS.get());

                            BlockEntity assignedBE = targetLevel.getBlockEntity(assignedDock);
                            if (assignedBE instanceof ZephyrDockBlockEntity newDockBE) newDockBE.setLinkId(entityLinkId);
                        } else {
                            this.triggerAnim("controller", "deny_action");
                            this.level().playSound(null, this.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
                            return false;
                        }
                    }
                    else if (be instanceof ZephyrDockBlockEntity dockBE) {
                        BlockState dState = targetLevel.getBlockState(targetGlobalPos.pos());
                        boolean isReserved = dState.hasProperty(com.zephyrhauler.block.ZephyrDockBlock.RESERVED) && dState.getValue(com.zephyrhauler.block.ZephyrDockBlock.RESERVED);

                        if (dockBE.getLinkId() == null || !dockBE.getLinkId().equals(entityLinkId) || dockBE.isOccupied() || isReserved) {
                            this.triggerAnim("controller", "deny_action");
                            this.level().playSound(null, this.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
                            return false;
                        }

                        if (dState.hasProperty(com.zephyrhauler.block.ZephyrDockBlock.RESERVED)) {
                            targetLevel.setBlock(targetGlobalPos.pos(), dState.setValue(com.zephyrhauler.block.ZephyrDockBlock.RESERVED, true), 3);
                        }
                    } else return false;
                }
            } else return false;

            boolean escapeBlocked = isAirspaceObstructed(this.level(), this.blockPosition());
            boolean landingBlocked = false;

            if (targetGlobalPos != null && !escapeBlocked) {
                ServerLevel targetLvl = this.getServer().getLevel(targetGlobalPos.dimension());
                if (targetLvl != null) landingBlocked = isAirspaceObstructed(targetLvl, targetGlobalPos.pos());
            }

            if (escapeBlocked || landingBlocked) {
                if (targetGlobalPos != null) {
                    ServerLevel targetLvl = this.getServer().getLevel(targetGlobalPos.dimension());
                    if (targetLvl != null) {
                        BlockEntity be = targetLvl.getBlockEntity(targetGlobalPos.pos());
                        if (be instanceof ZephyrDockBlockEntity dockBE) {
                            dockBE.setOccupied(false);
                            BlockState targetState = targetLvl.getBlockState(targetGlobalPos.pos());
                            if (targetState.hasProperty(com.zephyrhauler.block.ZephyrDockBlock.RESERVED)) {
                                targetLvl.setBlock(targetGlobalPos.pos(), targetState.setValue(com.zephyrhauler.block.ZephyrDockBlock.RESERVED, false), 3);
                            }
                        }
                    }
                }
                this.entityData.set(PATH_BLOCKED, true);
                this.triggerAnim("controller", "deny_action");
                this.level().playSound(null, this.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
                return false;
            }

            java.util.List<String> upgrades = this.haulerItem.getOrDefault(ZephyrDataComponents.HAULER_UPGRADES.get(), new java.util.ArrayList<>());
            if (upgrades.contains("supercharged_burner") && fuelTier > 0) {
                if (fuelTier == 1) { fuelTier = 2; speedMult = 1.5f; }
                else if (fuelTier == 2) { fuelTier = 3; speedMult = 2.0f; }
                else if (fuelTier == 3) { fuelTier = 4; speedMult = 3.0f; }
                else if (fuelTier == 4) { fuelTier = 5; speedMult = 5.0f; }
            }
            if (upgrades.contains("aerodynamic")) speedMult *= 1.25f;

            int wTier = this.entityData.get(WEIGHT_TIER);
            if (targetGlobalPos != null) {
                BlockPos originPos = this.blockPosition();
                double dx = targetGlobalPos.pos().getX() - originPos.getX();
                double dz = targetGlobalPos.pos().getZ() - originPos.getZ();

                if (dx != 0 || dz != 0) {
                    com.zephyrhauler.util.ZephyrWindSystem.WindInfo wind = com.zephyrhauler.util.ZephyrWindSystem.getCurrentWind(this.level());
                    double travelLength = Math.sqrt(dx * dx + dz * dz);
                    double dirX = dx / travelLength;
                    double dirZ = dz / travelLength;
                    double windLength = Math.sqrt(wind.direction().x * wind.direction().x + wind.direction().z * wind.direction().z);

                    if (windLength > 0) {
                        double dotProduct = (dirX * (wind.direction().x / windLength)) + (dirZ * (wind.direction().z / windLength));
                        float altitudeMultiplier = this.getY() > 128 ? 1.5f : (this.getY() < 60 ? 0.5f : 1.0f);
                        float windInertiaNerf = 1.0f - (wTier * 0.15f);
                        speedMult *= Math.max(0.2f, 1.0f + (float)(dotProduct * wind.speedBonus() * altitudeMultiplier * Math.max(0.2f, windInertiaNerf)));
                    }
                }
            }

            this.entityData.set(SPEED_MULTIPLIER, speedMult);
            this.entityData.set(FUEL_TIER, fuelTier);
            this.setWaitingForLaunch(false);
            this.entityData.set(PATH_BLOCKED, false);

            if (fuelTier == 1) this.level().playSound(null, this.blockPosition(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.NEUTRAL, 1.0F, 1.0F);
            else if (fuelTier == 2) this.level().playSound(null, this.blockPosition(), SoundEvents.GHAST_WARN, SoundSource.NEUTRAL, 0.5F, 1.0F);
            else if (fuelTier == 3) this.level().playSound(null, this.blockPosition(), SoundEvents.GHAST_SHOOT, SoundSource.NEUTRAL, 0.5F, 1.5F);
            else if (fuelTier >= 4) this.level().playSound(null, this.blockPosition(), SoundEvents.WIND_CHARGE_THROW, SoundSource.NEUTRAL, 0.5F, 2.0F);

            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.entityData.get(WAITING_FOR_LAUNCH) || this.entityData.get(WAITING_AT_DOCK) || this.entityData.get(PATH_BLOCKED)) {
            this.setDeltaMovement(0, 0, 0);
        } else {
            double currentY = this.getDeltaMovement().y;
            int wTier = this.entityData.get(WEIGHT_TIER);
            double baseSpeed = 0;

            if (this.entityData.get(IS_DESCENDING)) {
                if (wTier == 0) baseSpeed = -0.10;
                else if (wTier == 1) baseSpeed = -0.15;
                else if (wTier == 2) baseSpeed = -0.22;
                else if (wTier == 3) baseSpeed = -0.32;
                else if (wTier == 4) baseSpeed = -0.45;
                if (this.entityData.get(SYNCED_DURABILITY) <= 0) baseSpeed = -0.65;
            } else {
                if (wTier == 0) baseSpeed = 0.12;
                else if (wTier == 1) baseSpeed = 0.09;
                else if (wTier == 2) baseSpeed = 0.06;
                else if (wTier == 3) baseSpeed = 0.04;
                else if (wTier == 4) baseSpeed = 0.025;
            }

            double targetSpeed = baseSpeed * this.entityData.get(SPEED_MULTIPLIER);
            double smoothY = currentY + (targetSpeed - currentY) * 0.15;
            this.setDeltaMovement(0, smoothY, 0);

            if (this.level().isClientSide()) {
                double px = this.getX() + (this.random.nextDouble() - 0.5) * 0.5;
                double py = this.getY() + 0.5;
                double pz = this.getZ() + (this.random.nextDouble() - 0.5) * 0.5;

                if (!this.entityData.get(IS_DESCENDING)) {
                    int tier = this.entityData.get(FUEL_TIER);
                    if (tier == 1) this.level().addParticle(ParticleTypes.FLAME, px, py, pz, 0, -0.1, 0);
                    else if (tier == 2) {
                        this.level().addParticle(ParticleTypes.FLAME, px, py, pz, 0, -0.1, 0);
                        this.level().addParticle(ParticleTypes.LARGE_SMOKE, px, py, pz, 0, -0.1, 0);
                    } else if (tier == 3) this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 0, -0.1, 0);
                    else if (tier == 4) this.level().addParticle(ParticleTypes.CLOUD, px, py, pz, 0, -0.2, 0);
                    else if (tier == 5) {
                        this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 0, -0.3, 0);
                        this.level().addParticle(ParticleTypes.GUST, px, py, pz, 0, -0.3, 0);
                    }
                }

                if (this.entityData.get(SYNCED_DURABILITY) <= 2) {
                    this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, px, py + 1.0, pz, 0, 0.05, 0);
                    if (this.random.nextInt(20) == 0) this.level().playLocalSound(px, py, pz, SoundEvents.LAVA_EXTINGUISH, SoundSource.NEUTRAL, 0.2f, 1.5f, false);
                }
            }
        }

        this.move(MoverType.SELF, this.getDeltaMovement());

        if (!this.level().isClientSide()) {
            if (!this.entityData.get(WAITING_FOR_LAUNCH) && !this.entityData.get(WAITING_AT_DOCK) && !this.entityData.get(PATH_BLOCKED)) {
                if (!this.entityData.get(IS_DESCENDING)) {
                    if (this.getY() >= this.originY + 60) transferDataToDockAndDiscard();
                } else {
                    GlobalPos targetGlobal = this.haulerItem.get(ZephyrDataComponents.TARGET_POS.get());

                    if (targetGlobal != null && this.getY() <= targetGlobal.pos().getY() + 2.0) {
                        this.setPos(this.getX(), targetGlobal.pos().getY() + 1.1, this.getZ());
                        this.setDeltaMovement(0, 0, 0);
                        this.setWaitingAtDock(true);

                        ServerLevel targetLvl = this.getServer().getLevel(targetGlobal.dimension());
                        if (targetLvl != null) {
                            BlockEntity targetBE = targetLvl.getBlockEntity(targetGlobal.pos());
                            if (targetBE instanceof ZephyrDockBlockEntity dockBE) {
                                dockBE.setOccupied(true);
                                dockBE.setChanged();
                            }
                            BlockState dState = targetLvl.getBlockState(targetGlobal.pos());
                            if (dState.hasProperty(com.zephyrhauler.block.ZephyrDockBlock.RESERVED)) {
                                targetLvl.setBlock(targetGlobal.pos(), dState.setValue(com.zephyrhauler.block.ZephyrDockBlock.RESERVED, false), 3);
                            }
                        }

                        java.util.List<String> upgrades = this.haulerItem.getOrDefault(ZephyrDataComponents.HAULER_UPGRADES.get(), new java.util.ArrayList<>());
                        if (this.entityData.get(SYNCED_DURABILITY) <= 0) {
                            this.level().playSound(null, this.blockPosition(), SoundEvents.ANVIL_HIT, SoundSource.NEUTRAL, 1.0f, 0.5f);
                            ((ServerLevel) this.level()).sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 3, 0.5, 0.5, 0.5, 0.1);
                        }
                        else if (upgrades.contains("shock_absorbers") && this.entityData.get(WEIGHT_TIER) >= 2) this.level().playSound(null, this.blockPosition(), SoundEvents.SLIME_BLOCK_FALL, SoundSource.NEUTRAL, 1.0F, 1.0F);
                        else this.level().playSound(null, this.blockPosition(), SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.NEUTRAL, 0.5F, 1.0F);
                    }
                }
            }
        }
    }

    private void transferDataToDockAndDiscard() {
        if (!this.haulerItem.has(ZephyrDataComponents.TARGET_POS.get())) {
            this.discard();
            return;
        }
        GlobalPos targetPos = this.haulerItem.get(ZephyrDataComponents.TARGET_POS.get());
        ServerLevel targetLevel = this.getServer().getLevel(targetPos.dimension());

        if (targetLevel != null) {
            java.util.List<String> upgrades = this.haulerItem.getOrDefault(ZephyrDataComponents.HAULER_UPGRADES.get(), new java.util.ArrayList<>());
            int currentDurability = this.haulerItem.getOrDefault(ZephyrDataComponents.HAULER_DURABILITY.get(), upgrades.contains("netherite_plating") ? 32 : (upgrades.contains("reinforced") ? 12 : 8));
            int damage = 1;
            int wTier = this.entityData.get(WEIGHT_TIER);

            if (targetLevel.isThundering() && !upgrades.contains("lightning_proof")) damage = 3;
            else if (targetLevel.isRaining() && !upgrades.contains("waxed")) damage = 2;

            if (!upgrades.contains("shock_absorbers")) {
                if (wTier == 4 && !upgrades.contains("reinforced") && !upgrades.contains("netherite_plating")) damage += 2;
                else if (wTier >= 2 && !upgrades.contains("reinforced") && !upgrades.contains("netherite_plating") && this.random.nextFloat() < 0.3f) damage += 1;
            }

            int unbreakingLevel = 0;
            net.minecraft.world.item.enchantment.ItemEnchantments enchants = this.haulerItem.get(DataComponents.ENCHANTMENTS);
            if (enchants != null) {
                for (var entry : enchants.entrySet()) {
                    if (entry.getKey().is(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING)) {
                        unbreakingLevel = entry.getIntValue();
                        break;
                    }
                }
            }

            int finalDamage = 0;
            for (int d = 0; d < damage; d++) if (this.random.nextInt(unbreakingLevel + 1) == 0) finalDamage++;

            int newDurability = Math.max(0, currentDurability - finalDamage);
            this.haulerItem.set(ZephyrDataComponents.HAULER_DURABILITY.get(), newDurability);

            if (newDurability <= 0 && currentDurability > 0) {
                String[] damagePool = {"torn_fabric", "frayed_seams", "air_leak", "clogged_burner", "stuck_valve", "broken_straps", "soot_buildup", "deformed_ring"};
                this.haulerItem.set(ZephyrDataComponents.HAULER_DAMAGE.get(), (wTier >= 3 && this.random.nextBoolean()) ? "broken_straps" : damagePool[this.random.nextInt(damagePool.length)]);
            }

            targetLevel.getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.POST_TELEPORT, new ChunkPos(targetPos.pos()), 3, this.getId());
            BlockEntity be = targetLevel.getBlockEntity(targetPos.pos());
            if (be instanceof ZephyrDockBlockEntity dockBE) {
                CompoundTag payload = new CompoundTag();
                payload.put("BlockState", net.minecraft.nbt.NbtUtils.writeBlockState(this.entityData.get(CAPTURED_BLOCK)));
                payload.put("BlockEntityData", this.entityData.get(CAPTURED_BLOCK_NBT));
                payload.put("HaulerItem", this.haulerItem.saveOptional(targetLevel.registryAccess()));
                dockBE.setPendingDeliveryData(payload);
            }
        }
        this.discard();
    }

    private void attemptLanding(BlockPos dockPos, Player player) {
        BlockPos placePos = dockPos.above();
        if (this.level().getBlockState(placePos).canBeReplaced()) {
            this.level().setBlock(placePos, this.entityData.get(CAPTURED_BLOCK), 3);
            BlockEntity be = this.level().getBlockEntity(placePos);
            if (be != null && !this.entityData.get(CAPTURED_BLOCK_NBT).isEmpty()) be.loadWithComponents(this.entityData.get(CAPTURED_BLOCK_NBT), this.level().registryAccess());

            BlockEntity dockBe = this.level().getBlockEntity(dockPos);
            if (dockBe instanceof ZephyrDockBlockEntity dockBE) {
                dockBE.setOccupied(false);
                dockBE.setChanged();
            }
            BlockState dockState = this.level().getBlockState(dockPos);
            if (dockState.hasProperty(com.zephyrhauler.block.ZephyrDockBlock.RESERVED)) {
                this.level().setBlock(dockPos, dockState.setValue(com.zephyrhauler.block.ZephyrDockBlock.RESERVED, false), 3);
            }

            restoreHubLinkIfNecessary();
            if (!player.getInventory().add(this.haulerItem.copy())) this.spawnAtLocation(this.haulerItem);
            else this.level().playSound(null, this.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5f, 1.0f);

            this.discard();
        } else this.entityData.set(WAITING_AT_DOCK, true);
    }

    private void abortLaunch(Player player) {
        GlobalPos currentTarget = this.haulerItem.get(ZephyrDataComponents.TARGET_POS.get());

        restoreHubLinkIfNecessary();

        if (!player.getInventory().add(this.haulerItem.copy())) this.spawnAtLocation(this.haulerItem);
        else this.level().playSound(null, this.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5f, 1.0f);

        BlockState capturedState = this.entityData.get(CAPTURED_BLOCK);
        BlockPos basePos = this.blockPosition();

        if (capturedState.getBlock() != Blocks.AIR) {
            boolean placed = false;
            for (int i = 0; i <= 3; i++) {
                BlockPos checkPos = basePos.above(i);
                if (this.level().getBlockState(checkPos).canBeReplaced()) {
                    this.level().setBlock(checkPos, capturedState, 3);
                    BlockEntity be = this.level().getBlockEntity(checkPos);
                    if (be != null && !this.entityData.get(CAPTURED_BLOCK_NBT).isEmpty()) be.loadWithComponents(this.entityData.get(CAPTURED_BLOCK_NBT), this.level().registryAccess());
                    placed = true; break;
                }
            }
            if (!placed) {
                if (this.level().getBlockState(basePos).canBeReplaced()) {
                    this.level().setBlock(basePos, capturedState, 3);
                    BlockEntity be = this.level().getBlockEntity(basePos);
                    if (be != null && !this.entityData.get(CAPTURED_BLOCK_NBT).isEmpty()) be.loadWithComponents(this.entityData.get(CAPTURED_BLOCK_NBT), this.level().registryAccess());
                } else {
                    ItemStack blockItem = new ItemStack(capturedState.getBlock().asItem());
                    if (!this.entityData.get(CAPTURED_BLOCK_NBT).isEmpty()) blockItem.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(this.entityData.get(CAPTURED_BLOCK_NBT)));
                    if (!player.getInventory().add(blockItem)) this.spawnAtLocation(blockItem);
                    else this.level().playSound(null, this.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5f, 1.0f);
                }
            }
        }

        if (currentTarget != null) {
            ServerLevel targetLevel = this.getServer().getLevel(currentTarget.dimension());
            if (targetLevel != null) {
                BlockEntity be = targetLevel.getBlockEntity(currentTarget.pos());
                if (be instanceof ZephyrDockBlockEntity dockBE) {
                    dockBE.setOccupied(false);
                    dockBE.setChanged();
                }
                BlockState targetState = targetLevel.getBlockState(currentTarget.pos());
                if (targetState.hasProperty(com.zephyrhauler.block.ZephyrDockBlock.RESERVED)) {
                    targetLevel.setBlock(currentTarget.pos(), targetState.setValue(com.zephyrhauler.block.ZephyrDockBlock.RESERVED, false), 3);
                }
            }
        }
        this.discard();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide() && !this.isRemoved()) {
            if (!this.entityData.get(WAITING_FOR_LAUNCH) && !this.entityData.get(WAITING_AT_DOCK)) return false;

            GlobalPos currentTarget = this.haulerItem.get(ZephyrDataComponents.TARGET_POS.get());
            restoreHubLinkIfNecessary();

            this.spawnAtLocation(this.haulerItem);
            BlockState capturedState = this.entityData.get(CAPTURED_BLOCK);
            BlockPos pos = this.blockPosition();

            if (capturedState.getBlock() != Blocks.AIR) {
                boolean placed = false;
                for (int i = 0; i <= 3; i++) {
                    BlockPos checkPos = pos.above(i);
                    if (this.level().getBlockState(checkPos).canBeReplaced() && this.level().getBlockState(checkPos.below()).isFaceSturdy(this.level(), checkPos.below(), Direction.UP)) {
                        this.level().setBlock(checkPos, capturedState, 3);
                        BlockEntity be = this.level().getBlockEntity(checkPos);
                        if (be != null && !this.entityData.get(CAPTURED_BLOCK_NBT).isEmpty()) be.loadWithComponents(this.entityData.get(CAPTURED_BLOCK_NBT), this.level().registryAccess());
                        placed = true; break;
                    }
                }
                if (!placed) {
                    if (this.level().getBlockState(pos).canBeReplaced()) {
                        this.level().setBlock(pos, capturedState, 3);
                        BlockEntity be = this.level().getBlockEntity(pos);
                        if (be != null && !this.entityData.get(CAPTURED_BLOCK_NBT).isEmpty()) be.loadWithComponents(this.entityData.get(CAPTURED_BLOCK_NBT), this.level().registryAccess());
                    } else {
                        ItemStack blockItem = new ItemStack(capturedState.getBlock().asItem());
                        if (!this.entityData.get(CAPTURED_BLOCK_NBT).isEmpty()) blockItem.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(this.entityData.get(CAPTURED_BLOCK_NBT)));
                        this.spawnAtLocation(blockItem);
                    }
                }
            }

            if (currentTarget != null) {
                ServerLevel targetLevel = this.getServer().getLevel(currentTarget.dimension());
                if (targetLevel != null) {
                    BlockEntity be = targetLevel.getBlockEntity(currentTarget.pos());
                    if (be instanceof ZephyrDockBlockEntity dockBE) {
                        dockBE.setOccupied(false);
                        dockBE.setChanged();
                    }
                    BlockState targetState = targetLevel.getBlockState(currentTarget.pos());
                    if (targetState.hasProperty(com.zephyrhauler.block.ZephyrDockBlock.RESERVED)) {
                        targetLevel.setBlock(currentTarget.pos(), targetState.setValue(com.zephyrhauler.block.ZephyrDockBlock.RESERVED, false), 3);
                    }
                }
            }
            this.discard();
            return true;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "controller", 5, state -> {
            if (this.entityData.get(WAITING_FOR_LAUNCH) || this.entityData.get(WAITING_AT_DOCK) || this.getDeltaMovement().y == 0) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.zephyr_hauler.idle"));
            else return state.setAndContinue(RawAnimation.begin().thenLoop("animation.zephyr_hauler.fly"));
        }).triggerableAnim("deny_action", RawAnimation.begin().thenPlay("animation.zephyr_hauler.deny")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    public BlockState getCapturedBlock() { return this.entityData.get(CAPTURED_BLOCK); }
    public CompoundTag getCapturedBlockNBT() { return this.entityData.get(CAPTURED_BLOCK_NBT); }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("CapturedBlockEntityData")) this.entityData.set(CAPTURED_BLOCK_NBT, tag.getCompound("CapturedBlockEntityData"));
        if (tag.contains("HaulerItem")) this.haulerItem = ItemStack.parseOptional(this.level().registryAccess(), tag.getCompound("HaulerItem"));
        this.originY = tag.getDouble("OriginY");
        this.entityData.set(CAPTURED_BLOCK, net.minecraft.nbt.NbtUtils.readBlockState(this.level().holderLookup(net.minecraft.core.registries.Registries.BLOCK), tag.getCompound("CapturedBlockState")));
        this.entityData.set(IS_DESCENDING, tag.getBoolean("IsDescending"));
        this.entityData.set(PATH_BLOCKED, tag.getBoolean("PathBlocked"));
        this.entityData.set(WAITING_FOR_LAUNCH, tag.getBoolean("WaitingForLaunch"));
        this.entityData.set(WAITING_AT_DOCK, tag.getBoolean("WaitingAtDock"));

        if (tag.contains("HaulerColor")) this.setColor(tag.getString("HaulerColor"));
        if (tag.contains("SpeedMultiplier")) this.entityData.set(SPEED_MULTIPLIER, tag.getFloat("SpeedMultiplier"));
        if (tag.contains("FuelTier")) this.entityData.set(FUEL_TIER, tag.getInt("FuelTier"));
        if (tag.contains("SyncedDurability")) this.entityData.set(SYNCED_DURABILITY, tag.getInt("SyncedDurability"));
        if (tag.contains("WeightTier")) this.entityData.set(WEIGHT_TIER, tag.getInt("WeightTier"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.put("CapturedBlockEntityData", this.entityData.get(CAPTURED_BLOCK_NBT));
        tag.put("HaulerItem", this.haulerItem.saveOptional(this.level().registryAccess()));
        tag.putDouble("OriginY", this.originY);
        tag.put("CapturedBlockState", net.minecraft.nbt.NbtUtils.writeBlockState(this.entityData.get(CAPTURED_BLOCK)));
        tag.putBoolean("IsDescending", this.entityData.get(IS_DESCENDING));
        tag.putBoolean("PathBlocked", this.entityData.get(PATH_BLOCKED));
        tag.putBoolean("WaitingForLaunch", this.entityData.get(WAITING_FOR_LAUNCH));
        tag.putBoolean("WaitingAtDock", this.entityData.get(WAITING_AT_DOCK));

        tag.putString("HaulerColor", this.getColor());
        tag.putFloat("SpeedMultiplier", this.entityData.get(SPEED_MULTIPLIER));
        tag.putInt("FuelTier", this.entityData.get(FUEL_TIER));
        tag.putInt("SyncedDurability", this.entityData.get(SYNCED_DURABILITY));
        tag.putInt("WeightTier", this.entityData.get(WEIGHT_TIER));
    }

    @Override
    public boolean isPickable() { return true; }

    public ItemStack getDisplayItem() {
        if (this.cachedDisplayItem.isEmpty() && this.level().isClientSide()) {
            BlockState state = this.getCapturedBlock();
            CompoundTag nbt = this.getCapturedBlockNBT();

            if (state != null && !state.isAir()) {
                this.cachedDisplayItem = new ItemStack(state.getBlock().asItem());
                if (nbt != null && !nbt.isEmpty()) {
                    BlockEntity dummyBE = BlockEntity.loadStatic(this.blockPosition(), state, nbt, this.level().registryAccess());
                    if (dummyBE != null) dummyBE.saveToItem(this.cachedDisplayItem, this.level().registryAccess());
                    else this.cachedDisplayItem.set(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA, net.minecraft.world.item.component.CustomData.of(nbt));
                }
            }
        }
        return this.cachedDisplayItem;
    }
}