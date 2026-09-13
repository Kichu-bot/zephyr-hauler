package com.zephyrhauler.registry;

import com.zephyrhauler.ZephyrHauler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = ZephyrHauler.MOD_ID, value = Dist.CLIENT)
public class ModCapabilities {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ZEPHYR_AUTO_STATION_BE.get(),
                (be, side) -> be.inventory
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ZEPHYR_LAUNCHER_BE.get(),
                (be, side) -> be.inventory
        );
    }

}