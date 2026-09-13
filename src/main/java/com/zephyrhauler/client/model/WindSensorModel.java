package com.zephyrhauler.client.model;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.block.entity.WindSensorBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WindSensorModel extends GeoModel<WindSensorBlockEntity> {
    @Override
    public ResourceLocation getModelResource(WindSensorBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "geo/wind_sensor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WindSensorBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "textures/block/wind_sensor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(WindSensorBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "animations/wind_sensor.animation.json");
    }
}