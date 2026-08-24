package com.zephyrhauler;

import com.zephyrhauler.registry.*;
import com.zephyrhauler.component.ZephyrDataComponents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ZephyrHauler.MOD_ID)
public class ZephyrHauler {

    public static final String MOD_ID = "zephyr_hauler";

    public ZephyrHauler(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ZephyrDataComponents.COMPONENTS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModRecipes.SERIALIZERS.register(modEventBus);
        ModRecipes.RECIPES.register(modEventBus);
    }
}