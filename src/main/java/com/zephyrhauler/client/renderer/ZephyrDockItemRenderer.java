package com.zephyrhauler.client.renderer;

import com.zephyrhauler.client.model.ZephyrDockItemModel;
import com.zephyrhauler.item.ZephyrDockItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ZephyrDockItemRenderer extends GeoItemRenderer<ZephyrDockItem> {
    public ZephyrDockItemRenderer() { super(new ZephyrDockItemModel()); }
}