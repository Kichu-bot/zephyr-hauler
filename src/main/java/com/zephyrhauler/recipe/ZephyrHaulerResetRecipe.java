package com.zephyrhauler.recipe;

import com.zephyrhauler.component.ZephyrDataComponents;
import com.zephyrhauler.registry.ModItems;
import com.zephyrhauler.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class ZephyrHaulerResetRecipe extends CustomRecipe {

    public ZephyrHaulerResetRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        int haulerCount = 0;
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() == ModItems.ZEPHYR_HAULER.get()) {
                    haulerCount++;
                } else {
                    return false;
                }
            }
        }
        return haulerCount == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider provider) {
        ItemStack inputStack = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == ModItems.ZEPHYR_HAULER.get()) {
                inputStack = stack;
                break;
            }
        }

        if (inputStack.isEmpty()) return ItemStack.EMPTY;

        ItemStack outputStack = inputStack.copy();

        outputStack.remove(ZephyrDataComponents.TARGET_POS.get());
        outputStack.remove(ZephyrDataComponents.TARGET_NAME.get());
        outputStack.remove(ZephyrDataComponents.LINK_ID.get());

        return outputStack;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.RESET_HAULER.get();
    }
}