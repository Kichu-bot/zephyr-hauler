package com.zephyrhauler.registry;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.entity.ZephyrHaulerEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, ZephyrHauler.MOD_ID);

    public static final Supplier<EntityType<ZephyrHaulerEntity>> ZEPHYR_HAULER = ENTITIES.register("zephyr_hauler",
            () -> EntityType.Builder.<ZephyrHaulerEntity>of(ZephyrHaulerEntity::new, MobCategory.MISC)
                    .sized(1.2F, 2.5F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("zephyr_hauler"));
}