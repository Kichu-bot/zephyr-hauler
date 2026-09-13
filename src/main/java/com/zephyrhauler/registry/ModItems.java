package com.zephyrhauler.registry;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.item.WindMeterItem;
import com.zephyrhauler.item.ZephyrDockItem;
import com.zephyrhauler.item.ZephyrHaulerItem;
import com.zephyrhauler.item.ZephyrStationItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ZephyrHauler.MOD_ID);

    public static final DeferredItem<Item> ZEPHYR_HAULER = ITEMS.register("zephyr_hauler",
            () -> new ZephyrHaulerItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<BlockItem> ZEPHYR_DOCK_ITEM = ITEMS.register("zephyr_dock",
            () -> new ZephyrDockItem(ModBlocks.ZEPHYR_DOCK.get(), new Item.Properties()));

    public static final DeferredItem<Item> ZEPHYR_CONTROLLER = ITEMS.register("zephyr_controller",
            () -> new com.zephyrhauler.item.ZephyrControllerItem(new Item.Properties()));

    public static final DeferredItem<BlockItem> ZEPHYR_STATION_ITEM = ITEMS.register("zephyr_station",
            () -> new ZephyrStationItem(ModBlocks.ZEPHYR_STATION.get(), new Item.Properties()));

    public static final DeferredItem<Item> WIND_METER = ITEMS.register("wind_meter",
            () -> new WindMeterItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<com.zephyrhauler.item.ZephyrAutoStationItem> ZEPHYR_AUTO_STATION_ITEM = ITEMS.register("zephyr_auto_station",
            () -> new com.zephyrhauler.item.ZephyrAutoStationItem(ModBlocks.ZEPHYR_AUTO_STATION.get(), new Item.Properties()));

    public static final DeferredItem<net.minecraft.world.item.BlockItem> ZEPHYR_LAUNCHER_ITEM = ITEMS.register("zephyr_launcher",
            () -> new com.zephyrhauler.item.ZephyrLauncherItem(ModBlocks.ZEPHYR_LAUNCHER.get(), new net.minecraft.world.item.Item.Properties()));

    public static final DeferredItem<net.minecraft.world.item.BlockItem> WIND_SENSOR_ITEM = ITEMS.register("wind_sensor",
            () -> new com.zephyrhauler.item.WindSensorItem(ModBlocks.WIND_SENSOR.get(), new net.minecraft.world.item.Item.Properties()));

    public static final Supplier<Item> ZEPHYR_HUB_ITEM = ITEMS.register("zephyr_hub",
            () -> new com.zephyrhauler.item.ZephyrHubItem(ModBlocks.ZEPHYR_HUB.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> ZEPHYR_CABLE_ITEM = ITEMS.register("zephyr_cable",
            () -> new BlockItem(ModBlocks.ZEPHYR_CABLE.get(), new Item.Properties()));
}