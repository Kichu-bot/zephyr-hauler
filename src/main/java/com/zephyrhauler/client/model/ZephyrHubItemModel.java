package com.zephyrhauler.client.model;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.item.ZephyrHubItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ZephyrHubItemModel extends GeoModel<ZephyrHubItem> {
    @Override
    public ResourceLocation getModelResource(ZephyrHubItem object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "geo/zephyr_hub.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ZephyrHubItem object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "textures/block/zephyr_hub.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ZephyrHubItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "animations/zephyr_hub.animation.json");
    }
}