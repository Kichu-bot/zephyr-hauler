package com.zephyrhauler.menu;

import com.zephyrhauler.block.entity.WindSensorBlockEntity;
import com.zephyrhauler.registry.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;

public class WindSensorMenu extends AbstractContainerMenu {
    public final WindSensorBlockEntity blockEntity;
    public final ContainerData data;

    public WindSensorMenu(int containerId, Inventory playerInv, net.minecraft.network.FriendlyByteBuf extraData) {
        this(containerId, playerInv, (WindSensorBlockEntity) playerInv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public WindSensorMenu(int containerId, Inventory playerInv, WindSensorBlockEntity blockEntity) {
        super(ModMenus.WIND_SENSOR_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        this.data = blockEntity.getContainerData();
        this.addDataSlots(this.data);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(net.minecraft.world.inventory.ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player, com.zephyrhauler.registry.ModBlocks.WIND_SENSOR.get());
    }
}