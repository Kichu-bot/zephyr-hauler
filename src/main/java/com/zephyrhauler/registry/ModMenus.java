package com.zephyrhauler.registry;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.menu.ZephyrControllerMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, ZephyrHauler.MOD_ID);

    public static final Supplier<MenuType<ZephyrControllerMenu>> ZEPHYR_CONTROLLER_MENU =
            MENUS.register("zephyr_controller_menu", () -> new MenuType<>(ZephyrControllerMenu::new, FeatureFlags.DEFAULT_FLAGS));
}