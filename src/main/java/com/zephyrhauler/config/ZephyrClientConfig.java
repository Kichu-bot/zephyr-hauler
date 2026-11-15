package com.zephyrhauler.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ZephyrClientConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // CONFIGURACIÓN DE HOJAS
    public static final ModConfigSpec.BooleanValue ENABLE_LEAVES;
    public static final ModConfigSpec.IntValue LEAF_SPAWN_PERCENT;
    public static final ModConfigSpec.IntValue LEAF_SIZE_PERCENT;
    public static final ModConfigSpec.IntValue LEAF_SPAWN_RADIUS;
    public static final ModConfigSpec.BooleanValue LEAF_CULLING;
    public static final ModConfigSpec.IntValue LEAF_LIFETIME;

    // CONFIGURACIÓN DE VIENTO
    public static final ModConfigSpec.BooleanValue ENABLE_WIND_EFFECTS;

    // CONFIGURACIÓN DE MEJORAS VISUALES
    public static final ModConfigSpec.BooleanValue ENABLE_VISUAL_UPGRADES;
    public static final ModConfigSpec.BooleanValue SHOW_WAXED_UPGRADE;
    public static final ModConfigSpec.BooleanValue SHOW_REINFORCED_UPGRADE;
    public static final ModConfigSpec.BooleanValue SHOW_INSULATED_UPGRADE;
    public static final ModConfigSpec.BooleanValue SHOW_NETHERITE_UPGRADE;
    public static final ModConfigSpec.BooleanValue SHOW_ABSORBERS_UPGRADE;

    static {
        // --- SUBMENÚ DE HOJAS ---
        BUILDER.push("ambient_leaves_settings");

        ENABLE_LEAVES = BUILDER
                .comment("Master Switch: Enables ambient falling leaves.")
                .translation("config.zephyr_hauler.enableLeaves")
                .define("enableLeaves", true);

        LEAF_SPAWN_PERCENT = BUILDER
                .comment("Leaf density in percentage (100 = Normal).")
                .translation("config.zephyr_hauler.leafSpawnPercent")
                .defineInRange("leafSpawnPercent", 100, 0, 1000);

        LEAF_SIZE_PERCENT = BUILDER
                .comment("Leaf size in percentage (100 = Normal).")
                .translation("config.zephyr_hauler.leafSizePercent")
                .defineInRange("leafSizePercent", 100, 10, 500);

        LEAF_SPAWN_RADIUS = BUILDER
                .comment("Spawn radius for leaves in blocks.")
                .translation("config.zephyr_hauler.leafSpawnRadius")
                .defineInRange("leafSpawnRadius", 16, 5, 64);

        LEAF_CULLING = BUILDER
                .comment("Optimization: Stops generating leaves behind your camera.")
                .translation("config.zephyr_hauler.leafCulling")
                .define("leafCulling", true);

        LEAF_LIFETIME = BUILDER
                .comment("Leaf lifetime in ticks (20 Ticks = 1 Second).")
                .translation("config.zephyr_hauler.leafLifetime")
                .defineInRange("leafLifetime", 160, 20, 1200);

        BUILDER.pop();

        // --- SUBMENÚ DE VIENTO ---
        BUILDER.push("visual_wind_settings");

        ENABLE_WIND_EFFECTS = BUILDER
                .comment("Master Switch: Enables visual wind streams and gusts.")
                .translation("config.zephyr_hauler.enableWindEffects")
                .define("enableWindEffects", true);

        BUILDER.pop();

        // --- SUBMENÚ DE MEJORAS VISUALES ---
        BUILDER.push("visual_upgrades_settings");

        ENABLE_VISUAL_UPGRADES = BUILDER
                .comment("Master Switch: Enables 3D visual upgrades on Zephyr Haulers.")
                .translation("config.zephyr_hauler.enableVisualUpgrades")
                .define("enableVisualUpgrades", true);

        SHOW_WAXED_UPGRADE = BUILDER
                .comment("Shows the waxed cover layer on upgraded Haulers.")
                .translation("config.zephyr_hauler.showWaxedUpgrade")
                .define("showWaxedUpgrade", true);

        SHOW_REINFORCED_UPGRADE = BUILDER
                .comment("Shows the iron corner reinforcements on upgraded Haulers.")
                .translation("config.zephyr_hauler.showReinforcedUpgrade")
                .define("showReinforcedUpgrade", true);

        SHOW_INSULATED_UPGRADE = BUILDER
                .comment("Shows the lightning rod antenna on insulated Haulers.")
                .translation("config.zephyr_hauler.showInsulatedUpgrade")
                .define("showInsulatedUpgrade", true);

        SHOW_NETHERITE_UPGRADE = BUILDER
                .comment("Shows the netherite plating details on upgraded Haulers.")
                .translation("config.zephyr_hauler.showNetheriteUpgrade")
                .define("showNetheriteUpgrade", true);

        SHOW_ABSORBERS_UPGRADE = BUILDER
                .comment("Shows the shock absorbers on the lower struts.")
                .translation("config.zephyr_hauler.showAbsorbersUpgrade")
                .define("showAbsorbersUpgrade", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}