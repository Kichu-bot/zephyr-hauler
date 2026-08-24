package com.zephyrhauler.network;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ControllerActionPayload(int action, GlobalPos dockPos, String dockName) implements CustomPacketPayload {

    public static final Type<ControllerActionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("zephyr_hauler", "controller_action"));

    public static final StreamCodec<FriendlyByteBuf, ControllerActionPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ControllerActionPayload::action,
            GlobalPos.STREAM_CODEC, ControllerActionPayload::dockPos,
            ByteBufCodecs.STRING_UTF8, ControllerActionPayload::dockName,
            ControllerActionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}