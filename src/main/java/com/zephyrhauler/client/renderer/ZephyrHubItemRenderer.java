package com.zephyrhauler.client.renderer;

import com.zephyrhauler.client.model.ZephyrHubItemModel;
import com.zephyrhauler.item.ZephyrHubItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ZephyrHubItemRenderer extends GeoItemRenderer<ZephyrHubItem> {
    public ZephyrHubItemRenderer() {
        super(new ZephyrHubItemModel());
    }
}