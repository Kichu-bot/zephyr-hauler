package com.zephyrhauler.item;

import com.zephyrhauler.block.entity.ZephyrDockBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class WindSensorItem extends BlockItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public WindSensorItem(Block block, Properties properties) {
        super(block, properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        if (player != null && player.isShiftKeyDown()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ZephyrDockBlockEntity dockBE) {
                if (!level.isClientSide) {
                    CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                    CompoundTag tag = customData.copyTag();
                    tag.putLong("LinkedDockPos", pos.asLong());
                    String dockName = dockBE.getCustomName().isEmpty() ? Component.translatable("dock.zephyr_hauler.generic_dock").getString() : dockBE.getCustomName();
                    tag.putString("LinkedDockName", dockName);
                    tag.putString("LinkedDockDim", level.dimension().location().toString());

                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                    player.displayClientMessage(
                            Component.translatable("message.zephyr_hauler.sensor.linked_item")
                                    .withStyle(ChatFormatting.GREEN), true
                    );
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        if (tag.contains("LinkedDockPos")) {
            String dockName = tag.getString("LinkedDockName");
            if (dockName.isEmpty()) dockName = "Unknown Dock";

            BlockPos pos = BlockPos.of(tag.getLong("LinkedDockPos"));
            String coords = "X: " + pos.getX() + "  Y: " + pos.getY() + "  Z: " + pos.getZ();

            String dim = tag.getString("LinkedDockDim").replace("minecraft:", "");

            tooltip.add(Component.translatable("message.zephyr_hauler.sensor.locked").withStyle(ChatFormatting.RED));
            tooltip.add(Component.literal(" " + dockName).withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.literal(" " + coords).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(" " + dim).withStyle(ChatFormatting.DARK_AQUA));
        } else {
            tooltip.add(Component.translatable("tooltip.zephyr_hauler.sensor.unlinked").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) { }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}