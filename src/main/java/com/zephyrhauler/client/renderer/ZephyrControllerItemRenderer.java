package com.zephyrhauler.client.renderer;

import com.zephyrhauler.client.model.ZephyrControllerItemModel;
import com.zephyrhauler.item.ZephyrControllerItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ZephyrControllerItemRenderer extends GeoItemRenderer<ZephyrControllerItem> {
    public ZephyrControllerItemRenderer() {
        super(new ZephyrControllerItemModel());
    }
}