package com.zephyrhauler.registry;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.item.WindMeterItem;
import com.zephyrhauler.item.ZephyrDockItem;
import com.zephyrhauler.item.ZephyrHaulerItem;
// Si tienes tu ZephyrStationItem en otro paquete, asegúrate de importarlo.
import com.zephyrhauler.item.ZephyrStationItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

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
}