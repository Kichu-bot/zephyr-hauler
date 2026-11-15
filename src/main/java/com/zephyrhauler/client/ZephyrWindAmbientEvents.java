package com.zephyrhauler.client;

import com.zephyrhauler.registry.ModParticles;
import com.zephyrhauler.util.ZephyrWindSystem;
import com.zephyrhauler.config.ZephyrClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = com.zephyrhauler.ZephyrHauler.MOD_ID, value = Dist.CLIENT)
public class ZephyrWindAmbientEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused() || mc.level == null || mc.player == null) return;

        Player player = mc.player;
        Level level = mc.level;
        RandomSource random = level.random;

        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 eyePos = player.getEyePosition();
        ZephyrWindSystem.WindInfo wind = ZephyrWindSystem.getCurrentWind(level, player.blockPosition().above());


        // ==========================================
        // 1. SISTEMA DE HOJAS AMBIENTALES
        // ==========================================
        if (ZephyrClientConfig.ENABLE_LEAVES.get()) {
            double spawnMult = ZephyrClientConfig.LEAF_SPAWN_PERCENT.get() / 100.0;

            if (spawnMult > 0.0) {
                int leafRadius = ZephyrClientConfig.LEAF_SPAWN_RADIUS.get();
                double densityScale = Math.pow(leafRadius / 16.0, 2.5);
                int checksPerTick = (int) ((80 + (wind.speedBonus() * 50)) * spawnMult * densityScale);
                boolean cullBehindLeaf = ZephyrClientConfig.LEAF_CULLING.get();

                for (int i = 0; i < checksPerTick; i++) {
                    int ox = random.nextInt(leafRadius * 2) - leafRadius;
                    int oy = random.nextInt(leafRadius * 2) - leafRadius;
                    int oz = random.nextInt(leafRadius * 2) - leafRadius;

                    BlockPos randomPos = player.blockPosition().offset(ox, oy, oz);

                    if (cullBehindLeaf) {
                        Vec3 dirToBlock = randomPos.getCenter().subtract(eyePos).normalize();
                        if (lookVec.dot(dirToBlock) < -0.1) continue;
                    }

                    BlockState state = level.getBlockState(randomPos);

                    if (state.is(BlockTags.LEAVES)) {
                        BlockPos belowPos = randomPos.below();
                        BlockState belowState = level.getBlockState(belowPos);

                        if (!belowState.is(BlockTags.LEAVES) && belowState.getCollisionShape(level, belowPos).isEmpty()) {
                            if (random.nextFloat() < 0.6f) {
                                double px = randomPos.getX() + random.nextDouble();
                                double py = randomPos.getY() - 0.05;
                                double pz = randomPos.getZ() + random.nextDouble();

                                int color = mc.getBlockColors().getColor(state, level, randomPos);
                                if (state.is(Blocks.ACACIA_LEAVES)) {
                                    color = 0x8D9E45;
                                } else if (color == -1) {
                                    color = state.getMapColor(level, randomPos).col;
                                }

                                float r = ((color >> 16) & 0xFF) / 255.0F;
                                float g = ((color >> 8) & 0xFF) / 255.0F;
                                float b = (color & 0xFF) / 255.0F;

                                level.addParticle(ModParticles.ZEPHYR_LEAF.get(), px, py, pz, r, g, b);
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. SISTEMA DE VIENTO VISUAL (Recalibrado para Alturas Normales)
        // ==========================================
        if (ZephyrClientConfig.ENABLE_WIND_EFFECTS.get()) {
            if (wind.speedBonus() > 0.02f) {

                float spawnChance = 0.50f * wind.speedBonus();
                int maxClusters = 1;

                if (level.isThundering()) {
                    spawnChance = 0.90f;
                    maxClusters = Math.max(2, (int) (4 * wind.speedBonus()));
                } else if (level.isRaining()) {
                    spawnChance = 0.50f;
                    maxClusters = Math.max(1, (int) (2 * wind.speedBonus()));
                }

                maxClusters = Math.max(1, maxClusters);

                if (random.nextFloat() < spawnChance) {
                    int windRadius = 25;

                    for (int i = 0; i < maxClusters; i++) {
                        int ox = random.nextInt(windRadius * 2) - windRadius;
                        int oy = random.nextInt(windRadius) - (windRadius / 2);
                        int oz = random.nextInt(windRadius * 2) - windRadius;
                        BlockPos basePos = player.blockPosition().offset(ox, oy, oz);

                        if (!level.canSeeSky(basePos)) continue;

                        int streamCount = 2 + random.nextInt(4);

                        for(int j = 0; j < streamCount; j++) {
                            BlockPos randomPos = basePos.offset(
                                    random.nextInt(5) - 2,
                                    random.nextInt(3) - 1,
                                    random.nextInt(5) - 2
                            );

                            if (level.isEmptyBlock(randomPos) && level.canSeeSky(randomPos)) {
                                double px = randomPos.getX() + random.nextDouble();
                                double py = randomPos.getY() + random.nextDouble();
                                double pz = randomPos.getZ() + random.nextDouble();

                                double varX = wind.vectorX() * (0.9 + random.nextDouble() * 0.2);
                                double varZ = wind.vectorZ() * (0.9 + random.nextDouble() * 0.2);

                                level.addParticle(ModParticles.ZEPHYR_WIND.get(), px, py, pz, varX, 0, varZ);

                                if (random.nextFloat() < 0.15f) {
                                    Holder<Biome> biomeHolder = level.getBiome(randomPos);
                                    String biomeName = biomeHolder.unwrapKey().map(key -> key.location().getPath().toLowerCase()).orElse("");
                                    float temp = biomeHolder.value().getBaseTemperature();

                                    if (!biomeName.contains("desert") && !biomeName.contains("badlands") &&
                                            !biomeName.contains("mesa") && !biomeName.contains("ocean") &&
                                            !biomeName.contains("beach") && !biomeName.contains("mushroom") &&
                                            !biomeName.contains("nether") && temp > 0.15f) {

                                        int color = net.minecraft.client.renderer.BiomeColors.getAverageFoliageColor(level, randomPos);
                                        float r = ((color >> 16) & 0xFF) / 255.0F;
                                        float g = ((color >> 8) & 0xFF) / 255.0F;
                                        float b = (color & 0xFF) / 255.0F;

                                        if (biomeName.contains("cherry")) {
                                            r = 1.0f; g = 0.75f; b = 0.85f;
                                        }

                                        level.addParticle(ModParticles.ZEPHYR_LEAF.get(), px, py, pz, r, g, b);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}