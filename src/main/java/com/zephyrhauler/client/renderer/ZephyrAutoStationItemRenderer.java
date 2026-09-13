package com.zephyrhauler.client.renderer;

import com.zephyrhauler.client.model.ZephyrAutoStationItemModel;
import com.zephyrhauler.item.ZephyrAutoStationItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ZephyrAutoStationItemRenderer extends GeoItemRenderer<ZephyrAutoStationItem> {
    public ZephyrAutoStationItemRenderer() {
        super(new ZephyrAutoStationItemModel());
    }
}