package com.zephyrhauler.client.renderer.block;

import com.zephyrhauler.block.entity.ZephyrLauncherBlockEntity;
import com.zephyrhauler.client.model.ZephyrLauncherModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ZephyrLauncherRenderer extends GeoBlockRenderer<ZephyrLauncherBlockEntity> {
    public ZephyrLauncherRenderer(BlockEntityRendererProvider.Context context) {
        super(new ZephyrLauncherModel());
    }
}