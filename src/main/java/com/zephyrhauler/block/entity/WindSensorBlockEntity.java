package com.zephyrhauler.block.entity;

import com.zephyrhauler.registry.ModBlockEntities;
import com.zephyrhauler.util.ZephyrWindSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import org.jetbrains.annotations.Nullable;

public class WindSensorBlockEntity extends BlockEntity implements GeoBlockEntity, MenuProvider {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int configuredPower = 15;
    private int configuredCooldown = 5;
    private ZephyrWindSystem.WindDirection targetDirection = ZephyrWindSystem.WindDirection.NORTH;

    private boolean isLinked = false;
    private BlockPos linkedDockPos = BlockPos.ZERO;
    private String linkedDockName = "";
    private String linkedDockDimension = "";

    private int pulseTimer = 0;
    private int cooldownTimer = 0;

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> configuredPower;
                case 1 -> targetDirection.ordinal();
                case 2 -> isLinked ? 1 : 0;
                case 3 -> configuredCooldown;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> configuredPower = value;
                case 1 -> {
                    ZephyrWindSystem.WindDirection[] dirs = ZephyrWindSystem.WindDirection.values();
                    if (value >= 0 && value < dirs.length) targetDirection = dirs[value];
                }
                case 2 -> isLinked = (value == 1);
                case 3 -> configuredCooldown = value;
            }
        }

        @Override
        public int getCount() { return 4; }
    };

    public WindSensorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WIND_SENSOR_BE.get(), pos, state);
    }

    public ContainerData getContainerData() { return this.data; }

    public int getConfiguredPower() { return configuredPower; }
    public void setConfiguredPower(int power) {
        this.configuredPower = power;
        setChanged();
        if (this.level != null && !this.level.isClientSide()) this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public int getConfiguredCooldown() { return configuredCooldown; }
    public void setConfiguredCooldown(int secs) {
        this.configuredCooldown = secs;
        setChanged();
    }

    public ZephyrWindSystem.WindDirection getTargetDirection() { return targetDirection; }
    public void setTargetDirection(ZephyrWindSystem.WindDirection dir) {
        if (!isLinked) {
            this.targetDirection = dir;
            setChanged();
            if (this.level != null && !this.level.isClientSide()) this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public boolean isLinked() { return isLinked; }
    public BlockPos getLinkedDockPos() { return linkedDockPos; }
    public String getLinkedDockName() { return linkedDockName; }
    public String getLinkedDockDimension() { return linkedDockDimension; }

    public void linkToDock(BlockPos dockPos, String name, String dimension) {
        this.isLinked = true;
        this.linkedDockPos = dockPos;
        this.linkedDockName = name;
        this.linkedDockDimension = dimension;

        double dx = dockPos.getX() - this.worldPosition.getX();
        double dz = dockPos.getZ() - this.worldPosition.getZ();
        double angle = (Math.toDegrees(Math.atan2(dz, dx)) + 360) % 360;

        if (angle >= 337.5 || angle < 22.5) this.targetDirection = ZephyrWindSystem.WindDirection.EAST;
        else if (angle >= 22.5 && angle < 67.5) this.targetDirection = ZephyrWindSystem.WindDirection.SOUTH_EAST;
        else if (angle >= 67.5 && angle < 112.5) this.targetDirection = ZephyrWindSystem.WindDirection.SOUTH;
        else if (angle >= 112.5 && angle < 157.5) this.targetDirection = ZephyrWindSystem.WindDirection.SOUTH_WEST;
        else if (angle >= 157.5 && angle < 202.5) this.targetDirection = ZephyrWindSystem.WindDirection.WEST;
        else if (angle >= 202.5 && angle < 247.5) this.targetDirection = ZephyrWindSystem.WindDirection.NORTH_WEST;
        else if (angle >= 247.5 && angle < 292.5) this.targetDirection = ZephyrWindSystem.WindDirection.NORTH;
        else if (angle >= 292.5 && angle < 337.5) this.targetDirection = ZephyrWindSystem.WindDirection.NORTH_EAST;

        setChanged();
        if (this.level != null && !this.level.isClientSide()) this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public void unlink() {
        this.isLinked = false;
        this.linkedDockPos = BlockPos.ZERO;
        this.linkedDockName = "";
        this.linkedDockDimension = "";
        setChanged();
        if (this.level != null && !this.level.isClientSide()) this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public void tick() {
        if (this.level == null || this.level.isClientSide()) return;
        if (this.cooldownTimer > 0) this.cooldownTimer--;
        if (this.pulseTimer > 0) {
            this.pulseTimer--;
            if (this.pulseTimer == 0) {
                BlockState state = this.getBlockState();
                if (state.hasProperty(com.zephyrhauler.block.WindSensorBlock.POWER)) {
                    this.level.setBlock(this.worldPosition, state.setValue(com.zephyrhauler.block.WindSensorBlock.POWER, 0), 3);
                }
            }
        }

        if (this.cooldownTimer == 0 && this.pulseTimer == 0 && this.level.getGameTime() % 20 == 0) {
            ZephyrWindSystem.WindInfo wind = ZephyrWindSystem.getCurrentWind(this.level);

            if (wind.direction() == this.targetDirection) {
                BlockState state = this.getBlockState();
                if (state.hasProperty(com.zephyrhauler.block.WindSensorBlock.POWER)) {
                    this.level.setBlock(this.worldPosition, state.setValue(com.zephyrhauler.block.WindSensorBlock.POWER, this.configuredPower), 3);
                }

                this.pulseTimer = 20;
                this.cooldownTimer = this.configuredCooldown * 20;
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            return state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.zephyr_hauler.wind_sensor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new com.zephyrhauler.menu.WindSensorMenu(id, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Power", this.configuredPower);
        tag.putInt("Cooldown", this.configuredCooldown);
        tag.putString("Direction", this.targetDirection.name());
        tag.putBoolean("Linked", this.isLinked);
        if (this.isLinked) {
            tag.putLong("DockPos", this.linkedDockPos.asLong());
            tag.putString("DockName", this.linkedDockName);
            tag.putString("DockDim", this.linkedDockDimension);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Power")) this.configuredPower = tag.getInt("Power");
        if (tag.contains("Cooldown")) this.configuredCooldown = tag.getInt("Cooldown");
        if (tag.contains("Direction")) {
            try { this.targetDirection = ZephyrWindSystem.WindDirection.valueOf(tag.getString("Direction")); }
            catch (Exception ignored) {}
        }
        if (tag.contains("Linked")) this.isLinked = tag.getBoolean("Linked");
        if (this.isLinked && tag.contains("DockPos")) {
            this.linkedDockPos = BlockPos.of(tag.getLong("DockPos"));
            this.linkedDockName = tag.getString("DockName");
            this.linkedDockDimension = tag.getString("DockDim");
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
}