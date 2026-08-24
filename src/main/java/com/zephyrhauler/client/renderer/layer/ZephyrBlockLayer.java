package com.zephyrhauler.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zephyrhauler.entity.ZephyrHaulerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtil;

public class ZephyrBlockLayer extends GeoRenderLayer<ZephyrHaulerEntity> {

    public ZephyrBlockLayer(GeoEntityRenderer<ZephyrHaulerEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, ZephyrHaulerEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {

        GeoBone payloadBone = getGeoModel().getBone("payload_bone").orElse(null);

        if (payloadBone != null) {
            BlockState state = animatable.getCapturedBlock();

            if (state != null && !state.isAir()) {
                poseStack.pushPose();

                RenderUtil.translateMatrixToBone(poseStack, payloadBone);
                poseStack.translate(-0.5D, -0.5D, -0.5D);
                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                        state,
                        poseStack,
                        bufferSource,
                        packedLight,
                        packedOverlay,
                        ModelData.EMPTY,
                        null
                );

                poseStack.popPose();
            }
        }
    }
}