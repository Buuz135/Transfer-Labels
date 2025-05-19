package com.buuz135.transfer_labels.client;

import com.buuz135.transfer_labels.LabelInteractEvents;
import com.buuz135.transfer_labels.TransferLabels;
import com.buuz135.transfer_labels.item.TransferLabelItem;
import com.buuz135.transfer_labels.packet.LabelSyncPacket;
import com.buuz135.transfer_labels.storage.LabelLocatorInstance;
import com.buuz135.transfer_labels.storage.LabelStorage;
import com.buuz135.transfer_labels.storage.client.LabelClientStorage;
import com.hrznstudio.titanium.network.locator.LocatorFactory;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.List;

public class LabelClientEvents {

    @SubscribeEvent
    public void blockOverlayEvent(RenderHighlightEvent.Block event) {
        var distance = Minecraft.getInstance().player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).getValue();
        var isHoldingAccessor = Minecraft.getInstance().player.getItemInHand(InteractionHand.MAIN_HAND).is(TransferLabels.LABEL_ACCESSOR.get());
        var isHoldingLabel = Minecraft.getInstance().player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof TransferLabelItem || isHoldingAccessor;
        if (isHoldingLabel){
            var nearbyLabels = LabelClientStorage.getNearbyLabels(Minecraft.getInstance().level, event.getTarget().getBlockPos(), (int) (distance*distance));
            var pair = RayTraceUtils.rayTraceVoxelShape(nearbyLabels, Minecraft.getInstance().level, Minecraft.getInstance().player,  0, isHoldingAccessor ? null : event.getTarget().getBlockPos());
            if (pair != null) {
                event.setCanceled(true);

                double x = 0;
                double y = 0;
                double z = 0;

                // Calculate position relative to the camera
                Vec3 cameraPos = event.getCamera().getPosition();
                double renderX = x - cameraPos.x;
                double renderY = y - cameraPos.y;
                double renderZ = z - cameraPos.z;

                // Push the current matrix stack
                var poseStack = event.getPoseStack();
                poseStack.pushPose();
                poseStack.translate(renderX, renderY, renderZ);
                LevelRenderer.renderLineBox(event.getPoseStack(), Minecraft.getInstance().renderBuffers().outlineBufferSource().getBuffer(RenderType.lines()), pair.getFirst().collectShapes(pair.getSecond()), 0, 0, 0, 0.35F);


                // Restore the previous matrix stack
                poseStack.popPose();


            }
        }
    }

    public static RenderType createRenderType() {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionTexColorShader))
                .setTextureState(new RenderStateShard.TextureStateShard(ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/dirt.png"), false, false))
                .setTransparencyState(new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {

                }, () -> {

                })).createCompositeState(true);
        return RenderType.create("mycelial_render", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 262144, false, true, state);
    }

    @SubscribeEvent
    public void onRender(RenderLevelStageEvent event){
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;
        var currentPos = Minecraft.getInstance().player.blockPosition();
        var isHoldingAccessor = Minecraft.getInstance().player.getItemInHand(InteractionHand.MAIN_HAND).is(TransferLabels.LABEL_ACCESSOR.get());
        var isHoldingLabel = Minecraft.getInstance().player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof TransferLabelItem || isHoldingAccessor;
        var transparentAlpha = 100;
        var nearbyLabels = LabelClientStorage.getNearbyLabels(Minecraft.getInstance().level, currentPos, 20*20);
        var poseStack = event.getPoseStack();
        var combinedLight = LightTexture.FULL_BRIGHT;
        var combinedOverlay = OverlayTexture.NO_OVERLAY;
        if ((isHoldingLabel && Minecraft.getInstance().player.isCrouching()) || isHoldingAccessor){
            RenderSystem.disableDepthTest();
        } else {
            RenderSystem.enableDepthTest();
        }
        for (var label : nearbyLabels) {
            // Coordinates where the texture will be rendered
            double x = label.getPos().getX();
            double y = label.getPos().getY();
            double z = label.getPos().getZ();

            // Calculate position relative to the camera
            Vec3 cameraPos = event.getCamera().getPosition();
            double renderX = x - cameraPos.x;
            double renderY = y - cameraPos.y;
            double renderZ = z - cameraPos.z;

            // Push the current matrix stack
            poseStack.pushPose();
            poseStack.translate(renderX, renderY, renderZ);

            // Bind the texture you want to render

            RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);

            RenderSystem.enableBlend();

            //RenderSystem.defaultBlendFunc();
            ////ENABLE TO SEE THROUGH

            // Obtain the Tesselator and BufferBuilder


            // Define the size of the texture quad
            float size = 1.0f;

            // Start drawing the quad
            //buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            if (label.getLabels().containsKey(Direction.NORTH)) {
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
                RenderSystem.setShaderTexture(0, getTexture(label.getLabels().get(Direction.NORTH).getLabel()));
                buffer.addVertex(poseStack.last().pose(), 0, 0, -0.001f).setUv(1, 1).setLight(0xF000F0).setColor(255, 255, 255, isHoldingLabel ? 255 : transparentAlpha);
                buffer.addVertex(poseStack.last().pose(), 0, size, -0.001f).setUv(1, 0).setLight(0xF000F0).setColor(255, 255, 255, isHoldingLabel ? 255 : transparentAlpha);
                buffer.addVertex(poseStack.last().pose(), size, size, -0.001f).setUv(0, 0).setLight(0xF000F0).setColor(255, 255, 255, isHoldingLabel ? 255 : transparentAlpha);
                buffer.addVertex(poseStack.last().pose(), size, 0, -0.001f).setUv(0, 1).setLight(0xF000F0).setColor(255, 255, 255, isHoldingLabel ? 255 : transparentAlpha);
                var mesh = buffer.build();
                if (mesh != null) {
                    BufferUploader.drawWithShader(mesh);
                }
                tesselator.clear();
            }

            if (label.getLabels().containsKey(Direction.SOUTH)) {
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
                RenderSystem.setShaderTexture(0, getTexture(label.getLabels().get(Direction.SOUTH).getLabel()));
                buffer.addVertex(poseStack.last().pose(), size, 0, 1.001f).setUv(1, 1).setLight(0xF000F0).setColor(255, 255, 255, isHoldingLabel ? 255 : transparentAlpha);
                buffer.addVertex(poseStack.last().pose(), size, size, 1.001f).setUv(1, 0).setLight(0xF000F0).setColor(255, 255, 255, isHoldingLabel ? 255 : transparentAlpha);
                buffer.addVertex(poseStack.last().pose(), 0, size, 1.001f).setUv(0, 0).setLight(0xF000F0).setColor(255, 255, 255, isHoldingLabel ? 255 : transparentAlpha);
                buffer.addVertex(poseStack.last().pose(), 0, 0, 1.001f).setUv(0, 1).setLight(0xF000F0).setColor(255, 255, 255, isHoldingLabel ? 255 : transparentAlpha);
                var mesh = buffer.build();
                if (mesh != null) {
                    BufferUploader.drawWithShader(mesh);
                }
                tesselator.clear();
            }

            if (label.getLabels().containsKey(Direction.WEST)) {
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
                RenderSystem.setShaderTexture(0, getTexture(label.getLabels().get(Direction.WEST).getLabel()));
                buffer.addVertex(poseStack.last().pose(), -0.001f, 0, size).setUv(1, 1).setLight(0xF000F0).setColor(255, 255, 255, isHoldingLabel ? 255 : transparentAlpha);
                buffer.addVertex(poseStack.last().pose(), -0.001f, size, size).setUv(1, 0).setLight(0xF000F0).setColor(255, 255, 255, isHoldingLabel ? 255 : transparentAlpha);
                buffer.addVertex(poseStack.last().pose(), -0.001f, size, 0).setUv(0, 0).setLight(0xF000F0).setColor(255, 255, 255, isHoldingLabel ? 255 : transparentAlpha);
                buffer.addVertex(poseStack.last().pose(), -0.001f, 0, 0).setUv(0, 1).setLight(0xF000F0).setColor(255, 255, 255, isHoldingLabel ? 255 : transparentAlpha);
                var mesh = buffer.build();
                if (mesh != null) {
                    BufferUploader.drawWithShader(mesh);
                }
                tesselator.clear();
            }

            if (label.getLabels().containsKey(Direction.EAST)) {
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
                RenderSystem.setShaderTexture(0, getTexture(label.getLabels().get(Direction.EAST).getLabel()));
                buffer.addVertex(poseStack.last().pose(), 1.001f, 0, 0).setUv(1, 1).setLight(0xF000F0).setColor(255, 255, 255, isHoldingLabel ? 255 : transparentAlpha);
                buffer.addVertex(poseStack.last().pose(), 1.001f, size, 0).setUv(1, 0).setLight(0xF000F0).setColor(255, 255, 255, isHoldingLabel ? 255 : transparentAlpha);
                buffer.addVertex(poseStack.last().pose(), 1.001f, size, size).setUv(0, 0).setLight(0xF000F0).setColor(255, 255, 255, isHoldingLabel ? 255 : transparentAlpha);
                buffer.addVertex(poseStack.last().pose(), 1.001f, 0, size).setUv(0, 1).setLight(0xF000F0).setColor(255, 255, 255, isHoldingLabel ? 255 : transparentAlpha);
                var mesh = buffer.build();
                if (mesh != null) {
                    BufferUploader.drawWithShader(mesh);
                }
                tesselator.clear();
            }

            if (label.getLabels().containsKey(Direction.UP)) {
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
                RenderSystem.setShaderTexture(0, getTexture(label.getLabels().get(Direction.UP).getLabel()));
                buffer.addVertex(poseStack.last().pose(), 0, 1.001f, 0).setUv(0, 0).setLight(0xF000F0).setColor(255, 255, 255, isHoldingLabel ? 255 : transparentAlpha);
                buffer.addVertex(poseStack.last().pose(), 0, 1.001f, size).setUv(0, 1).setLight(0xF000F0).setColor(255, 255, 255, isHoldingLabel ? 255 : transparentAlpha);
                buffer.addVertex(poseStack.last().pose(), size, 1.001f, size).setUv(1, 1).setLight(0xF000F0).setColor(255, 255, 255, isHoldingLabel ? 255 : transparentAlpha);
                buffer.addVertex(poseStack.last().pose(), size, 1.001f, 0).setUv(1, 0).setLight(0xF000F0).setColor(255, 255, 255, isHoldingLabel ? 255 : transparentAlpha);
                var mesh = buffer.build();
                if (mesh != null) {
                    BufferUploader.drawWithShader(mesh);
                }
                tesselator.clear();
            }

            if (label.getLabels().containsKey(Direction.DOWN)) {
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
                RenderSystem.setShaderTexture(0, getTexture(label.getLabels().get(Direction.DOWN).getLabel()));
                buffer.addVertex(poseStack.last().pose(), 0, -0.001f, size).setUv(0, 0).setLight(0xF000F0).setColor(255, 255, 255, isHoldingLabel ? 255 : transparentAlpha);
                buffer.addVertex(poseStack.last().pose(), 0, -0.001f, 0).setUv(0, 1).setLight(0xF000F0).setColor(255, 255, 255, isHoldingLabel ? 255 : transparentAlpha);
                buffer.addVertex(poseStack.last().pose(), size, -0.001f, 0).setUv(1, 1).setLight(0xF000F0).setColor(255, 255, 255, isHoldingLabel ? 255 : transparentAlpha);
                buffer.addVertex(poseStack.last().pose(), size, -0.001f, size).setUv(1, 0).setLight(0xF000F0).setColor(255, 255, 255, isHoldingLabel ? 255 : transparentAlpha);
                var mesh = buffer.build();
                if (mesh != null) {
                    BufferUploader.drawWithShader(mesh);
                }
                tesselator.clear();
            }


            // Restore the previous matrix stack
            poseStack.popPose();
        }
        RenderSystem.enableDepthTest();
    }

    public static ResourceLocation getTexture(ItemStack stack) {
        if (stack.getItem() instanceof TransferLabelItem) {
            return ((TransferLabelItem) stack.getItem()).getTexture();
        }
        return ResourceLocation.fromNamespaceAndPath(TransferLabels.MODID, "textures/item/itemstack_insert_transfer_label.png");
    }

    public static List<LabelInteractEvents.DelayedEvent> CLIENT_UPDATE = new ArrayList<>();

    @SubscribeEvent
    public void onTick(LevelTickEvent.Post event) {
        var delay = 2;
        if (event.getLevel() instanceof ClientLevel clientLevel) {
            CLIENT_UPDATE.forEach(leftClickBlock -> {
                if (clientLevel.getGameTime() > (leftClickBlock.time() + delay) && leftClickBlock.event().getLevel().equals(event.getLevel())) leftClickBlock.event().getLevel().destroyBlockProgress(leftClickBlock.event().getEntity().getId(), leftClickBlock.event().getPos(), -1);
            });
            CLIENT_UPDATE.removeIf(leftClickBlock -> clientLevel.getGameTime() > (leftClickBlock.time() + delay) && leftClickBlock.event().getLevel().equals(event.getLevel()));
        }
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        var validInteraction =  event.getItemStack().getItem() instanceof TransferLabelItem || event.getItemStack().is(TransferLabels.LABEL_ACCESSOR);
        if (event.getLevel() instanceof ClientLevel clientLevel && validInteraction) {
            var nearbyLabels = LabelClientStorage.getNearbyLabels(clientLevel, event.getPos(), 20);
            var pair = RayTraceUtils.rayTraceVoxelShape(nearbyLabels, clientLevel, event.getEntity(),  0, event.getItemStack().is(TransferLabels.LABEL_ACCESSOR) ? null : event.getPos());
            if (pair != null) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }


    @SubscribeEvent
    public void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getItemStack().getItem() instanceof TransferLabelItem && event.getAction() == PlayerInteractEvent.LeftClickBlock.Action.START){
             if (event.getLevel() instanceof ClientLevel clientLevel) {
                var distance = event.getEntity().getAttribute(Attributes.BLOCK_INTERACTION_RANGE).getValue();
                var nearbyLabels = LabelClientStorage.getNearbyLabels(clientLevel, event.getPos(), (int) (distance*distance));
                var pair = RayTraceUtils.rayTraceVoxelShape(nearbyLabels, clientLevel, event.getEntity(),  0, event.getPos());
                if (pair != null) {
                    event.setCanceled(true);
                    CLIENT_UPDATE.add(new LabelInteractEvents.DelayedEvent(event, clientLevel.getGameTime()));
                }
            }
        }
    }

}
