package com.zephyrhauler.client.model;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.item.WindSensorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WindSensorItemModel extends GeoModel<WindSensorItem> {
    @Override
    public ResourceLocation getModelResource(WindSensorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "geo/wind_sensor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WindSensorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "textures/block/wind_sensor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(WindSensorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "animations/wind_sensor.animation.json");
    }
}