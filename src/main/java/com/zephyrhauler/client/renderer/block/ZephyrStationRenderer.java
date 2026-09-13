package com.zephyrhauler.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zephyrhauler.block.ZephyrStationBlock;
import com.zephyrhauler.block.entity.ZephyrStationBlockEntity;
import com.zephyrhauler.client.model.ZephyrStationModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ZephyrStationRenderer extends GeoBlockRenderer<ZephyrStationBlockEntity> {

    public ZephyrStationRenderer(BlockEntityRendererProvider.Context context) {
        super(new ZephyrStationModel());
    }

    @Override
    public void render(ZephyrStationBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        super.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);

        ItemStack stack = blockEntity.getStoredHauler();

        if (!stack.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 1.1D, 0.5D);
            poseStack.scale(1.5f, 1.5f, 1.5f);

            long time = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : 0;
            float angle = (time + partialTick) * 3.0F;
            poseStack.mulPose(Axis.YP.rotationDegrees(angle));

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    stack,
                    ItemDisplayContext.GROUND,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    bufferSource,
                    blockEntity.getLevel(),
                    0
            );

            poseStack.popPose();
        }
    }
}