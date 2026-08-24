package com.zephyrhauler.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SaveDockNamePayload(BlockPos pos, String name) implements CustomPacketPayload {
    public static final Type<SaveDockNamePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("zephyr_hauler", "save_dock_name"));

    public static final StreamCodec<FriendlyByteBuf, SaveDockNamePayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SaveDockNamePayload::pos,
            ByteBufCodecs.STRING_UTF8, SaveDockNamePayload::name,
            SaveDockNamePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}