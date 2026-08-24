package com.zephyrhauler.client.model;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.item.WindMeterItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WindMeterModel extends GeoModel<WindMeterItem> {
    @Override
    public ResourceLocation getModelResource(WindMeterItem object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "geo/wind_meter.geo.json"); }

    @Override
    public ResourceLocation getTextureResource(WindMeterItem object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "textures/item/wind_meter.png"); }

    @Override
    public ResourceLocation getAnimationResource(WindMeterItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "animations/wind_meter.animation.json"); }
}