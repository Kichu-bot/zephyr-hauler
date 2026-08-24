package com.zephyrhauler.util;

import net.minecraft.world.level.Level;
import java.util.Random;

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
    }

    public record WindInfo(WindDirection direction, float speedBonus) {}

    public static WindInfo getCurrentWind(Level level) {
        long timePeriod = level.getGameTime() / 8000L;
        long dimHash = level.dimension().location().hashCode();
        Random random = new Random(dimHash + (timePeriod * 987654321L));

        WindDirection currentDirection;
        float baseBonus = 0.0f;

        if (random.nextFloat() < 0.15f) {
            currentDirection = WindDirection.NONE;
        } else {
            WindDirection[] allDirections = WindDirection.values();
            currentDirection = allDirections[1 + random.nextInt(allDirections.length - 1)];
            baseBonus = 0.10f + (random.nextFloat() * 0.15f);
        }

        if (currentDirection != WindDirection.NONE) {
            if (level.isThundering()) {
                baseBonus *= 2.5f;
            } else if (level.isRaining()) {
                baseBonus *= 1.5f;
            }
        }

        return new WindInfo(currentDirection, baseBonus);
    }
}