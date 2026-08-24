package com.zephyrhauler.registry;

import com.zephyrhauler.ZephyrHauler;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {

        public static final TagKey<Block> HAULER_BLACKLIST = tag("hauler_blacklist");

        private static TagKey<Block> tag(String name) {
            return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, name));
        }
    }
}