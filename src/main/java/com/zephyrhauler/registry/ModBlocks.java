package com.zephyrhauler.registry;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.block.ZephyrDockBlock;
import com.zephyrhauler.block.ZephyrStationBlock;
import com.zephyrhauler.block.ZephyrCableBlock;
import com.zephyrhauler.block.ZephyrHubBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ZephyrHauler.MOD_ID);

    public static final DeferredBlock<ZephyrDockBlock> ZEPHYR_DOCK = BLOCKS.register("zephyr_dock",
            () -> new ZephyrDockBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<Block> ZEPHYR_STATION = BLOCKS.register("zephyr_station",
            () -> new ZephyrStationBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<Block> ZEPHYR_AUTO_STATION = BLOCKS.register("zephyr_auto_station",
            () -> new com.zephyrhauler.block.ZephyrAutoStationBlock(BlockBehaviour.Properties.of()
                    .strength(3.5f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<Block> ZEPHYR_LAUNCHER = BLOCKS.register("zephyr_launcher",
            () -> new com.zephyrhauler.block.ZephyrLauncherBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<Block> WIND_SENSOR = BLOCKS.register("wind_sensor",
            () -> new com.zephyrhauler.block.WindSensorBlock(BlockBehaviour.Properties.of()
                    .strength(2.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<Block> ZEPHYR_HUB = BLOCKS.register("zephyr_hub",
            () -> new ZephyrHubBlock(BlockBehaviour.Properties.of()
                    .strength(4.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final DeferredBlock<Block> ZEPHYR_CABLE = BLOCKS.register("zephyr_cable",
            () -> new ZephyrCableBlock(BlockBehaviour.Properties.of()
                    .strength(1.5f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));
}