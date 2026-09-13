package com.zephyrhauler.menu;

import com.zephyrhauler.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ZephyrControllerMenu extends AbstractContainerMenu {

    public final Container haulerContainer = new SimpleContainer(1);

    public ZephyrControllerMenu(int id, Inventory playerInv) {
        super(ModMenus.ZEPHYR_CONTROLLER_MENU.get(), id);

        this.addSlot(new Slot(haulerContainer, 0, 80, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof com.zephyrhauler.item.ZephyrHaulerItem ||
                        stack.getItem() instanceof com.zephyrhauler.item.WindSensorItem;
            }
        });

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInv, k, 8 + k * 18, 142));
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        ItemStack stackInSlot = this.haulerContainer.removeItemNoUpdate(0);
        if (!stackInSlot.isEmpty()) {
            if (!player.getInventory().add(stackInSlot)) {
                player.drop(stackInSlot, false);
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            if (index == 0) {
                if (!this.moveItemStackTo(stackInSlot, 1, 37, true)) return ItemStack.EMPTY;
            } else {
                if (stackInSlot.getItem() instanceof com.zephyrhauler.item.ZephyrHaulerItem ||
                        stackInSlot.getItem() instanceof com.zephyrhauler.item.WindSensorItem) {
                    if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) return ItemStack.EMPTY;
                } else {
                    return ItemStack.EMPTY;
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
        return player.getMainHandItem().getItem() instanceof com.zephyrhauler.item.ZephyrControllerItem ||
                player.getOffhandItem().getItem() instanceof com.zephyrhauler.item.ZephyrControllerItem;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < this.slots.size()) {
            Slot slot = this.slots.get(slotId);
            if (slot != null && slot.hasItem() && slot.getItem().getItem() instanceof com.zephyrhauler.item.ZephyrControllerItem) {
                return;
            }
        }

        if (clickType == ClickType.SWAP) {
            ItemStack stackInHotbar = player.getInventory().getItem(button);
            if (!stackInHotbar.isEmpty() && stackInHotbar.getItem() instanceof com.zephyrhauler.item.ZephyrControllerItem) {
                return;
            }
        }

        super.clicked(slotId, button, clickType, player);
    }
}