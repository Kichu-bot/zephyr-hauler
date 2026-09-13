package com.zephyrhauler.menu;

import com.zephyrhauler.block.entity.ZephyrAutoStationBlockEntity;
import com.zephyrhauler.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ZephyrAutoStationMenu extends AbstractContainerMenu {
    public final ZephyrAutoStationBlockEntity blockEntity;
    private final ContainerData data;

    public ZephyrAutoStationMenu(int id, Inventory playerInv, FriendlyByteBuf extraData) {
        this(id, playerInv, playerInv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(1));
    }

    public ZephyrAutoStationMenu(int id, Inventory playerInv, BlockEntity entity, ContainerData data) {
        super(ModMenus.ZEPHYR_AUTO_STATION_MENU.get(), id);
        this.blockEntity = (ZephyrAutoStationBlockEntity) entity;
        this.data = data;

        this.addSlot(new SlotItemHandler(this.blockEntity.inventory, 0, 80, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof com.zephyrhauler.item.ZephyrHaulerItem;
            }
        });

        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new SlotItemHandler(this.blockEntity.inventory, 1 + j + i * 9, 8 + j * 18, 51 + i * 18));
            }
        }

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 102 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInv, k, 8 + k * 18, 160));
        }

        this.addDataSlots(data);
    }

    public boolean isAutoRepairEnabled() {
        return this.data.get(0) > 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            if (index < 19) {
                if (!this.moveItemStackTo(stackInSlot, 19, 55, true)) return ItemStack.EMPTY;
            } else {
                if (stackInSlot.getItem() instanceof com.zephyrhauler.item.ZephyrHaulerItem) {
                    if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) return ItemStack.EMPTY;
                } else {
                    if (!this.moveItemStackTo(stackInSlot, 1, 19, false)) return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();

            if (stackInSlot.getCount() == itemstack.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, stackInSlot);
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}