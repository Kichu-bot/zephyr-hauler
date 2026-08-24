package com.zephyrhauler.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record DockEntry(String name, GlobalPos pos) {

    public static final Codec<DockEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("name").forGetter(DockEntry::name),
                    GlobalPos.CODEC.fieldOf("pos").forGetter(DockEntry::pos)
            ).apply(instance, DockEntry::new)
    );

    public static final StreamCodec<ByteBuf, DockEntry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, DockEntry::name,
            GlobalPos.STREAM_CODEC, DockEntry::pos,
            DockEntry::new
    );
}