package com.zephyrhauler.client.model;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.item.ZephyrAutoStationItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ZephyrAutoStationItemModel extends GeoModel<ZephyrAutoStationItem> {
    @Override
    public ResourceLocation getModelResource(ZephyrAutoStationItem object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "geo/zephyr_auto_station.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ZephyrAutoStationItem object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "textures/block/zephyr_auto_station.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ZephyrAutoStationItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "animations/zephyr_station.animation.json");
    }
}