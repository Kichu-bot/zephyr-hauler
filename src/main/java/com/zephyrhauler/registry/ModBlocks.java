package com.zephyrhauler.registry;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.block.ZephyrDockBlock;
import com.zephyrhauler.block.ZephyrStationBlock;
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
            () -> new ZephyrStationBlock(BlockBehaviour.Properties.of().strength(2.0f).noOcclusion()));
}