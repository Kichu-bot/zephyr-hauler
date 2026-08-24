package com.zephyrhauler.client.renderer;

import com.zephyrhauler.client.model.ZephyrStationItemModel; // <-- IMPORTACIÓN CORREGIDA
import com.zephyrhauler.item.ZephyrStationItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ZephyrStationItemRenderer extends GeoItemRenderer<ZephyrStationItem> {

    public ZephyrStationItemRenderer() {
        super(new ZephyrStationItemModel());
    }
}