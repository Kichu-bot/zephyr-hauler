package com.zephyrhauler.client.renderer.block;

import com.zephyrhauler.block.entity.ZephyrDockBlockEntity;
import com.zephyrhauler.client.model.ZephyrDockModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider; // <-- NUEVA IMPORTACIÓN
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ZephyrDockRenderer extends GeoBlockRenderer<ZephyrDockBlockEntity> {

    public ZephyrDockRenderer(BlockEntityRendererProvider.Context context) {
        super(new ZephyrDockModel());
    }
}