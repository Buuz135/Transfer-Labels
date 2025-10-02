package com.buuz135.transfer_labels.storage;

import com.buuz135.transfer_labels.TransferLabels;
import com.buuz135.transfer_labels.client.LabelShapes;
import com.buuz135.transfer_labels.packet.SingleLabelSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class LabelBlock implements INBTSerializable<CompoundTag> {

    private final BlockPos pos;
    private final Level level;
    private HashMap<Direction, LabelInstance> labels;

    public LabelBlock(BlockPos pos, Level level) {
        this.pos = pos;
        this.level = level;
        this.labels = new HashMap<>();
    }

    public void setLabel(Direction direction, ItemStack label) {
        this.labels.put(direction, new LabelInstance(label, level, pos, direction, this));
    }

    public BlockPos getPos() {
        return pos;
    }

    public HashMap<Direction, LabelInstance> getLabels() {
        return labels;
    }

    public AABB collectShapes(Direction direction2) {
        if (direction2 == Direction.NORTH && this.labels.containsKey(Direction.NORTH)) return LabelShapes.NORTH.move(this.getPos());
        if (direction2 == Direction.SOUTH && this.labels.containsKey(Direction.SOUTH)) return LabelShapes.SOUTH.move(this.getPos());
        if (direction2 == Direction.EAST && this.labels.containsKey(Direction.EAST)) return LabelShapes.EAST.move(this.getPos());
        if (direction2 == Direction.WEST && this.labels.containsKey(Direction.WEST)) return LabelShapes.WEST.move(this.getPos());
        if (direction2 == Direction.UP && this.labels.containsKey(Direction.UP)) return LabelShapes.UP.move(this.getPos());
        if (direction2 == Direction.DOWN && this.labels.containsKey(Direction.DOWN)) return LabelShapes.DOWN.move(this.getPos());
        return null;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var compoundTag = new CompoundTag();
        this.labels.forEach((direction, labelInstance) -> {
            var labelInstanceCompoundTag = new CompoundTag();
            labelInstanceCompoundTag.put("Stack", labelInstance.getLabel().saveOptional(provider));
            labelInstanceCompoundTag.put("Extra", labelInstance.serializeNBT(provider));
            compoundTag.put(direction.name(), labelInstanceCompoundTag);
        });
        return compoundTag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        //this.labels = new HashMap<>();
        List<Direction> visitedDirections = new ArrayList<>();
        compoundTag.getAllKeys().forEach(s -> {
            var labelInstanceCompoundTag = compoundTag.getCompound(s);
            var direction = Direction.valueOf(s);
            visitedDirections.add(direction);
            var instance = this.labels.computeIfAbsent(direction, direction1 -> new LabelInstance(ItemStack.parseOptional(provider, labelInstanceCompoundTag.getCompound("Stack")), level, pos, direction, this));
            instance.deserializeNBT(provider, labelInstanceCompoundTag.getCompound("Extra"));
            this.labels.put(direction, instance);
        });
        this.labels.keySet().removeIf(direction -> !visitedDirections.contains(direction));
    }

    public void updateToNearby(Player player){
        if (player.level() instanceof ServerLevel serverLevel){
            TransferLabels.NETWORK.sendToNearby(level, pos, 32, new SingleLabelSyncPacket(pos, this.serializeNBT(level.registryAccess())));
            LabelStorage.getStorageFor(serverLevel).markDirty();
        }
    }

    public void remove(Player player, Direction direction){
        var instance = this.getLabels().remove(direction);
        if (instance != null && player != null){
            var original = instance.getLabel().copy();
            ItemHandlerHelper.giveItemToPlayer(player, original);
            var amount = instance.getAmountFilter();
            var speed = instance.getSpeedFilter();
            if (amount.getStackInSlot(0).getCount() > 0){
                ItemHandlerHelper.giveItemToPlayer(player, amount.getStackInSlot(0).copy());
            }
            if (speed.getStackInSlot(0).getCount() > 0){
                ItemHandlerHelper.giveItemToPlayer(player, speed.getStackInSlot(0).copy());
            }
        }
    }
}
