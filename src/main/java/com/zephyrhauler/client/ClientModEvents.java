package com.zephyrhauler.client;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.client.gui.screen.ZephyrControllerScreen;
import com.zephyrhauler.client.renderer.ZephyrHaulerItemRenderer;
import com.zephyrhauler.client.renderer.ZephyrControllerItemRenderer;
import com.zephyrhauler.client.renderer.ZephyrStationItemRenderer;
import com.zephyrhauler.client.renderer.ZephyrDockItemRenderer;
import com.zephyrhauler.client.renderer.WindMeterRenderer; // <-- IMPORTACIÓN DEL WIND METER
import com.zephyrhauler.client.renderer.ZephyrHaulerRenderer;
import com.zephyrhauler.client.renderer.block.ZephyrStationRenderer;
import com.zephyrhauler.client.renderer.block.ZephyrDockRenderer;
import com.zephyrhauler.registry.ModBlockEntities;
import com.zephyrhauler.registry.ModEntities;
import com.zephyrhauler.registry.ModItems;
import com.zephyrhauler.registry.ModMenus;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = ZephyrHauler.MOD_ID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ZEPHYR_HAULER.get(), ZephyrHaulerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ZEPHYR_STATION_BE.get(), ZephyrStationRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ZEPHYR_DOCK_BE.get(), ZephyrDockRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.ZEPHYR_CONTROLLER_MENU.get(), ZephyrControllerScreen::new);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private ZephyrHaulerItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new ZephyrHaulerItemRenderer();
                }
                return this.renderer;
            }
        }, ModItems.ZEPHYR_HAULER.get());

        event.registerItem(new IClientItemExtensions() {
            private ZephyrControllerItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new ZephyrControllerItemRenderer();
                }
                return this.renderer;
            }
        }, ModItems.ZEPHYR_CONTROLLER.get());

        event.registerItem(new IClientItemExtensions() {
            private ZephyrStationItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new ZephyrStationItemRenderer();
                }
                return this.renderer;
            }
        }, ModItems.ZEPHYR_STATION_ITEM.get());

        event.registerItem(new IClientItemExtensions() {
            private ZephyrDockItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new ZephyrDockItemRenderer();
                }
                return this.renderer;
            }
        }, ModItems.ZEPHYR_DOCK_ITEM.get());

        event.registerItem(new IClientItemExtensions() {
            private WindMeterRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new WindMeterRenderer();
                }
                return this.renderer;
            }
        }, ModItems.WIND_METER.get());
    }
}