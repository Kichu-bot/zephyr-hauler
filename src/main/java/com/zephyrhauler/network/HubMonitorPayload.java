package com.zephyrhauler.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record HubMonitorPayload(BlockPos hubPos, String hubName, List<DockInfo> docks) implements CustomPacketPayload {

    public static final Type<HubMonitorPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("zephyr_hauler", "hub_monitor"));

    public static final StreamCodec<FriendlyByteBuf, HubMonitorPayload> STREAM_CODEC = StreamCodec.ofMember(
            HubMonitorPayload::write, HubMonitorPayload::new
    );

    public HubMonitorPayload(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readUtf(256), readDocks(buf));
    }

    private static List<DockInfo> readDocks(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<DockInfo> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(new DockInfo(buf.readBlockPos(), buf.readUtf(256), buf.readInt()));
        }
        return list;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.hubPos);
        buf.writeUtf(this.hubName, 256);
        buf.writeVarInt(this.docks.size());
        for (DockInfo info : this.docks) {
            buf.writeBlockPos(info.pos());
            buf.writeUtf(info.name(), 256);
            buf.writeInt(info.status());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public record DockInfo(BlockPos pos, String name, int status) {}
}