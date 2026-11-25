package com.buuz135.transfer_labels.client;

import com.buuz135.transfer_labels.LabelInteractEvents;
import com.buuz135.transfer_labels.TransferLabels;
import com.buuz135.transfer_labels.item.TransferLabelItem;
import com.buuz135.transfer_labels.storage.client.LabelClientStorage;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class LabelClientEvents {

    private static final ContextKey<List<LabelRenderData>> LABEL_RENDER_DATA = new ContextKey<>(Identifier.fromNamespaceAndPath(TransferLabels.MODID, "labels"));
    private static final RenderPipeline LABEL_NO_DEPTH_PIPELINE = RenderPipeline.builder(RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(TransferLabels.MODID, "pipeline/label_no_depth"))
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withShaderDefine("PER_FACE_LIGHTING")
            .withSampler("Sampler1")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .withDepthStencilState(Optional.empty())
            .build();
    private static final Function<Identifier, RenderType> LABEL_NO_DEPTH_RENDER_TYPE = Util.memoize(texture -> RenderType.create(
            "transfer_label_no_depth",
            RenderSetup.builder(LABEL_NO_DEPTH_PIPELINE)
                    .withTexture("Sampler0", texture)
                    .useOverlay()
                    .sortOnUpload()
                    .createRenderSetup()));
    public static final List<LabelInteractEvents.DelayedEvent> CLIENT_UPDATE = new ArrayList<>();

    public static void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(LABEL_NO_DEPTH_PIPELINE);
    }

    @SubscribeEvent
    public void blockOverlayEvent(ExtractBlockOutlineRenderStateEvent event) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        var distance = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).getValue();
        var isHoldingAccessor = player.getItemInHand(InteractionHand.MAIN_HAND).is(TransferLabels.LABEL_ACCESSOR.get());
        var isHoldingLabel = player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof TransferLabelItem || isHoldingAccessor;
        if (!isHoldingLabel) return;
        var nearbyLabels = LabelClientStorage.getNearbyLabels(event.getLevel(), event.getBlockPos(), (int) (distance * distance));
        var pair = RayTraceUtils.rayTraceVoxelShape(nearbyLabels, event.getLevel(), player, 0, isHoldingAccessor ? null : event.getBlockPos());
        if (pair == null) return;
        var box = pair.getFirst().collectShapes(pair.getSecond());
        if (box == null) return;
        var shape = Shapes.create(box);
        var cameraPos = event.getCamera().position();
        BlockOutlineRenderState state = event.getLevelRenderState().blockOutlineRenderState;
        int outlineColor = state != null && state.highContrast() ? -11010079 : ARGB.black(102);
        event.addCustomRenderer((renderState, buffer, stack, translucentPass, levelRenderState) -> {
            ShapeRenderer.renderShape(stack, buffer.getBuffer(RenderTypes.lines()), shape, -cameraPos.x, -cameraPos.y, -cameraPos.z, outlineColor, Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth);
            return true;
        });
    }

    @SubscribeEvent
    public void extractLabels(ExtractLevelRenderStateEvent event) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            event.getRenderState().setRenderData(LABEL_RENDER_DATA, List.of());
            return;
        }
        var isHoldingAccessor = player.getItemInHand(InteractionHand.MAIN_HAND).is(TransferLabels.LABEL_ACCESSOR.get());
        var isHoldingLabel = player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof TransferLabelItem || isHoldingAccessor;
        var renderWithoutDepth = isHoldingLabel && player.isShiftKeyDown();
        var cameraPos = event.getCamera().position();
        List<LabelRenderData> renderData = new ArrayList<>();
        for (var label : LabelClientStorage.getNearbyLabels(event.getLevel(), player.blockPosition(), 20 * 20)) {
            for (var entry : label.getLabels().entrySet()) {
                renderData.add(new LabelRenderData(
                        label.getPos().getX() - cameraPos.x,
                        label.getPos().getY() - cameraPos.y,
                        label.getPos().getZ() - cameraPos.z,
                        entry.getKey(),
                        getTexture(entry.getValue().getLabel()),
                        isHoldingLabel ? 255 : 100,
                        renderWithoutDepth));
            }
        }
        event.getRenderState().setRenderData(LABEL_RENDER_DATA, List.copyOf(renderData));
    }

    @SubscribeEvent
    public void submitLabels(SubmitCustomGeometryEvent event) {
        var renderData = event.getLevelRenderState().getRenderData(LABEL_RENDER_DATA);
        if (renderData == null) return;
        var poseStack = event.getPoseStack();
        var collector = event.getSubmitNodeCollector();
        for (var label : renderData) {
            poseStack.pushPose();
            poseStack.translate(label.x(), label.y(), label.z());
            var renderType = label.renderWithoutDepth() ? LABEL_NO_DEPTH_RENDER_TYPE.apply(label.texture()) : RenderTypes.entityTranslucentEmissive(label.texture());
            collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> renderLabel(pose, buffer, label.direction(), label.alpha()));
            poseStack.popPose();
        }
    }

    private static void renderLabel(PoseStack.Pose pose, VertexConsumer buffer, Direction direction, int alpha) {
        switch (direction) {
            case NORTH -> quad(pose, buffer, alpha, 0, 0, -0.001f, 0, 1, -0.001f, 1, 1, -0.001f, 1, 0, -0.001f, 0, 0, -1);
            case SOUTH -> quad(pose, buffer, alpha, 1, 0, 1.001f, 1, 1, 1.001f, 0, 1, 1.001f, 0, 0, 1.001f, 0, 0, 1);
            case WEST -> quad(pose, buffer, alpha, -0.001f, 0, 1, -0.001f, 1, 1, -0.001f, 1, 0, -0.001f, 0, 0, -1, 0, 0);
            case EAST -> quad(pose, buffer, alpha, 1.001f, 0, 0, 1.001f, 1, 0, 1.001f, 1, 1, 1.001f, 0, 1, 1, 0, 0);
            case UP -> quad(pose, buffer, alpha, 0, 1.001f, 0, 0, 1.001f, 1, 1, 1.001f, 1, 1, 1.001f, 0, 0, 1, 0);
            case DOWN -> quad(pose, buffer, alpha, 0, -0.001f, 1, 0, -0.001f, 0, 1, -0.001f, 0, 1, -0.001f, 1, 0, -1, 0);
        }
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, int alpha,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4,
                             float normalX, float normalY, float normalZ) {
        vertex(pose, buffer, x1, y1, z1, 1, 1, alpha, normalX, normalY, normalZ);
        vertex(pose, buffer, x2, y2, z2, 1, 0, alpha, normalX, normalY, normalZ);
        vertex(pose, buffer, x3, y3, z3, 0, 0, alpha, normalX, normalY, normalZ);
        vertex(pose, buffer, x4, y4, z4, 0, 1, alpha, normalX, normalY, normalZ);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z, float u, float v, int alpha, float normalX, float normalY, float normalZ) {
        buffer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(normalX, normalY, normalZ);
    }

    public static Identifier getTexture(ItemStack stack) {
        if (stack.getItem() instanceof TransferLabelItem transferLabelItem) return transferLabelItem.getTexture();
        return Identifier.fromNamespaceAndPath(TransferLabels.MODID, "textures/item/itemstack_insert_transfer_label.png");
    }

    @SubscribeEvent
    public void onTick(LevelTickEvent.Post event) {
        var delay = 2;
        if (event.getLevel() instanceof ClientLevel clientLevel) {
            CLIENT_UPDATE.forEach(leftClickBlock -> {
                if (clientLevel.getGameTime() > leftClickBlock.time() + delay && leftClickBlock.event().getLevel().equals(event.getLevel())) leftClickBlock.event().getLevel().destroyBlockProgress(leftClickBlock.event().getEntity().getId(), leftClickBlock.event().getPos(), -1);
            });
            CLIENT_UPDATE.removeIf(leftClickBlock -> clientLevel.getGameTime() > leftClickBlock.time() + delay && leftClickBlock.event().getLevel().equals(event.getLevel()));
        }
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        var validInteraction = event.getItemStack().getItem() instanceof TransferLabelItem || event.getItemStack().is(TransferLabels.LABEL_ACCESSOR);
        if (event.getLevel() instanceof ClientLevel clientLevel && validInteraction) {
            var nearbyLabels = LabelClientStorage.getNearbyLabels(clientLevel, event.getPos(), 20);
            var pair = RayTraceUtils.rayTraceVoxelShape(nearbyLabels, clientLevel, event.getEntity(), 0, event.getItemStack().is(TransferLabels.LABEL_ACCESSOR) ? null : event.getPos());
            if (pair != null) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }

    @SubscribeEvent
    public void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getItemStack().getItem() instanceof TransferLabelItem && event.getAction() == PlayerInteractEvent.LeftClickBlock.Action.START && event.getLevel() instanceof ClientLevel clientLevel) {
            var distance = event.getEntity().getAttribute(Attributes.BLOCK_INTERACTION_RANGE).getValue();
            var nearbyLabels = LabelClientStorage.getNearbyLabels(clientLevel, event.getPos(), (int) (distance * distance));
            var pair = RayTraceUtils.rayTraceVoxelShape(nearbyLabels, clientLevel, event.getEntity(), 0, event.getPos());
            if (pair != null) {
                event.setCanceled(true);
                CLIENT_UPDATE.add(new LabelInteractEvents.DelayedEvent(event, clientLevel.getGameTime()));
            }
        }
    }

    private record LabelRenderData(double x, double y, double z, Direction direction, Identifier texture, int alpha, boolean renderWithoutDepth) {
    }
}
