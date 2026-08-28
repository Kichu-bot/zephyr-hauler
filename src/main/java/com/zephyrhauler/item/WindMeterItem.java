package com.zephyrhauler.item;

import com.zephyrhauler.util.ZephyrWindSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class WindMeterItem extends Item implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public WindMeterItem(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "wind_controller", 5, state -> {
            state.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
            return PlayState.CONTINUE;
        })
                .triggerableAnim("NONE", RawAnimation.begin().thenPlay("idle"))
                .triggerableAnim("NORTH", RawAnimation.begin().thenPlay("north").thenLoop("idle"))
                .triggerableAnim("NORTH_EAST", RawAnimation.begin().thenPlay("north_east").thenLoop("idle"))
                .triggerableAnim("EAST", RawAnimation.begin().thenPlay("east").thenLoop("idle"))
                .triggerableAnim("SOUTH_EAST", RawAnimation.begin().thenPlay("south_east").thenLoop("idle"))
                .triggerableAnim("SOUTH", RawAnimation.begin().thenPlay("south").thenLoop("idle"))
                .triggerableAnim("SOUTH_WEST", RawAnimation.begin().thenPlay("south_west").thenLoop("idle"))
                .triggerableAnim("WEST", RawAnimation.begin().thenPlay("west").thenLoop("idle"))
                .triggerableAnim("NORTH_WEST", RawAnimation.begin().thenPlay("north_west").thenLoop("idle")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            ZephyrWindSystem.WindInfo wind = ZephyrWindSystem.getCurrentWind(level);
            long id = GeoItem.getOrAssignId(stack, (ServerLevel) level);

            this.triggerAnim(player, id, "wind_controller", wind.direction().name());

            double yPos = player.getY();
            MutableComponent altitudeNote = Component.empty();
            ChatFormatting altColor = ChatFormatting.GRAY;

            if (yPos < 60) {
                altitudeNote = Component.literal(" ").append(Component.translatable("message.zephyr_hauler.wind.altitude.low"));
                altColor = ChatFormatting.DARK_GRAY;
            } else if (yPos > 128) {
                altitudeNote = Component.literal(" ").append(Component.translatable("message.zephyr_hauler.wind.altitude.high"));
                altColor = ChatFormatting.GREEN;
            }

            if (wind.direction() == ZephyrWindSystem.WindDirection.NONE) {
                level.playSound(null, player.blockPosition(), SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 0.1f, 0.5f);
                player.displayClientMessage(
                        Component.translatable("message.zephyr_hauler.wind.calm").withStyle(ChatFormatting.GRAY), true
                );
            } else {
                String strengthKey = "message.zephyr_hauler.wind.soft_breeze";
                ChatFormatting color = ChatFormatting.AQUA;
                float volume = 0.3f;
                float pitch = 1.0f;

                if (level.isThundering()) {
                    strengthKey = "message.zephyr_hauler.wind.hurricane_wind";
                    color = ChatFormatting.DARK_RED;
                    volume = 1.0f;
                    pitch = 0.5f;
                } else if (level.isRaining()) {
                    strengthKey = "message.zephyr_hauler.wind.strong_wind";
                    color = ChatFormatting.BLUE;
                    volume = 0.7f;
                    pitch = 0.8f;
                }

                if (level instanceof ServerLevel serverLevel) {
                    Vec3 look = player.getLookAngle();
                    double px = player.getX() + look.x;
                    double py = player.getY() + 1.5;
                    double pz = player.getZ() + look.z;

                    serverLevel.sendParticles(ParticleTypes.CLOUD, px, py, pz, 3, 0.2, 0.2, 0.2, 0.0);
                }

                Component strengthComp = Component.translatable(strengthKey);
                Component directionComp = Component.translatable(wind.direction().translationKey);

                player.displayClientMessage(
                        Component.translatable("message.zephyr_hauler.wind.message", strengthComp, directionComp)
                                .withStyle(color)
                                .append(altitudeNote.withStyle(altColor)),
                        true
                );
            }
        }

        return InteractionResultHolder.success(stack);
    }
}