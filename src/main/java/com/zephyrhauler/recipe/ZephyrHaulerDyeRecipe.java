package com.zephyrhauler.recipe;

import com.zephyrhauler.component.ZephyrDataComponents;
import com.zephyrhauler.registry.ModItems;
import com.zephyrhauler.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class ZephyrHaulerDyeRecipe extends CustomRecipe {

    public ZephyrHaulerDyeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        boolean hasHauler = false;
        boolean hasDye = false;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() == ModItems.ZEPHYR_HAULER.get()) {
                    if (hasHauler) return false;
                    hasHauler = true;
                } else if (stack.getItem() instanceof DyeItem) {
                    if (hasDye) return false;
                    hasDye = true;
                } else {
                    return false;
                }
            }
        }
        return hasHauler && hasDye;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider provider) {
        ItemStack haulerStack = ItemStack.EMPTY;
        ItemStack dyeStack = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() == ModItems.ZEPHYR_HAULER.get()) {
                    haulerStack = stack;
                } else if (stack.getItem() instanceof DyeItem) {
                    dyeStack = stack;
                }
            }
        }

        if (haulerStack.isEmpty() || dyeStack.isEmpty()) return ItemStack.EMPTY;

        ItemStack outputStack = haulerStack.copy();
        String newColor = ((DyeItem) dyeStack.getItem()).getDyeColor().getName();
        outputStack.set(ZephyrDataComponents.HAULER_COLOR.get(), newColor);

        return outputStack;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.DYE_HAULER.get();
    }
}