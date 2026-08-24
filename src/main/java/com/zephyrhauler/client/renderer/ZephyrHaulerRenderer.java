package com.zephyrhauler.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zephyrhauler.client.model.ZephyrHaulerModel; // <-- IMPORTANTE: Importamos tu modelo
import com.zephyrhauler.entity.ZephyrHaulerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtil;

@OnlyIn(Dist.CLIENT)
public class ZephyrHaulerRenderer extends GeoEntityRenderer<ZephyrHaulerEntity> {

    public ZephyrHaulerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ZephyrHaulerModel());
        this.addRenderLayer(new BlockPayloadLayer(this));
    }

    class BlockPayloadLayer extends GeoRenderLayer<ZephyrHaulerEntity> {
        public BlockPayloadLayer(GeoEntityRenderer<ZephyrHaulerEntity> entityRendererIn) {
            super(entityRendererIn);
        }

        @Override
        public void render(PoseStack poseStack, ZephyrHaulerEntity animatable, BakedGeoModel bakedModel,
                           RenderType renderType, MultiBufferSource bufferSource,
                           com.mojang.blaze3d.vertex.VertexConsumer buffer,
                           float partialTick, int packedLight, int packedOverlay) {

            BlockState capturedBlock = animatable.getCapturedBlock();
            if (capturedBlock == null || capturedBlock.isAir()) return;

            ItemStack blockItem = animatable.getDisplayItem();
            if (blockItem.isEmpty()) return;

            poseStack.pushPose();

            GeoBone payloadBone = bakedModel.getBone("payload_bone").orElse(null);
            if (payloadBone != null) {
                RenderUtil.prepMatrixForBone(poseStack, payloadBone);
            }

            poseStack.translate(0.0, 0.5, 0.0);

            net.minecraft.core.Direction facingDir = null;
            boolean is3D = false;
            net.minecraft.core.Direction.Axis pillarAxis = null;

            String blockName = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(capturedBlock.getBlock()).getPath();

            for (net.minecraft.world.level.block.state.properties.Property<?> prop : capturedBlock.getProperties()) {
                Object rawValue = capturedBlock.getValue(prop);
                String propName = prop.getName().toLowerCase();

                if (propName.contains("facing") && rawValue instanceof net.minecraft.core.Direction) {
                    facingDir = (net.minecraft.core.Direction) rawValue;
                    is3D = prop.getPossibleValues().contains(net.minecraft.core.Direction.UP);
                } else if (propName.contains("axis") && rawValue instanceof net.minecraft.core.Direction.Axis) {
                    pillarAxis = (net.minecraft.core.Direction.Axis) rawValue;
                }
            }

            if (facingDir != null) {
                if (is3D || blockName.contains("barrel")) {
                    switch (facingDir) {
                        case DOWN: poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180.0f)); break;
                        case NORTH: poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90.0f)); break;
                        case SOUTH: poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0f)); break;
                        case WEST: poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(90.0f)); break;
                        case EAST: poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-90.0f)); break;
                        case UP: default: break;
                    }
                } else {
                    float rotation = -facingDir.toYRot();
                    float offset = 180.0f;

                    if (blockName.contains("drawer")) {
                        offset = 0.0f;
                    }

                    poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation + offset));
                }
            } else if (pillarAxis != null) {
                if (pillarAxis == net.minecraft.core.Direction.Axis.X) {
                    poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-90.0f));
                } else if (pillarAxis == net.minecraft.core.Direction.Axis.Z) {
                    poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0f));
                }
            }

            poseStack.scale(2.0f, 2.0f, 2.0f);

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    blockItem,
                    net.minecraft.world.item.ItemDisplayContext.FIXED,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    bufferSource,
                    animatable.level(),
                    animatable.getId()
            );

            poseStack.popPose();
        }
    }
}