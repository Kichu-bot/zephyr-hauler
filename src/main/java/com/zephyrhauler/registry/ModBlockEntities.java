package com.zephyrhauler.registry;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.block.entity.ZephyrDockBlockEntity;
import com.zephyrhauler.block.entity.ZephyrStationBlockEntity;
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
}