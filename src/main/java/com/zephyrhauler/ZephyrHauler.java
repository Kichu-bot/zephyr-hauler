package com.zephyrhauler;

import com.zephyrhauler.component.ZephyrDataComponents;
import com.zephyrhauler.config.ZephyrClientConfig;
import com.zephyrhauler.registry.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(ZephyrHauler.MOD_ID)
public class ZephyrHauler {

    public static final String MOD_ID = "zephyr_hauler";

    public ZephyrHauler(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ZephyrDataComponents.COMPONENTS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModRecipes.SERIALIZERS.register(modEventBus);
        ModRecipes.RECIPES.register(modEventBus);
        ModParticles.PARTICLES.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.CLIENT, ZephyrClientConfig.SPEC);
    }
}