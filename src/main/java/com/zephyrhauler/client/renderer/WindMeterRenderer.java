package com.zephyrhauler.client.renderer;

import com.zephyrhauler.client.model.WindMeterModel;
import com.zephyrhauler.item.WindMeterItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class WindMeterRenderer extends GeoItemRenderer<WindMeterItem> {
    public WindMeterRenderer() { super(new WindMeterModel()); }
}