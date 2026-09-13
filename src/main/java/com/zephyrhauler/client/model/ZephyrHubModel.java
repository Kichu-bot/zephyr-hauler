package com.zephyrhauler.client.model;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.block.entity.ZephyrHubBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ZephyrHubModel extends GeoModel<ZephyrHubBlockEntity> {
    @Override
    public ResourceLocation getModelResource(ZephyrHubBlockEntity object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "geo/zephyr_hub.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ZephyrHubBlockEntity object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "textures/block/zephyr_hub.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ZephyrHubBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "animations/zephyr_hub.animation.json");
    }
}