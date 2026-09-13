package com.zephyrhauler.client.model;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.block.entity.ZephyrAutoStationBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ZephyrAutoStationModel extends GeoModel<ZephyrAutoStationBlockEntity> {
    @Override
    public ResourceLocation getModelResource(ZephyrAutoStationBlockEntity object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "geo/zephyr_auto_station.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ZephyrAutoStationBlockEntity object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "textures/block/zephyr_auto_station.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ZephyrAutoStationBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "animations/zephyr_station.animation.json");
    }
}