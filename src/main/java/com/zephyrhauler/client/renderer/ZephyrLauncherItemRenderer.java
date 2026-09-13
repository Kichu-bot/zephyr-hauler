package com.zephyrhauler.client.renderer;

import com.zephyrhauler.client.model.ZephyrLauncherItemModel;
import com.zephyrhauler.item.ZephyrLauncherItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ZephyrLauncherItemRenderer extends GeoItemRenderer<ZephyrLauncherItem> {
    public ZephyrLauncherItemRenderer() {
        super(new ZephyrLauncherItemModel());
    }
}