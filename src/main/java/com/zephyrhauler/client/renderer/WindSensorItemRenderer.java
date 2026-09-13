package com.zephyrhauler.client.renderer;

import com.zephyrhauler.client.model.WindSensorItemModel;
import com.zephyrhauler.item.WindSensorItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class WindSensorItemRenderer extends GeoItemRenderer<WindSensorItem> {
    public WindSensorItemRenderer() {
        super(new WindSensorItemModel());
    }
}