package com.zephyrhauler.registry;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.recipe.ZephyrHaulerDyeRecipe;
import com.zephyrhauler.recipe.ZephyrHaulerResetRecipe;
import com.zephyrhauler.recipe.ZephyrHaulerUpgradeRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ZephyrHauler.MOD_ID);

    public static final Supplier<RecipeSerializer<ZephyrHaulerResetRecipe>> RESET_HAULER =
            SERIALIZERS.register("reset_hauler", () -> new SimpleCraftingRecipeSerializer<>(ZephyrHaulerResetRecipe::new));

    public static final DeferredRegister<RecipeSerializer<?>> RECIPES =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ZephyrHauler.MOD_ID);

    public static final Supplier<RecipeSerializer<?>> HAULER_UPGRADE = RECIPES.register("hauler_upgrade",
            () -> new SimpleCraftingRecipeSerializer<>(ZephyrHaulerUpgradeRecipe::new));

    public static final Supplier<RecipeSerializer<ZephyrHaulerDyeRecipe>> DYE_HAULER =
            SERIALIZERS.register("dye_hauler", () -> new SimpleCraftingRecipeSerializer<>(ZephyrHaulerDyeRecipe::new));
}