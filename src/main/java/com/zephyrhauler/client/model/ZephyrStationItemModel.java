package com.zephyrhauler.client.model;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.item.ZephyrStationItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ZephyrStationItemModel extends GeoModel<ZephyrStationItem> {

    @Override
    public ResourceLocation getModelResource(ZephyrStationItem object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "geo/zephyr_station.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ZephyrStationItem object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "textures/block/zephyr_station.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ZephyrStationItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "animations/zephyr_station.animation.json");
    }
}