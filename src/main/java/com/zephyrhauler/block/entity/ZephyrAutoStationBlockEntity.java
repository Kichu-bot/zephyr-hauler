package com.zephyrhauler.block.entity;

import com.zephyrhauler.component.ZephyrDataComponents;
import com.zephyrhauler.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class ZephyrAutoStationBlockEntity extends BlockEntity implements GeoBlockEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public final ItemStackHandler inventory = new ItemStackHandler(19) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == 0) {
                return stack.getItem() instanceof com.zephyrhauler.item.ZephyrHaulerItem;
            }
            return true;
        }
    };

    private boolean autoRepairEnabled = false;
    private int repairCooldown = 0;

    public ZephyrAutoStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ZEPHYR_AUTO_STATION_BE.get(), pos, state);
    }

    public boolean isAutoRepairEnabled() { return autoRepairEnabled; }
    public void setAutoRepairEnabled(boolean enabled) { this.autoRepairEnabled = enabled; this.setChanged(); }

    public void tick() {
        if (level == null || level.isClientSide()) return;

        if (repairCooldown > 0) {
            repairCooldown--;
            return;
        }

        if (!autoRepairEnabled) return;

        ItemStack hauler = inventory.getStackInSlot(0);
        if (hauler.isEmpty()) return;

        List<String> upgrades = hauler.getOrDefault(ZephyrDataComponents.HAULER_UPGRADES.get(), new java.util.ArrayList<>());
        int maxDur = upgrades.contains("netherite_plating") ? 32 : (upgrades.contains("reinforced") ? 12 : 8);
        int durability = hauler.getOrDefault(ZephyrDataComponents.HAULER_DURABILITY.get(), maxDur);

        if (durability <= 0 && hauler.has(ZephyrDataComponents.HAULER_DAMAGE.get())) {
            String damageType = hauler.getOrDefault(ZephyrDataComponents.HAULER_DAMAGE.get(), "unknown");

            for (int i = 1; i < 19; i++) {
                ItemStack supply = inventory.getStackInSlot(i);
                if (!supply.isEmpty() && isRepairMaterial(damageType, supply)) {
                    supply.shrink(1);

                    hauler.set(ZephyrDataComponents.HAULER_DURABILITY.get(), 1);
                    hauler.remove(ZephyrDataComponents.HAULER_DAMAGE.get());
                    hauler.set(ZephyrDataComponents.PREVENTIVE_REQ.get(), rollPreventiveRequirement(upgrades, level.random));

                    playRepairEffects(damageType, (ServerLevel) level, worldPosition);
                    repairCooldown = 20;
                    setChanged();
                    return;
                }
            }
        }
        else if (durability > 0 && durability < maxDur) {
            String preventiveReq = hauler.getOrDefault(ZephyrDataComponents.PREVENTIVE_REQ.get(), "wool");

            for (int i = 1; i < 19; i++) {
                ItemStack supply = inventory.getStackInSlot(i);
                if (!supply.isEmpty() && isPreventiveMaterial(preventiveReq, supply)) {
                    supply.shrink(1);

                    int healAmount = Math.max(2, maxDur / 4);
                    int newDurability = Math.min(maxDur, durability + healAmount);
                    hauler.set(ZephyrDataComponents.HAULER_DURABILITY.get(), newDurability);

                    playPreventiveEffects(preventiveReq, (ServerLevel) level, worldPosition);

                    if (newDurability == maxDur) {
                        hauler.remove(ZephyrDataComponents.PREVENTIVE_REQ.get());
                    } else {
                        hauler.set(ZephyrDataComponents.PREVENTIVE_REQ.get(), rollPreventiveRequirement(upgrades, level.random));
                    }

                    repairCooldown = 20;
                    setChanged();
                    return;
                }
            }
        }
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

    private String rollPreventiveRequirement(List<String> upgrades, net.minecraft.util.RandomSource random) {
        java.util.List<String> pool = new java.util.ArrayList<>(java.util.List.of("wool", "string", "leather", "flint", "coal"));
        if (upgrades.contains("reinforced") || upgrades.contains("netherite_plating")) pool.addAll(java.util.List.of("iron", "copper", "iron_nugget", "chain", "redstone"));
        if (upgrades.contains("netherite_plating")) pool.addAll(java.util.List.of("netherite", "blaze_powder", "magma_cream"));
        return pool.get(random.nextInt(pool.size()));
    }

    private void playPreventiveEffects(String materialType, ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.3f, 2.0f);
    }

    private void playRepairEffects(String damageType, ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.5f, 1.5f);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putBoolean("AutoRepairEnabled", autoRepairEnabled);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
        autoRepairEnabled = tag.getBoolean("AutoRepairEnabled");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) { }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}