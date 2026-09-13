package com.zephyrhauler.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HubStatsResponsePayload(int total, int free, int reserved, int occupied, int inaccessible) implements CustomPacketPayload {
    public static final Type<HubStatsResponsePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("zephyr_hauler", "hub_stats_response"));
    public static final StreamCodec<FriendlyByteBuf, HubStatsResponsePayload> CODEC = StreamCodec.ofMember(HubStatsResponsePayload::write, HubStatsResponsePayload::new);

    public HubStatsResponsePayload(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.total);
        buf.writeInt(this.free);
        buf.writeInt(this.reserved);
        buf.writeInt(this.occupied);
        buf.writeInt(this.inaccessible);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}