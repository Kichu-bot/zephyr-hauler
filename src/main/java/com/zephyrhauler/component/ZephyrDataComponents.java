package com.zephyrhauler.component;

import com.zephyrhauler.ZephyrHauler;
import com.mojang.serialization.Codec;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class ZephyrDataComponents {

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, ZephyrHauler.MOD_ID);

    public static final Supplier<DataComponentType<GlobalPos>> TARGET_POS =
            COMPONENTS.register("target_pos", () -> DataComponentType.<GlobalPos>builder()
                    .persistent(GlobalPos.CODEC)
                    .networkSynchronized(GlobalPos.STREAM_CODEC)
                    .build());

    public static final Supplier<DataComponentType<String>> TARGET_NAME =
            COMPONENTS.register("target_name", () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());

    public static final Supplier<DataComponentType<UUID>> LINK_ID =
            COMPONENTS.register("link_id", () -> DataComponentType.<UUID>builder()
                    .persistent(UUIDUtil.CODEC)
                    .networkSynchronized(UUIDUtil.STREAM_CODEC)
                    .build());

    public static final Supplier<DataComponentType<List<DockEntry>>> SAVED_DOCKS =
            COMPONENTS.register("saved_docks", () -> DataComponentType.<List<DockEntry>>builder()
                    .persistent(DockEntry.CODEC.listOf())
                    .networkSynchronized(DockEntry.STREAM_CODEC.apply(ByteBufCodecs.list()))
                    .build());

    public static final Supplier<DataComponentType<String>> HAULER_COLOR =
            COMPONENTS.register("hauler_color", () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());

    public static final Supplier<DataComponentType<Integer>> HAULER_DURABILITY =
            COMPONENTS.register("hauler_durability", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static final Supplier<DataComponentType<String>> HAULER_DAMAGE =
            COMPONENTS.register("hauler_damage", () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());

    public static final Supplier<DataComponentType<List<String>>> HAULER_UPGRADES =
            COMPONENTS.register("hauler_upgrades", () -> DataComponentType.<List<String>>builder()
                    .persistent(Codec.STRING.listOf())
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()))
                    .build());
}