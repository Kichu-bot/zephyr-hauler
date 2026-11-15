package com.zephyrhauler.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class ZephyrWindSystem {

    public enum WindDirection {
        NONE(0, 0, "direction.zephyr_hauler.none"),
        NORTH(0, -1, "direction.zephyr_hauler.north"),
        NORTH_EAST(1, -1, "direction.zephyr_hauler.north_east"),
        EAST(1, 0, "direction.zephyr_hauler.east"),
        SOUTH_EAST(1, 1, "direction.zephyr_hauler.south_east"),
        SOUTH(0, 1, "direction.zephyr_hauler.south"),
        SOUTH_WEST(-1, 1, "direction.zephyr_hauler.south_west"),
        WEST(-1, 0, "direction.zephyr_hauler.west"),
        NORTH_WEST(-1, -1, "direction.zephyr_hauler.north_west");

        public final int x;
        public final int z;
        public final String translationKey;

        WindDirection(int x, int z, String translationKey) {
            this.x = x;
            this.z = z;
            this.translationKey = translationKey;
        }

        public static WindDirection fromAngle(float angleDegrees, float speed) {
            if (speed < 0.05f) return NONE;

            float normalizedAngle = (angleDegrees % 360 + 360) % 360;

            if (normalizedAngle >= 337.5 || normalizedAngle < 22.5) return SOUTH;
            if (normalizedAngle >= 22.5 && normalizedAngle < 67.5) return SOUTH_EAST;
            if (normalizedAngle >= 67.5 && normalizedAngle < 112.5) return EAST;
            if (normalizedAngle >= 112.5 && normalizedAngle < 157.5) return NORTH_EAST;
            if (normalizedAngle >= 157.5 && normalizedAngle < 202.5) return NORTH;
            if (normalizedAngle >= 202.5 && normalizedAngle < 247.5) return NORTH_WEST;
            if (normalizedAngle >= 247.5 && normalizedAngle < 292.5) return WEST;
            if (normalizedAngle >= 292.5 && normalizedAngle < 337.5) return SOUTH_WEST;

            return NONE;
        }
    }

    public record WindInfo(WindDirection direction, float speedBonus, float rawAngle, float vectorX, float vectorZ) {}

    private static float getSmoothNoise(long time, long seed, float speedMultiplier) {
        double t = ((time % 1000000) * speedMultiplier);
        double s = Math.abs(seed) % 10000;

        double wave1 = Math.sin(t + s);
        double wave2 = Math.sin(t * 0.732 + s + 2.4);
        double wave3 = Math.sin(t * 1.317 + s + 5.1);

        return (float) ((wave1 + wave2 + wave3) / 3.0);
    }

    public static WindInfo getCurrentWind(Level level, BlockPos pos) {
        long time = level.getGameTime();
        long seed = level.dimension().location().hashCode();

        float weatherSpeedMult = 1.0f;
        float weatherVolatility = 1.0f;
        float baseBonusAdd = 0.0f;

        if (level.isThundering()) {
            weatherSpeedMult = 3.0f;
            weatherVolatility = 2.5f;
            baseBonusAdd = 0.4f;
        } else if (level.isRaining()) {
            weatherSpeedMult = 1.5f;
            weatherVolatility = 1.2f;
            baseBonusAdd = 0.15f;
        }

        float y = pos.getY();
        float altitudeMultiplier = 1.0f;
        float windShearAngle = 0.0f;

        if (y < 60) {
            altitudeMultiplier = Math.max(0.0f, (y - 20) / 40.0f);
        } else if (y > 90) {
            altitudeMultiplier = 1.0f + Math.min(1.5f, (y - 90) / 100.0f);
            windShearAngle = Math.min(45.0f, (y - 90) * 0.5f);
        }

        if (!level.canSeeSky(pos)) {
            altitudeMultiplier *= 0.15f;
        }

        float baseX = getSmoothNoise(time, seed, 0.0003f * weatherVolatility);
        float baseZ = getSmoothNoise(time, seed + 5000, 0.0003f * weatherVolatility);

        float gustIntensity = getSmoothNoise(time, seed + 9000, 0.005f * weatherSpeedMult);
        float gustX = 0.0f;
        float gustZ = 0.0f;

        if (gustIntensity > 0.3f) {
            float gustMult = (gustIntensity - 0.3f) * 2.0f * weatherSpeedMult;
            gustX = getSmoothNoise(time, seed + 12000, 0.015f) * gustMult;
            gustZ = getSmoothNoise(time, seed + 15000, 0.015f) * gustMult;
        }

        float finalX = (baseX * 0.25f) + gustX;
        float finalZ = (baseZ * 0.25f) + gustZ;

        float rawSpeed = (float) Math.sqrt((finalX * finalX) + (finalZ * finalZ)) + baseBonusAdd;
        rawSpeed *= altitudeMultiplier;

        float rawAngle = (float) Math.toDegrees(Math.atan2(finalX, finalZ));
        if (rawAngle < 0) rawAngle += 360.0f;

        rawAngle = (rawAngle + windShearAngle) % 360.0f;

        if (!level.isRaining() && rawSpeed < 0.05f) {
            rawSpeed = 0.0f;
        }

        WindDirection currentDirection = WindDirection.fromAngle(rawAngle, rawSpeed);

        float vectorX = (float) Math.sin(Math.toRadians(rawAngle)) * rawSpeed;
        float vectorZ = (float) Math.cos(Math.toRadians(rawAngle)) * rawSpeed;

        return new WindInfo(currentDirection, rawSpeed, rawAngle, vectorX, vectorZ);
    }
}