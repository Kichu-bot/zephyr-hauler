package com.zephyrhauler.client.model;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.block.entity.ZephyrLauncherBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ZephyrLauncherModel extends GeoModel<ZephyrLauncherBlockEntity> {
    @Override
    public ResourceLocation getModelResource(ZephyrLauncherBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "geo/zephyr_launcher.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ZephyrLauncherBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "textures/block/zephyr_launcher.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ZephyrLauncherBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "animations/zephyr_launcher.animation.json");
    }
}