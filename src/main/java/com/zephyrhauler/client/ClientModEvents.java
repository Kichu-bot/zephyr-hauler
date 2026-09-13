package com.zephyrhauler.client;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.client.gui.screen.ZephyrControllerScreen;
import com.zephyrhauler.client.gui.screen.ZephyrAutoStationScreen;
import com.zephyrhauler.client.gui.screen.ZephyrLauncherScreen;
import com.zephyrhauler.client.renderer.*;
import com.zephyrhauler.client.renderer.block.ZephyrAutoStationRenderer;
import com.zephyrhauler.client.renderer.block.ZephyrStationRenderer;
import com.zephyrhauler.client.renderer.block.ZephyrDockRenderer;
import com.zephyrhauler.client.renderer.block.ZephyrLauncherRenderer;
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
        event.registerBlockEntityRenderer(ModBlockEntities.ZEPHYR_AUTO_STATION_BE.get(), ZephyrAutoStationRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ZEPHYR_LAUNCHER_BE.get(), ZephyrLauncherRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.WIND_SENSOR_BE.get(), com.zephyrhauler.client.renderer.block.WindSensorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ZEPHYR_HUB_BE.get(), com.zephyrhauler.client.renderer.block.ZephyrHubRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.ZEPHYR_CONTROLLER_MENU.get(), ZephyrControllerScreen::new);
        event.register(ModMenus.ZEPHYR_AUTO_STATION_MENU.get(), ZephyrAutoStationScreen::new);
        event.register(ModMenus.ZEPHYR_LAUNCHER_MENU.get(), ZephyrLauncherScreen::new);
        event.register(ModMenus.WIND_SENSOR_MENU.get(), com.zephyrhauler.client.gui.screen.WindSensorScreen::new);
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

        event.registerItem(new IClientItemExtensions() {
            private ZephyrAutoStationItemRenderer renderer;
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new ZephyrAutoStationItemRenderer();
                }
                return this.renderer;
            }
        }, ModItems.ZEPHYR_AUTO_STATION_ITEM.get());

        event.registerItem(new IClientItemExtensions() {
            private ZephyrLauncherItemRenderer renderer;
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new ZephyrLauncherItemRenderer();
                }
                return this.renderer;
            }
        }, ModItems.ZEPHYR_LAUNCHER_ITEM.get());

        event.registerItem(new IClientItemExtensions() {
            private WindSensorItemRenderer renderer;
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new WindSensorItemRenderer();
                }
                return this.renderer;
            }
        }, ModItems.WIND_SENSOR_ITEM.get());

        event.registerItem(new IClientItemExtensions() {
            private ZephyrHubItemRenderer renderer;
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new ZephyrHubItemRenderer();
                }
                return this.renderer;
            }
        }, ModItems.ZEPHYR_HUB_ITEM.get());
    }
}