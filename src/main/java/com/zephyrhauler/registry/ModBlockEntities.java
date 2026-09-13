package com.zephyrhauler.registry;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.block.entity.ZephyrDockBlockEntity;
import com.zephyrhauler.block.entity.ZephyrStationBlockEntity;
import com.zephyrhauler.block.entity.ZephyrHubBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ZephyrHauler.MOD_ID);

    public static final Supplier<BlockEntityType<ZephyrDockBlockEntity>> ZEPHYR_DOCK_BE =
            BLOCK_ENTITIES.register("zephyr_dock_be",
                    () -> BlockEntityType.Builder.of(ZephyrDockBlockEntity::new, ModBlocks.ZEPHYR_DOCK.get()).build(null));

    public static final Supplier<BlockEntityType<ZephyrStationBlockEntity>> ZEPHYR_STATION_BE =
            BLOCK_ENTITIES.register("zephyr_station_be", () -> BlockEntityType.Builder.of(ZephyrStationBlockEntity::new, ModBlocks.ZEPHYR_STATION.get()).build(null));

    public static final Supplier<BlockEntityType<com.zephyrhauler.block.entity.ZephyrAutoStationBlockEntity>> ZEPHYR_AUTO_STATION_BE =
            BLOCK_ENTITIES.register("zephyr_auto_station_be", () -> BlockEntityType.Builder.of(com.zephyrhauler.block.entity.ZephyrAutoStationBlockEntity::new, ModBlocks.ZEPHYR_AUTO_STATION.get()).build(null));

    public static final Supplier<net.minecraft.world.level.block.entity.BlockEntityType<com.zephyrhauler.block.entity.ZephyrLauncherBlockEntity>> ZEPHYR_LAUNCHER_BE =
            BLOCK_ENTITIES.register("zephyr_launcher_be",
                    () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(com.zephyrhauler.block.entity.ZephyrLauncherBlockEntity::new, ModBlocks.ZEPHYR_LAUNCHER.get()).build(null));

    public static final Supplier<net.minecraft.world.level.block.entity.BlockEntityType<com.zephyrhauler.block.entity.WindSensorBlockEntity>> WIND_SENSOR_BE =
            BLOCK_ENTITIES.register("wind_sensor_be",
                    () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(com.zephyrhauler.block.entity.WindSensorBlockEntity::new, ModBlocks.WIND_SENSOR.get()).build(null));

    public static final Supplier<BlockEntityType<ZephyrHubBlockEntity>> ZEPHYR_HUB_BE =
            BLOCK_ENTITIES.register("zephyr_hub_be",
                    () -> BlockEntityType.Builder.of(ZephyrHubBlockEntity::new, ModBlocks.ZEPHYR_HUB.get()).build(null));
}