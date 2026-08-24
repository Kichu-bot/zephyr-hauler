package com.zephyrhauler.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DockStatusResponsePayload(int status, String message) implements CustomPacketPayload {
    public static final Type<DockStatusResponsePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("zephyr_hauler", "dock_status_response"));

    public static final StreamCodec<FriendlyByteBuf, DockStatusResponsePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, DockStatusResponsePayload::status,
            ByteBufCodecs.STRING_UTF8, DockStatusResponsePayload::message,
            DockStatusResponsePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}