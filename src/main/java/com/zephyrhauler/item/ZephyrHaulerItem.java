package com.zephyrhauler.item;

import com.zephyrhauler.block.entity.ZephyrDockBlockEntity;
import com.zephyrhauler.block.entity.ZephyrHubBlockEntity;
import com.zephyrhauler.component.ZephyrDataComponents;
import com.zephyrhauler.entity.ZephyrHaulerEntity;
import com.zephyrhauler.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Clearable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;

public class ZephyrHaulerItem extends Item implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ZephyrHaulerItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getEnchantmentValue() {
        return 14;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.level.LevelReader level, BlockPos pos, Player player) {
        Block block = level.getBlockState(pos).getBlock();
        return block instanceof com.zephyrhauler.block.ZephyrDockBlock || block instanceof com.zephyrhauler.block.ZephyrHubBlock;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String color = stack.getOrDefault(ZephyrDataComponents.HAULER_COLOR.get(), "white");
        Component colorComp = Component.translatable("color.zephyr_hauler." + color);
        tooltip.add(Component.translatable("tooltip.zephyr_hauler.color", colorComp).withStyle(ChatFormatting.GOLD));

        List<String> currentUpgrades = stack.getOrDefault(ZephyrDataComponents.HAULER_UPGRADES.get(), new java.util.ArrayList<>());
        int maxDur = 8;
        if (currentUpgrades.contains("netherite_plating")) {
            maxDur = 32;
        } else if (currentUpgrades.contains("reinforced")) {
            maxDur = 12;
        }

        int durability = stack.getOrDefault(ZephyrDataComponents.HAULER_DURABILITY.get(), maxDur);

        if (durability > 0) {
            ChatFormatting durColor = durability > (maxDur / 2) ? ChatFormatting.GREEN : (durability > (maxDur / 4) ? ChatFormatting.YELLOW : ChatFormatting.RED);
            tooltip.add(Component.translatable("tooltip.zephyr_hauler.durability_dynamic", durability, maxDur).withStyle(durColor));
        } else {
            String damageType = stack.getOrDefault(ZephyrDataComponents.HAULER_DAMAGE.get(), "unknown");
            Component damageComp = Component.translatable("damage.zephyr_hauler." + damageType);

            tooltip.add(Component.translatable("tooltip.zephyr_hauler.inoperable").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
            tooltip.add(Component.translatable("tooltip.zephyr_hauler.damage_cause", damageComp).withStyle(ChatFormatting.RED));
        }

        if (stack.has(ZephyrDataComponents.HAULER_UPGRADES.get())) {
            List<String> upgrades = stack.get(ZephyrDataComponents.HAULER_UPGRADES.get());
            if (upgrades != null && !upgrades.isEmpty()) {

                boolean isAltDown = false;
                try {
                    isAltDown = net.minecraft.client.gui.screens.Screen.hasAltDown();
                } catch (Exception ignored) { }

                tooltip.add(Component.literal(" "));

                if (isAltDown) {
                    for (String upgrade : upgrades) {
                        Component upgradeComp = Component.translatable("upgrade.zephyr_hauler." + upgrade);
                        tooltip.add(Component.translatable("tooltip.zephyr_hauler.upgrade_format", upgradeComp).withStyle(ChatFormatting.AQUA));
                    }
                } else {
                    tooltip.add(Component.translatable("tooltip.zephyr_hauler.hold_alt").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                }
            }
        }

        if (stack.has(ZephyrDataComponents.TARGET_POS.get())) {
            String targetName = stack.getOrDefault(ZephyrDataComponents.TARGET_NAME.get(), "Muelle Desconocido");
            GlobalPos pos = stack.get(ZephyrDataComponents.TARGET_POS.get());
            tooltip.add(Component.literal(" "));
            tooltip.add(Component.translatable("tooltip.zephyr_hauler.linked_to", Component.literal(targetName).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.zephyr_hauler.dimension", pos.dimension().location().getPath()).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.zephyr_hauler.coords", pos.pos().getX(), pos.pos().getY(), pos.pos().getZ()).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal(" "));
            tooltip.add(Component.translatable("tooltip.zephyr_hauler.unlinked").withStyle(ChatFormatting.RED));
        }

        net.minecraft.world.item.enchantment.ItemEnchantments enchants = stack.get(net.minecraft.core.component.DataComponents.ENCHANTMENTS);
        if (enchants != null) {
            for (var entry : enchants.entrySet()) {
                if (entry.getKey().is(net.minecraft.world.item.enchantment.Enchantments.MENDING)) {
                    tooltip.add(Component.literal(" "));
                    tooltip.add(Component.translatable("tooltip.zephyr_hauler.mending_joke")
                            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
                    break;
                }
            }
        }

        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player == null) {
            return InteractionResult.PASS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        BlockState state = level.getBlockState(pos);

        if (be instanceof ZephyrDockBlockEntity dockBE) {
            if (player.isShiftKeyDown()) {

                if (state.hasProperty(com.zephyrhauler.block.ZephyrDockBlock.HUB_MODE) &&
                        state.getValue(com.zephyrhauler.block.ZephyrDockBlock.HUB_MODE)) {
                    if (!level.isClientSide) {
                        player.displayClientMessage(
                                Component.translatable("message.zephyr_hauler.hub.link_to_hub").withStyle(ChatFormatting.RED),
                                true
                        );
                    }
                    return InteractionResult.FAIL;
                }

                if (stack.has(ZephyrDataComponents.TARGET_POS.get())) {
                    if (!level.isClientSide) {
                        String existingName = stack.getOrDefault(ZephyrDataComponents.TARGET_NAME.get(), "un destino");
                        player.displayClientMessage(
                                Component.translatable("message.zephyr_hauler.hauler.already_linked", Component.literal(existingName).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.RED),
                                true
                        );
                    }
                    return InteractionResult.FAIL;
                }

                if (dockBE.getLinkId() != null) {
                    if (!level.isClientSide) {
                        player.displayClientMessage(
                                Component.translatable("message.zephyr_hauler.hauler.dock_reserved").withStyle(ChatFormatting.RED),
                                true
                        );
                    }
                    return InteractionResult.FAIL;
                }

                String dockName = dockBE.getCustomName();
                String displayName = dockName.isEmpty() ? Component.translatable("dock.zephyr_hauler.generic_dock").getString() : dockName;
                GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);

                stack.set(ZephyrDataComponents.TARGET_POS.get(), globalPos);
                stack.set(ZephyrDataComponents.TARGET_NAME.get(), displayName);

                if (!level.isClientSide) {
                    UUID newLinkId = UUID.randomUUID();
                    dockBE.setLinkId(newLinkId);
                    stack.set(ZephyrDataComponents.LINK_ID.get(), newLinkId);
                    player.displayClientMessage(
                            Component.translatable("message.zephyr_hauler.hauler.linked_success", Component.literal(displayName).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GREEN),
                            true
                    );
                } else {
                    stack.set(ZephyrDataComponents.LINK_ID.get(), UUID.randomUUID());
                }

                return InteractionResult.sidedSuccess(level.isClientSide);
            } else {
                if (!level.isClientSide) {
                    player.displayClientMessage(
                            Component.translatable("message.zephyr_hauler.hauler.link_hint").withStyle(ChatFormatting.YELLOW),
                            true
                    );
                }
                return InteractionResult.SUCCESS;
            }
        }
        else if (be instanceof ZephyrHubBlockEntity hubBE) {
            if (player.isShiftKeyDown()) {
                if (stack.has(ZephyrDataComponents.TARGET_POS.get())) {
                    if (!level.isClientSide) {
                        String existingName = stack.getOrDefault(ZephyrDataComponents.TARGET_NAME.get(), "un destino");
                        player.displayClientMessage(
                                Component.translatable("message.zephyr_hauler.hauler.already_linked", Component.literal(existingName).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.RED),
                                true
                        );
                    }
                    return InteractionResult.FAIL;
                }

                String hubName = hubBE.getCustomName();
                String displayName = hubName.isEmpty() ? "Zephyr Hub" : hubName;
                GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);

                stack.set(ZephyrDataComponents.TARGET_POS.get(), globalPos);
                stack.set(ZephyrDataComponents.TARGET_NAME.get(), displayName);

                if (!level.isClientSide) {
                    stack.set(ZephyrDataComponents.LINK_ID.get(), hubBE.getHubId());
                    player.displayClientMessage(
                            Component.translatable("message.zephyr_hauler.hauler.linked_success", Component.literal(displayName).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.AQUA),
                            true
                    );
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            } else {
                if (!level.isClientSide) {
                    player.displayClientMessage(
                            Component.translatable("message.zephyr_hauler.hauler.link_hint").withStyle(ChatFormatting.YELLOW),
                            true
                    );
                }
                return InteractionResult.SUCCESS;
            }
        }

        if (!stack.has(ZephyrDataComponents.TARGET_POS.get())) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("message.zephyr_hauler.hauler.must_link_first").withStyle(ChatFormatting.RED),
                        true
                );
            }
            return InteractionResult.FAIL;
        }

        List<String> currentUpgrades = stack.getOrDefault(ZephyrDataComponents.HAULER_UPGRADES.get(), new java.util.ArrayList<>());
        int maxDur = 8;
        if (currentUpgrades.contains("netherite_plating")) {
            maxDur = 32;
        } else if (currentUpgrades.contains("reinforced")) {
            maxDur = 12;
        }

        int durability = stack.getOrDefault(ZephyrDataComponents.HAULER_DURABILITY.get(), maxDur);

        if (durability <= 0) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("message.zephyr_hauler.hauler.inoperable_warning").withStyle(ChatFormatting.DARK_RED),
                        true
                );
            }
            return InteractionResult.FAIL;
        }

        if (be != null && !state.is(com.zephyrhauler.registry.ModTags.Blocks.HAULER_BLACKLIST)) {
            if (!level.isClientSide) {
                CompoundTag savedBlockEntityData = be.saveWithFullMetadata(level.registryAccess());
                ItemStack balloonStack = stack.copy();
                balloonStack.setCount(1);

                Clearable.tryClear(be);
                level.removeBlock(pos, false);

                ZephyrHaulerEntity hauler = ModEntities.ZEPHYR_HAULER.get().create(level);
                if (hauler != null) {
                    hauler.setPos(pos.getX() + 0.5, pos.getY() + 0.0, pos.getZ() + 0.5);
                    hauler.setCapturedData(state, savedBlockEntityData, balloonStack);
                    hauler.setWaitingForLaunch(true);

                    String color = stack.getOrDefault(ZephyrDataComponents.HAULER_COLOR.get(), "white");
                    hauler.setColor(color);

                    level.addFreshEntity(hauler);
                }

                player.displayClientMessage(
                        Component.translatable("message.zephyr_hauler.hauler.anchored_ready").withStyle(ChatFormatting.YELLOW),
                        true
                );

                if (!player.isCreative()) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}