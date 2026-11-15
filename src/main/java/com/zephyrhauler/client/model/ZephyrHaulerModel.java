package com.zephyrhauler.client.model;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.config.ZephyrClientConfig;
import com.zephyrhauler.entity.ZephyrHaulerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public class ZephyrHaulerModel extends GeoModel<ZephyrHaulerEntity> {

    @Override
    public ResourceLocation getModelResource(ZephyrHaulerEntity object) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "geo/entity/zephyr_hauler.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ZephyrHaulerEntity object) {
        String color = object.getColor();
        if (color == null || color.isEmpty()) {
            color = "white";
        }
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "textures/entity/zephyr_hauler_" + color + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(ZephyrHaulerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "animations/entity/zephyr_hauler.animation.json");
    }

    @Override
    public void setCustomAnimations(ZephyrHaulerEntity entity, long instanceId, AnimationState<ZephyrHaulerEntity> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        boolean masterEnabled = ZephyrClientConfig.ENABLE_VISUAL_UPGRADES.get();

        boolean showWaxed = masterEnabled && ZephyrClientConfig.SHOW_WAXED_UPGRADE.get() && entity.hasWaxedUpgrade();
        setBoneHidden("reinforced_base", showWaxed);
        setBoneHidden("up_waxed", !showWaxed);

        boolean hasReinforcedOrNetherite = entity.hasReinforcedUpgrade() || entity.hasNetheriteUpgrade();
        boolean showReinforced = masterEnabled && ZephyrClientConfig.SHOW_REINFORCED_UPGRADE.get() && hasReinforcedOrNetherite;
        setBoneHidden("up_reinforced", !showReinforced);

        boolean showInsulated = masterEnabled && ZephyrClientConfig.SHOW_INSULATED_UPGRADE.get() && entity.hasInsulatedUpgrade();
        setBoneHidden("up_insulated", !showInsulated);

        boolean showNetherite = masterEnabled && ZephyrClientConfig.SHOW_NETHERITE_UPGRADE.get() && entity.hasNetheriteUpgrade();
        setBoneHidden("up_netherite", !showNetherite);

        boolean showAbsorbers = masterEnabled && ZephyrClientConfig.SHOW_ABSORBERS_UPGRADE.get() && entity.hasAbsorbersUpgrade();
        setBoneHidden("up_absorbers", !showAbsorbers);
    }

    private void setBoneHidden(String boneName, boolean hidden) {
        GeoBone bone = this.getAnimationProcessor().getBone(boneName);
        if (bone != null) {
            bone.setHidden(hidden);
        }
    }
}