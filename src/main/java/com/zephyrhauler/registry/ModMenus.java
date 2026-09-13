package com.zephyrhauler.registry;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.menu.ZephyrControllerMenu;
import com.zephyrhauler.menu.ZephyrAutoStationMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, ZephyrHauler.MOD_ID);

    public static final Supplier<MenuType<ZephyrControllerMenu>> ZEPHYR_CONTROLLER_MENU =
            MENUS.register("zephyr_controller_menu", () -> new MenuType<>(ZephyrControllerMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<ZephyrAutoStationMenu>> ZEPHYR_AUTO_STATION_MENU =
            MENUS.register("zephyr_auto_station_menu", () -> IMenuTypeExtension.create(ZephyrAutoStationMenu::new));

    public static final Supplier<MenuType<com.zephyrhauler.menu.ZephyrLauncherMenu>> ZEPHYR_LAUNCHER_MENU =
            MENUS.register("zephyr_launcher_menu", () -> IMenuTypeExtension.create(com.zephyrhauler.menu.ZephyrLauncherMenu::new));

    public static final Supplier<MenuType<com.zephyrhauler.menu.WindSensorMenu>> WIND_SENSOR_MENU =
            MENUS.register("wind_sensor_menu", () -> IMenuTypeExtension.create(com.zephyrhauler.menu.WindSensorMenu::new));
}