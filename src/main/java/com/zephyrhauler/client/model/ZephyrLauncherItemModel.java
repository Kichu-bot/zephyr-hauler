package com.zephyrhauler.client.model;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.item.ZephyrLauncherItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ZephyrLauncherItemModel extends GeoModel<ZephyrLauncherItem> {
    @Override
    public ResourceLocation getModelResource(ZephyrLauncherItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "geo/zephyr_launcher.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ZephyrLauncherItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "textures/block/zephyr_launcher.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ZephyrLauncherItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "animations/zephyr_launcher.animation.json");
    }
}