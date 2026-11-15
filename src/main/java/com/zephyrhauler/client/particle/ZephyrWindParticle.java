package com.zephyrhauler.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ZephyrWindParticle extends TextureSheetParticle {

    private final Vec3[] history;
    private final Vec3[] historyOld;
    private int historyCount = 0;

    private final float baseThickness;
    private final boolean isWavy;
    private final float waveSpeed;

    protected ZephyrWindParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z);

        this.xd = vx * 3.0;
        this.yd = (this.random.nextFloat() - 0.5f) * 0.02f;
        this.zd = vz * 3.0;

        this.lifetime = 40 + this.random.nextInt(40);
        this.hasPhysics = false;
        this.alpha = 0.0F;

        int tailLength = 15 + this.random.nextInt(15);
        this.history = new Vec3[tailLength];
        this.historyOld = new Vec3[tailLength];

        this.history[0] = new Vec3(this.x, this.y, this.z);
        this.historyOld[0] = new Vec3(this.x, this.y, this.z);

        BlockPos pos = BlockPos.containing(x, y, z);
        Holder<Biome> biomeHolder = level.getBiome(pos);
        String biomeName = biomeHolder.unwrapKey().map(key -> key.location().getPath().toLowerCase()).orElse("");

        float targetR = 1.0f, targetG = 1.0f, targetB = 1.0f;
        float blend = 0.0f;

        if (biomeName.contains("desert") || biomeName.contains("beach")) {
            targetR = 0.9f; targetG = 0.8f; targetB = 0.6f; blend = 0.6f;
        } else if (biomeName.contains("badlands") || biomeName.contains("mesa")) {
            targetR = 0.85f; targetG = 0.45f; targetB = 0.25f; blend = 0.7f;
        } else if (biomeName.contains("swamp") || biomeName.contains("mangrove")) {
            targetR = 0.4f; targetG = 0.5f; targetB = 0.3f; blend = 0.45f;
        } else if (biomeName.contains("cherry")) {
            targetR = 1.0f; targetG = 0.75f; targetB = 0.85f; blend = 0.5f;
        } else if (biomeName.contains("mushroom")) {
            targetR = 0.6f; targetG = 0.5f; targetB = 0.65f; blend = 0.5f;
        } else if (biomeName.contains("basalt") || biomeName.contains("soul")) {
            targetR = 0.4f; targetG = 0.4f; targetB = 0.45f; blend = 0.8f;
        } else if (biomeName.contains("crimson")) {
            targetR = 0.8f; targetG = 0.2f; targetB = 0.2f; blend = 0.7f;
        } else if (biomeName.contains("warped")) {
            targetR = 0.1f; targetG = 0.6f; targetB = 0.6f; blend = 0.7f;
        } else if (biomeHolder.value().getBaseTemperature() < 0.15f) {
            targetR = 0.75f; targetG = 0.85f; targetB = 1.0f; blend = 0.6f;
        }

        float variance = (this.random.nextFloat() * 0.1f) - 0.05f;
        this.rCol = Mth.clamp((1.0f * (1.0f - blend)) + (targetR * blend) + variance, 0.0f, 1.0f);
        this.gCol = Mth.clamp((1.0f * (1.0f - blend)) + (targetG * blend) + variance, 0.0f, 1.0f);
        this.bCol = Mth.clamp((1.0f * (1.0f - blend)) + (targetB * blend) + variance, 0.0f, 1.0f);

        this.baseThickness = 0.15f + this.random.nextFloat() * 0.15f;
        this.isWavy = this.random.nextFloat() < 0.5f;
        this.waveSpeed = 0.1f + this.random.nextFloat() * 0.15f;

        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        for (int i = this.history.length - 1; i > 0; i--) {
            this.history[i] = this.history[i - 1];
            this.historyOld[i] = this.historyOld[i - 1];
        }

        this.historyOld[0] = new Vec3(this.x, this.y, this.z);
        super.tick();
        this.history[0] = new Vec3(this.x, this.y, this.z);
        if (this.historyCount < this.history.length) this.historyCount++;

        if (this.isWavy) {
            this.yd += Math.sin(this.age * this.waveSpeed) * 0.004f;
        }

        float lifeRatio = (float) this.age / (float) this.lifetime;
        float maxAlpha = 0.45f;
        if (lifeRatio < 0.2f) {
            this.alpha = maxAlpha * (lifeRatio / 0.2f);
        } else if (lifeRatio > 0.6f) {
            this.alpha = maxAlpha * ((1.0f - lifeRatio) / 0.4f);
        } else {
            this.alpha = maxAlpha;
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        if (this.historyCount < 2) return;

        Vec3 camPos = camera.getPosition();

        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int light = this.getLightColor(partialTicks);

        for (int i = 0; i < this.historyCount - 1; i++) {
            Vec3 pCurrentOld = this.historyOld[i];
            Vec3 pCurrentNew = this.history[i];
            Vec3 pNextOld = this.historyOld[i + 1];
            Vec3 pNextNew = this.history[i + 1];

            if (pCurrentOld == null || pCurrentNew == null || pNextOld == null || pNextNew == null) break;

            Vec3 pCurrent = new Vec3(
                    Mth.lerp(partialTicks, pCurrentOld.x, pCurrentNew.x),
                    Mth.lerp(partialTicks, pCurrentOld.y, pCurrentNew.y),
                    Mth.lerp(partialTicks, pCurrentOld.z, pCurrentNew.z)
            ).subtract(camPos);

            Vec3 pNext = new Vec3(
                    Mth.lerp(partialTicks, pNextOld.x, pNextNew.x),
                    Mth.lerp(partialTicks, pNextOld.y, pNextNew.y),
                    Mth.lerp(partialTicks, pNextOld.z, pNextNew.z)
            ).subtract(camPos);

            Vec3 dir = pCurrent.subtract(pNext).normalize();
            Vec3 toCamera = camPos.subtract(pCurrent).normalize();

            float segmentRatio = (float) i / (float) this.history.length;
            float rawTaper = 1.0f - segmentRatio;

            float steppedTaper = (float) Math.ceil(rawTaper * 5.0f) / 5.0f;
            float finalTaper = (rawTaper * 0.4f) + (steppedTaper * 0.6f);
            float thickness = this.baseThickness * finalTaper;

            Vec3 widthVecCurrent = dir.cross(toCamera).normalize().scale(thickness);
            Vec3 widthVecNext = dir.cross(toCamera).normalize().scale(thickness * 0.9f);

            float rawAlpha = this.alpha * (1.0f - (segmentRatio * segmentRatio));
            float steppedAlpha = (float) Math.ceil(rawAlpha * 5.0f) / 5.0f;
            float finalAlpha = (rawAlpha * 0.5f) + (steppedAlpha * 0.5f);

            Vec3 pos1 = pCurrent.add(widthVecCurrent);
            Vec3 pos2 = pCurrent.subtract(widthVecCurrent);
            Vec3 pos3 = pNext.subtract(widthVecNext);
            Vec3 pos4 = pNext.add(widthVecNext);

            buffer.addVertex((float) pos1.x, (float) pos1.y, (float) pos1.z).setUv(u1, v1).setColor(this.rCol, this.gCol, this.bCol, finalAlpha).setLight(light);
            buffer.addVertex((float) pos2.x, (float) pos2.y, (float) pos2.z).setUv(u1, v0).setColor(this.rCol, this.gCol, this.bCol, finalAlpha).setLight(light);
            buffer.addVertex((float) pos3.x, (float) pos3.y, (float) pos3.z).setUv(u0, v0).setColor(this.rCol, this.gCol, this.bCol, finalAlpha).setLight(light);
            buffer.addVertex((float) pos4.x, (float) pos4.y, (float) pos4.z).setUv(u0, v1).setColor(this.rCol, this.gCol, this.bCol, finalAlpha).setLight(light);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
            return new ZephyrWindParticle(level, x, y, z, vx, vy, vz, this.sprites);
        }
    }
}