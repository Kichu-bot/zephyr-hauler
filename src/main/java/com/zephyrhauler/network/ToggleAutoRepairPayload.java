package com.zephyrhauler.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ToggleAutoRepairPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<ToggleAutoRepairPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("zephyr_hauler", "toggle_auto_repair"));

    public static final StreamCodec<FriendlyByteBuf, ToggleAutoRepairPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ToggleAutoRepairPayload::pos,
            ToggleAutoRepairPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}