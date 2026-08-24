package com.zephyrhauler.item;

import com.zephyrhauler.block.entity.ZephyrDockBlockEntity;
import com.zephyrhauler.component.DockEntry;
import com.zephyrhauler.component.ZephyrDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

// --- IMPORTACIONES DE GECKOLIB ---
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

public class ZephyrControllerItem extends Item implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ZephyrControllerItem(Properties properties) {
        super(properties.stacksTo(1));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, state -> {
            state.getController().setAnimation(RawAnimation.begin().thenPlayAndHold("idle"));
            return PlayState.CONTINUE;
        })
                .triggerableAnim("writing_trigger", RawAnimation.begin().thenLoop("writing")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.level.LevelReader level, BlockPos pos, Player player) {
        return level.getBlockState(pos).getBlock() instanceof com.zephyrhauler.block.ZephyrDockBlock;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player == null) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof ZephyrDockBlockEntity dockBE && player.isShiftKeyDown()) {
            String dockName = dockBE.getCustomName().isEmpty() ? Component.translatable("dock.zephyr_hauler.generic_dock").getString() : dockBE.getCustomName();
            GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);
            DockEntry newEntry = new DockEntry(dockName, globalPos);

            List<DockEntry> currentDocks = new ArrayList<>(stack.getOrDefault(ZephyrDataComponents.SAVED_DOCKS.get(), List.of()));
            boolean alreadyExists = currentDocks.stream().anyMatch(entry -> entry.pos().equals(globalPos));

            if (alreadyExists) {
                currentDocks.removeIf(entry -> entry.pos().equals(globalPos));
                currentDocks.add(newEntry);
                stack.set(ZephyrDataComponents.SAVED_DOCKS.get(), currentDocks);

                if (!level.isClientSide) {
                    player.displayClientMessage(
                            Component.translatable("message.zephyr_hauler.controller.dock_updated",
                                    Component.literal(dockName).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.YELLOW),
                            true
                    );
                }
            } else {
                currentDocks.add(newEntry);
                stack.set(ZephyrDataComponents.SAVED_DOCKS.get(), currentDocks);

                if (!level.isClientSide) {
                    player.displayClientMessage(
                            Component.translatable("message.zephyr_hauler.controller.dock_registered",
                                    Component.literal(dockName).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GREEN),
                            true
                    );
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                long id = GeoItem.getOrAssignId(stack, (ServerLevel) level);

                this.triggerAnim(player, id, "controller", "writing_trigger");

                player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                        (windowId, inv, p) -> new com.zephyrhauler.menu.ZephyrControllerMenu(windowId, inv),
                        Component.translatable("container.zephyr_hauler.controller")
                ));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        List<DockEntry> docks = stack.getOrDefault(ZephyrDataComponents.SAVED_DOCKS.get(), List.of());

        if (docks.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.zephyr_hauler.controller.no_docks").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.zephyr_hauler.controller.register_hint").withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.translatable("tooltip.zephyr_hauler.controller.saved_count", docks.size()).withStyle(ChatFormatting.GOLD));
            int limit = Math.min(docks.size(), 4);
            for (int i = 0; i < limit; i++) {
                DockEntry entry = docks.get(i);
                tooltip.add(Component.literal(" - ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(entry.name()).withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" (" + entry.pos().pos().toShortString() + ")").withStyle(ChatFormatting.DARK_GRAY)));
            }
            if (docks.size() > 4) {
                tooltip.add(Component.translatable("tooltip.zephyr_hauler.controller.more_docks", (docks.size() - 4)).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        super.appendHoverText(stack, context, tooltip, flag);
    }
}