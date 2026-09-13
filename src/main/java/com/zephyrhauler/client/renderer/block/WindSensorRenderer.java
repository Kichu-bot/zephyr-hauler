package com.zephyrhauler.client.renderer.block;

import com.zephyrhauler.block.entity.WindSensorBlockEntity;
import com.zephyrhauler.client.model.WindSensorModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class WindSensorRenderer extends GeoBlockRenderer<WindSensorBlockEntity> {
    public WindSensorRenderer(BlockEntityRendererProvider.Context context) {
        super(new WindSensorModel());
    }
}