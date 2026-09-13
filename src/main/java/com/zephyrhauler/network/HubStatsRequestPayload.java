package com.zephyrhauler.network;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import java.util.UUID;

public record HubStatsRequestPayload(UUID hubId, GlobalPos pos) implements CustomPacketPayload {
    public static final Type<HubStatsRequestPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("zephyr_hauler", "hub_stats_request"));
    public static final StreamCodec<FriendlyByteBuf, HubStatsRequestPayload> CODEC = StreamCodec.ofMember(HubStatsRequestPayload::write, HubStatsRequestPayload::new);

    public HubStatsRequestPayload(FriendlyByteBuf buf) {
        this(buf.readUUID(), GlobalPos.STREAM_CODEC.decode(buf));
    }
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.hubId);
        GlobalPos.STREAM_CODEC.encode(buf, this.pos);
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}