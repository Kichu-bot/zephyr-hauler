package com.zephyrhauler.registry;

import com.zephyrhauler.ZephyrHauler;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, ZephyrHauler.MOD_ID);

    public static final Supplier<SimpleParticleType> ZEPHYR_LEAF = PARTICLES.register("zephyr_leaf", () -> new SimpleParticleType(true));

    public static final Supplier<SimpleParticleType> ZEPHYR_WIND = PARTICLES.register("zephyr_wind", () -> new SimpleParticleType(true));
}