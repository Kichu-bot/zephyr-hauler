package com.zephyrhauler.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zephyrhauler.block.ZephyrHubBlock;
import com.zephyrhauler.block.entity.ZephyrHubBlockEntity;
import com.zephyrhauler.client.model.ZephyrHubModel;
import com.zephyrhauler.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ZephyrHubRenderer extends GeoBlockRenderer<ZephyrHubBlockEntity> {
    private ItemStack controllerStack = ItemStack.EMPTY;

    public ZephyrHubRenderer(BlockEntityRendererProvider.Context context) {
        super(new ZephyrHubModel());
    }

    @Override
    public void render(ZephyrHubBlockEntity animatable, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        super.render(animatable, partialTick, poseStack, bufferSource, packedLight, packedOverlay);

        if (controllerStack.isEmpty()) {
            controllerStack = new ItemStack(ModItems.ZEPHYR_CONTROLLER.get());
        }

        poseStack.pushPose();
        poseStack.translate(0.5f, 0.0f, 0.5f);

        if (animatable.hasLevel()) {
            Direction facing = animatable.getBlockState().getValue(ZephyrHubBlock.FACING);
            poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot() + 180f));
        }

        poseStack.translate(-0.5f, 0.0f, -0.5f);

        poseStack.translate(0.4f, 0.65f, 0.5f);

        poseStack.mulPose(Axis.XP.rotationDegrees(90f));

        poseStack.mulPose(Axis.YP.rotationDegrees(0f));

        poseStack.scale(0.5f, 0.5f, 0.5f);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                controllerStack,
                ItemDisplayContext.FIXED,
                packedLight,
                packedOverlay,
                poseStack,
                bufferSource,
                animatable.getLevel(),
                0
        );

        poseStack.popPose();
    }
}