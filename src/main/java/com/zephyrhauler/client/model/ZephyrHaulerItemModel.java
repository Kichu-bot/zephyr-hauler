package com.zephyrhauler.client.model;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.item.ZephyrHaulerItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ZephyrHaulerItemModel extends GeoModel<ZephyrHaulerItem> {

    @Override
    public ResourceLocation getModelResource(ZephyrHaulerItem object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "geo/zephyr_hauler_item.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ZephyrHaulerItem object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "textures/item/zephyr_hauler_white.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ZephyrHaulerItem animatable) {
        return null;
    }
}