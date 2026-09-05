package com.zephyrhauler.network;

import com.zephyrhauler.ZephyrHauler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RemoveDockPayload(int index) implements CustomPacketPayload {
    public static final Type<RemoveDockPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "remove_dock"));

    public static final StreamCodec<FriendlyByteBuf, RemoveDockPayload> CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeInt(payload.index()),
            buf -> new RemoveDockPayload(buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}