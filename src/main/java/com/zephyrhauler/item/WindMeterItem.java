package com.zephyrhauler.item;

import com.zephyrhauler.util.ZephyrWindSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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

            if (wind.direction() == ZephyrWindSystem.WindDirection.NONE) {
                player.displayClientMessage(
                        Component.translatable("message.zephyr_hauler.wind.calm").withStyle(ChatFormatting.GRAY),
                        true
                );
            } else {
                String strengthKey = "message.zephyr_hauler.wind.soft_breeze";
                ChatFormatting color = ChatFormatting.AQUA;

                if (level.isThundering()) {
                    strengthKey = "message.zephyr_hauler.wind.hurricane_wind";
                    color = ChatFormatting.DARK_RED;
                } else if (level.isRaining()) {
                    strengthKey = "message.zephyr_hauler.wind.strong_wind";
                    color = ChatFormatting.BLUE;
                }

                Component strengthComp = Component.translatable(strengthKey);
                Component directionComp = Component.translatable(wind.direction().translationKey);

                player.displayClientMessage(
                        Component.translatable("message.zephyr_hauler.wind.message", strengthComp, directionComp)
                                .withStyle(color),
                        true
                );
            }
        }

        return InteractionResultHolder.success(stack);
    }
}