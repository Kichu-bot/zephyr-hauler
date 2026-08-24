package com.zephyrhauler.block.entity;

import com.zephyrhauler.entity.ZephyrHaulerEntity;
import com.zephyrhauler.registry.ModBlockEntities;
import com.zephyrhauler.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// --- IMPORTACIONES DE GECKOLIB ---
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ZephyrDockBlockEntity extends BlockEntity implements GeoBlockEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private String customName = "";
    private boolean isOccupied = false;
    private CompoundTag pendingDeliveryData = null;
    private java.util.UUID linkId = null;

    public ZephyrDockBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ZEPHYR_DOCK_BE.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
    public java.util.UUID getLinkId() { return linkId; }
    public void setLinkId(java.util.UUID linkId) { this.linkId = linkId; this.setChanged(); }

    public String getCustomName() {
        return this.customName != null ? this.customName : "";
    }

    public void setCustomName(String customName) {
        this.customName = customName;
        this.setChanged();

        if (this.level != null && !this.level.isClientSide()) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    public boolean isOccupied() { return isOccupied; }
    public void setOccupied(boolean occupied) { this.isOccupied = occupied; this.setChanged(); }

    public CompoundTag getPendingDeliveryData() { return pendingDeliveryData; }
    public void setPendingDeliveryData(CompoundTag pendingDeliveryData) { this.pendingDeliveryData = pendingDeliveryData; this.setChanged(); }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.customName != null && !this.customName.isEmpty()) {
            tag.putString("CustomName", this.customName);
        }
        tag.putBoolean("IsOccupied", this.isOccupied);
        if (this.pendingDeliveryData != null) {
            tag.put("PendingDeliveryData", this.pendingDeliveryData);
        }
        if (this.linkId != null) {
            tag.putUUID("LinkId", this.linkId);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("CustomName")) {
            this.customName = tag.getString("CustomName");
        } else {
            this.customName = "";
        }

        this.isOccupied = tag.getBoolean("IsOccupied");

        if (tag.contains("PendingDeliveryData")) {
            this.pendingDeliveryData = tag.getCompound("PendingDeliveryData");
        } else {
            this.pendingDeliveryData = null;
        }

        if (tag.contains("LinkId")) {
            this.linkId = tag.getUUID("LinkId");
        } else {
            this.linkId = null;
        }
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        net.minecraft.network.chat.Component nameComponent = componentInput.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME);
        if (nameComponent != null) {
            this.customName = nameComponent.getString();
        }
    }

    @Override
    protected void collectImplicitComponents(net.minecraft.core.component.DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (this.customName != null && !this.customName.isEmpty()) {
            components.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal(this.customName));
        }
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide() && this.pendingDeliveryData != null) {

            ZephyrHaulerEntity hauler = ModEntities.ZEPHYR_HAULER.get().create(level);
            if (hauler != null) {
                BlockState capturedState = net.minecraft.nbt.NbtUtils.readBlockState(level.holderLookup(net.minecraft.core.registries.Registries.BLOCK), this.pendingDeliveryData.getCompound("BlockState"));
                CompoundTag beData = this.pendingDeliveryData.getCompound("BlockEntityData");
                ItemStack haulerItem = ItemStack.parseOptional(level.registryAccess(), this.pendingDeliveryData.getCompound("HaulerItem"));

                hauler.setPos(pos.getX() + 0.5, pos.getY() + 60.0, pos.getZ() + 0.5);
                hauler.setCapturedData(capturedState, beData, haulerItem);
                hauler.setDescending(true);

                level.addFreshEntity(hauler);
            }
            this.pendingDeliveryData = null;
            this.setChanged();
        }
    }
}