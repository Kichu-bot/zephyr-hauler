package com.zephyrhauler.client.particle;

import com.zephyrhauler.config.ZephyrClientConfig;
import com.zephyrhauler.util.ZephyrWindSystem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ZephyrLeafParticle extends TextureSheetParticle {

    private final float rotSpeed;
    private final float swirlRadius;
    private final float swirlPhase; // RECUPERADO: Cada hoja tendrá su propio ritmo

    protected ZephyrLeafParticle(ClientLevel level, double x, double y, double z, double r, double g, double b, SpriteSet spriteSet) {
        super(level, x, y, z);

        this.setSprite(spriteSet.get(this.random));
        this.setColor((float) r, (float) g, (float) b);

        this.gravity = 0.012F;
        this.friction = 0.98F;

        this.lifetime = ZephyrClientConfig.LEAF_LIFETIME.get() + this.random.nextInt(40);

        this.rotSpeed = (this.random.nextFloat() - 0.5F) * 0.08F;
        this.roll = this.random.nextFloat() * ((float) Math.PI * 2F);

        this.swirlRadius = 0.02F;
        // RECUPERADO: Un desfase aleatorio para que no salgan disparadas en la misma dirección
        this.swirlPhase = this.random.nextFloat() * ((float) Math.PI * 2F);

        float configSize = ZephyrClientConfig.LEAF_SIZE_PERCENT.get() / 100.0f;
        this.scale(configSize * (0.8f + this.random.nextFloat() * 0.4f));

        // SOLUCIÓN AL "BOOMERANG":
        // Al nacer, leemos el viento exacto en esta posición y le damos a la hoja
        // la inercia inicial del viento. Así no empieza desde cero.
        BlockPos pos = BlockPos.containing(this.x, this.y, this.z);
        ZephyrWindSystem.WindInfo initialWind = ZephyrWindSystem.getCurrentWind(level, pos);

        this.xd = initialWind.vectorX() * 0.3;
        this.yd = -0.05; // Un ligero empuje hacia abajo para que se suelte natural
        this.zd = initialWind.vectorZ() * 0.3;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime || this.onGround) {
            this.remove();
            return;
        }

        BlockPos pos = BlockPos.containing(this.x, this.y, this.z);
        ZephyrWindSystem.WindInfo wind = ZephyrWindSystem.getCurrentWind(this.level, pos);

        // Aceleración suave constante del viento
        if (wind.speedBonus() > 0.0f) {
            this.xd += wind.vectorX() * 0.04;
            this.zd += wind.vectorZ() * 0.04;
        }

        // Remolino usando la "Fase Aleatoria" (swirlPhase) para que la oscilación sea orgánica
        this.xd += Math.sin((this.age * 0.1f) + this.swirlPhase) * this.swirlRadius;
        this.zd += Math.cos((this.age * 0.1f) + this.swirlPhase) * this.swirlRadius;

        this.yd -= this.gravity;

        float maxSpeed = 0.5f;
        this.xd = Math.max(-maxSpeed, Math.min(maxSpeed, this.xd));
        this.zd = Math.max(-maxSpeed, Math.min(maxSpeed, this.zd));

        this.move(this.xd, this.yd, this.zd);

        this.oRoll = this.roll;
        this.roll += this.rotSpeed;

        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double r, double g, double b) {
            return new ZephyrLeafParticle(level, x, y, z, r, g, b, this.spriteSet);
        }
    }
}