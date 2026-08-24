package com.zephyrhauler.client.model;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.item.ZephyrControllerItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ZephyrControllerItemModel extends GeoModel<ZephyrControllerItem> {

    @Override
    public ResourceLocation getModelResource(ZephyrControllerItem object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "geo/zephyr_controller.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ZephyrControllerItem object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "textures/item/zephyr_controller.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ZephyrControllerItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "animations/zephyr_controller.animation.json");
    }
}