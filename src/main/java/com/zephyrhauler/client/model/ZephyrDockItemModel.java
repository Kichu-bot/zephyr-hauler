package com.zephyrhauler.client.model;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.item.ZephyrDockItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ZephyrDockItemModel extends GeoModel<ZephyrDockItem> {
    @Override
    public ResourceLocation getModelResource(ZephyrDockItem object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "geo/zephyr_dock.geo.json"); }

    @Override
    public ResourceLocation getTextureResource(ZephyrDockItem object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "textures/block/zephyr_dock.png"); }

    @Override
    public ResourceLocation getAnimationResource(ZephyrDockItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "animations/zephyr_dock.animation.json"); }
}