package com.zephyrhauler.registry;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.component.ZephyrDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ZephyrHauler.MOD_ID);

    public static final Supplier<CreativeModeTab> ZEPHYR_TAB = CREATIVE_MODE_TABS.register("zephyr_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("creativetab.zephyr_hauler"))
            .icon(() -> new ItemStack(ModItems.ZEPHYR_HAULER.get()))
            .displayItems((parameters, output) -> {

                output.accept(ModItems.ZEPHYR_CONTROLLER.get());
                output.accept(ModItems.ZEPHYR_DOCK_ITEM.get());
                output.accept(ModItems.ZEPHYR_STATION_ITEM.get()); // La estación de reparación
                output.accept(ModItems.WIND_METER.get());

                String[] colors = {
                        "white", "orange", "magenta", "light_blue", "yellow", "lime",
                        "pink", "gray", "light_gray", "cyan", "purple", "blue",
                        "brown", "green", "red", "black"
                };

                for (String color : colors) {
                    ItemStack coloredHauler = new ItemStack(ModItems.ZEPHYR_HAULER.get());
                    coloredHauler.set(ZephyrDataComponents.HAULER_COLOR.get(), color);
                    coloredHauler.set(ZephyrDataComponents.HAULER_DURABILITY.get(), 8);

                    output.accept(coloredHauler);
                }
            })
            .build());
}