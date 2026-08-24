package com.zephyrhauler.client.model;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.block.entity.ZephyrDockBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ZephyrDockModel extends GeoModel<ZephyrDockBlockEntity> {
    @Override
    public ResourceLocation getModelResource(ZephyrDockBlockEntity object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "geo/zephyr_dock.geo.json"); }

    @Override
    public ResourceLocation getTextureResource(ZephyrDockBlockEntity object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "textures/block/zephyr_dock.png"); }

    @Override
    public ResourceLocation getAnimationResource(ZephyrDockBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "animations/zephyr_dock.animation.json"); }
}