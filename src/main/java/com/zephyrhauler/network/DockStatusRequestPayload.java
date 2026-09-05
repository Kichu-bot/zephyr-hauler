package com.zephyrhauler.network;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import java.util.UUID;

public record DockStatusRequestPayload(UUID dockId, GlobalPos pos) implements CustomPacketPayload {
    public static final Type<DockStatusRequestPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("zephyr_hauler", "dock_status_request"));

    public static final StreamCodec<FriendlyByteBuf, DockStatusRequestPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, DockStatusRequestPayload::dockId,
            GlobalPos.STREAM_CODEC, DockStatusRequestPayload::pos,
            DockStatusRequestPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}