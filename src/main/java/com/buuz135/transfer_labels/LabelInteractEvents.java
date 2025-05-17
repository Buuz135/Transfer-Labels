package com.buuz135.transfer_labels;

import com.buuz135.transfer_labels.client.RayTraceUtils;
import com.buuz135.transfer_labels.item.TransferLabelItem;
import com.buuz135.transfer_labels.packet.LabelSyncPacket;
import com.buuz135.transfer_labels.storage.LabelLocatorInstance;
import com.buuz135.transfer_labels.storage.LabelStorage;
import com.hrznstudio.titanium.network.locator.LocatorFactory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.List;

public class LabelInteractEvents {

    public static List<DelayedEvent> SERVER_UPDATE = new ArrayList<>();


    @SubscribeEvent
    public void onTick(LevelTickEvent.Pre event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            LabelStorage.getStorageFor(serverLevel).getLabelBlocks().forEach(labelBlock -> labelBlock.getLabels().forEach( (direction, label) -> label.work(serverLevel)));
            if (event.getLevel().getGameTime() % 20 == 0) {
                serverLevel.players().forEach(player -> {
                    TransferLabels.NETWORK.sendTo(new LabelSyncPacket(serverLevel.getLevel().dimension().location(), LabelStorage.getStorageFor(serverLevel).save(new CompoundTag(), serverLevel.registryAccess())), player);
                });
            }
        }
    }

    @SubscribeEvent
    public void onTick(LevelTickEvent.Post event) {
        var delay = 2;
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            SERVER_UPDATE.forEach(leftClickBlock -> {
                if (serverLevel.getGameTime() > (leftClickBlock.time + delay) && leftClickBlock.event.getLevel().equals(event.getLevel())) leftClickBlock.event.getLevel().destroyBlockProgress(leftClickBlock.event.getEntity().getId(), leftClickBlock.event.getPos(), -1);
            });
            SERVER_UPDATE.removeIf(leftClickBlock -> serverLevel.getGameTime() > (leftClickBlock.time + delay) && leftClickBlock.event.getLevel().equals(event.getLevel()));
        }
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            var nearbyLabels = LabelStorage.getNearbyLabels(serverLevel, event.getPos(), 20);
            var pair = RayTraceUtils.rayTraceVoxelShape(nearbyLabels, serverLevel, event.getEntity(),  0, event.getPos());
            if (pair != null) {
                if (event.getItemStack().getItem() instanceof TransferLabelItem){
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                        serverPlayer.openMenu(pair.getFirst().getLabels().get(pair.getSecond()), (buffer) -> LocatorFactory.writePacketBuffer(buffer, new LabelLocatorInstance(event.getPos(), pair.getSecond())));//TODO SYNC LABEL
                    }
                } else {

                }
            }
        }
    }


    @SubscribeEvent
    public void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getItemStack().getItem() instanceof TransferLabelItem && event.getAction() == PlayerInteractEvent.LeftClickBlock.Action.START){
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                var distance = event.getEntity().getAttribute(Attributes.BLOCK_INTERACTION_RANGE).getValue();
                var nearbyLabels = LabelStorage.getNearbyLabels(serverLevel, event.getPos(), (int) (distance*distance));
                var pair = RayTraceUtils.rayTraceVoxelShape(nearbyLabels, serverLevel, event.getEntity(),  0, event.getPos());
                if (pair != null) {
                    event.setCanceled(true);
                    LabelStorage.removeLabel(event.getEntity(), serverLevel, pair.getFirst().getPos(), pair.getSecond());
                    TransferLabels.NETWORK.sendTo(new LabelSyncPacket(serverLevel.getLevel().dimension().location(), LabelStorage.getStorageFor(serverLevel).save(new CompoundTag(), serverLevel.registryAccess())), (ServerPlayer) event.getEntity());
                    event.getEntity().playNotifySound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1, 1);
                    SERVER_UPDATE.add(new DelayedEvent(event, serverLevel.getGameTime()));
                }
            }
        }
    }


    public static record DelayedEvent(PlayerInteractEvent.LeftClickBlock event, long time) {}

}
