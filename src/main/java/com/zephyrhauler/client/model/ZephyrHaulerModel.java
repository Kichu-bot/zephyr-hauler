package com.zephyrhauler.client.model;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.entity.ZephyrHaulerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ZephyrHaulerModel extends GeoModel<ZephyrHaulerEntity> {

    @Override
    public ResourceLocation getModelResource(ZephyrHaulerEntity object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "geo/entity/zephyr_hauler.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ZephyrHaulerEntity object) {
        String color = object.getColor();
        if (color == null || color.isEmpty()) {
            color = "white";
        }
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "textures/entity/zephyr_hauler_" + color + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(ZephyrHaulerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "animations/entity/zephyr_hauler.animation.json");
    }
}