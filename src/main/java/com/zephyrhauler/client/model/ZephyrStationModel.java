package com.zephyrhauler.client.model;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.block.entity.ZephyrStationBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ZephyrStationModel extends GeoModel<ZephyrStationBlockEntity> {

    @Override
    public ResourceLocation getModelResource(ZephyrStationBlockEntity object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "geo/zephyr_station.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ZephyrStationBlockEntity object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "textures/block/zephyr_station.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ZephyrStationBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "animations/zephyr_station.animation.json");
    }
}