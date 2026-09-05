package com.zephyrhauler.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RefreshDocksPayload(boolean dummy) implements CustomPacketPayload {
    public static final Type<RefreshDocksPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("zephyr_hauler", "refresh_docks"));

    public static final StreamCodec<FriendlyByteBuf, RefreshDocksPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, RefreshDocksPayload::dummy,
            RefreshDocksPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}