package com.zephyrhauler.client.model;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.block.ZephyrDockBlock;
import com.zephyrhauler.block.entity.ZephyrDockBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.model.GeoModel;

public class ZephyrDockModel extends GeoModel<ZephyrDockBlockEntity> {
    @Override
    public ResourceLocation getModelResource(ZephyrDockBlockEntity object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "geo/zephyr_dock.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ZephyrDockBlockEntity object) {
        if (object != null && object.hasLevel()) {
            BlockState state = object.getBlockState();
            if (state.hasProperty(ZephyrDockBlock.HUB_MODE) && state.getValue(ZephyrDockBlock.HUB_MODE)) {
                return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "textures/block/zephyr_dock_hubmode.png");
            }
        }
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "textures/block/zephyr_dock.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ZephyrDockBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "animations/zephyr_dock.animation.json");
    }
}