package com.zephyrhauler.client.renderer;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.client.model.ZephyrHaulerItemModel;
import com.zephyrhauler.component.ZephyrDataComponents;
import com.zephyrhauler.item.ZephyrHaulerItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ZephyrHaulerItemRenderer extends GeoItemRenderer<ZephyrHaulerItem> {

    public ZephyrHaulerItemRenderer() {
        super(new ZephyrHaulerItemModel());
    }

    @Override
    public ResourceLocation getTextureLocation(ZephyrHaulerItem animatable) {
        ItemStack stack = this.getCurrentItemStack();
        if (stack != null) {
            String color = stack.getOrDefault(ZephyrDataComponents.HAULER_COLOR.get(), "white");
            return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "textures/item/zephyr_hauler_" + color + ".png");
        }
        return super.getTextureLocation(animatable);
    }
}