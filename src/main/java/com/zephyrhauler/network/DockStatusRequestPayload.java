package com.zephyrhauler.network;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DockStatusRequestPayload(GlobalPos pos) implements CustomPacketPayload {
    public static final Type<DockStatusRequestPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("zephyr_hauler", "dock_status_request"));

    public static final StreamCodec<FriendlyByteBuf, DockStatusRequestPayload> CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC, DockStatusRequestPayload::pos,
            DockStatusRequestPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}