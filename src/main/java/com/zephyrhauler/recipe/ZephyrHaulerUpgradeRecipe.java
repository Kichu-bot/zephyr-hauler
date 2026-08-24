package com.zephyrhauler.recipe;

import com.zephyrhauler.component.ZephyrDataComponents;
import com.zephyrhauler.item.ZephyrHaulerItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class ZephyrHaulerUpgradeRecipe extends CustomRecipe {

    public ZephyrHaulerUpgradeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean hasHauler = false;
        boolean hasUpgrade = false;
        ItemStack haulerStack = ItemStack.EMPTY;
        ItemStack upgradeStack = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof ZephyrHaulerItem) {
                    if (hasHauler) return false;
                    hasHauler = true;
                    haulerStack = stack;
                } else if (isUpgradeItem(stack)) {
                    if (hasUpgrade) return false;
                    hasUpgrade = true;
                    upgradeStack = stack;
                } else {
                    return false;
                }
            }
        }

        if (hasHauler && hasUpgrade) {
            String upgradeType = getUpgradeType(upgradeStack);
            List<String> currentUpgrades = haulerStack.getOrDefault(ZephyrDataComponents.HAULER_UPGRADES.get(), new ArrayList<>());

            if (upgradeType.equals("netherite_plating") && !currentUpgrades.contains("reinforced")) {
                return false;
            }

            return !currentUpgrades.contains(upgradeType);
        }

        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack haulerStack = ItemStack.EMPTY;
        ItemStack upgradeStack = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof ZephyrHaulerItem) {
                    haulerStack = stack;
                } else if (isUpgradeItem(stack)) {
                    upgradeStack = stack;
                }
            }
        }

        if (haulerStack.isEmpty() || upgradeStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = haulerStack.copy();
        List<String> upgrades = new ArrayList<>(result.getOrDefault(ZephyrDataComponents.HAULER_UPGRADES.get(), new ArrayList<>()));
        String upgradeType = getUpgradeType(upgradeStack);

        if (upgradeType.equals("netherite_plating")) {
            upgrades.remove("reinforced");
        }

        if (!upgrades.contains(upgradeType)) {
            upgrades.add(upgradeType);
        }

        result.set(ZephyrDataComponents.HAULER_UPGRADES.get(), upgrades);

        int currentDurability = result.getOrDefault(ZephyrDataComponents.HAULER_DURABILITY.get(), 8);
        if (upgradeType.equals("netherite_plating")) {
            result.set(ZephyrDataComponents.HAULER_DURABILITY.get(), Math.max(currentDurability, 32));
        } else if (upgradeType.equals("reinforced")) {
            result.set(ZephyrDataComponents.HAULER_DURABILITY.get(), currentDurability + 4);
        }

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return com.zephyrhauler.registry.ModRecipes.HAULER_UPGRADE.get();
    }

    private boolean isUpgradeItem(ItemStack stack) {
        return stack.is(Items.HONEYCOMB) || stack.is(Items.LIGHTNING_ROD) ||
                stack.is(Items.PHANTOM_MEMBRANE) || stack.is(Items.IRON_INGOT) ||
                stack.is(Items.SHULKER_SHELL) || stack.is(Items.SLIME_BLOCK) ||
                stack.is(Items.MAGMA_BLOCK) || stack.is(Items.NETHERITE_INGOT);
    }

    private String getUpgradeType(ItemStack stack) {
        if (stack.is(Items.HONEYCOMB)) return "waxed";
        if (stack.is(Items.LIGHTNING_ROD)) return "lightning_proof";
        if (stack.is(Items.PHANTOM_MEMBRANE)) return "aerodynamic";
        if (stack.is(Items.IRON_INGOT)) return "reinforced";
        if (stack.is(Items.SHULKER_SHELL)) return "shulker_buoyancy";
        if (stack.is(Items.SLIME_BLOCK)) return "shock_absorbers";
        if (stack.is(Items.MAGMA_BLOCK)) return "supercharged_burner";
        if (stack.is(Items.NETHERITE_INGOT)) return "netherite_plating";
        return "";
    }
}