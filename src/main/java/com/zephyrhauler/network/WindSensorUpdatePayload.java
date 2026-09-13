package com.zephyrhauler.network;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.block.entity.WindSensorBlockEntity;
import com.zephyrhauler.util.ZephyrWindSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record WindSensorUpdatePayload(BlockPos pos, int power, int cooldown, String directionName) implements CustomPacketPayload {

    public static final Type<WindSensorUpdatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "wind_sensor_update"));

    public static final StreamCodec<FriendlyByteBuf, WindSensorUpdatePayload> STREAM_CODEC = StreamCodec.ofMember(
            WindSensorUpdatePayload::write,
            WindSensorUpdatePayload::new
    );

    public WindSensorUpdatePayload(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readInt(), buffer.readInt(), buffer.readUtf());
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeInt(this.power);
        buffer.writeInt(this.cooldown);
        buffer.writeUtf(this.directionName);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final WindSensorUpdatePayload data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            Level level = player.level();

            if (level.isLoaded(data.pos())) {
                if (level.getBlockEntity(data.pos()) instanceof WindSensorBlockEntity sensorBE) {
                    sensorBE.setConfiguredPower(data.power());
                    sensorBE.setConfiguredCooldown(data.cooldown());
                    try {
                        sensorBE.setTargetDirection(ZephyrWindSystem.WindDirection.valueOf(data.directionName()));
                    } catch (Exception ignored) {}
                }
            }
        });
    }
}