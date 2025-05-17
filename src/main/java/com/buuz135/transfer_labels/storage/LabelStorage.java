package com.buuz135.transfer_labels.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class LabelStorage extends SavedData {

    public static final String DATA_NAME = "TRANSFER_LABELS_LABEL_STORAGE";

    public static LabelStorage getStorageFor(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new Factory<LabelStorage>(
                () -> new LabelStorage(level),
                (compoundTag, provider) -> createFromCompound(level, compoundTag, provider)
        ), DATA_NAME);
    }

    private static LabelStorage createFromCompound(Level level, CompoundTag compoundTag, HolderLookup.Provider provider) {
        LabelStorage storage = new LabelStorage(level);
        storage.load(compoundTag, provider);
        return storage;
    }

    public static List<LabelBlock> getNearbyLabels(ServerLevel level, BlockPos pos, int distance) {
        return getStorageFor(level).getLabelBlocks().stream().filter(labelBlock -> labelBlock.getPos().distSqr(pos) <= distance).collect(Collectors.toList());
    }

    public static void addLabel(ServerLevel level, BlockPos blockPos, Direction direction, ItemStack label) {
        getStorageFor(level).labelBlocks.computeIfAbsent(blockPos, labelBlock -> new LabelBlock(blockPos, level)).setLabel(direction, label);
        getStorageFor(level).setDirty();
    }

    public static void removeLabel(Player player, ServerLevel level, BlockPos blockPos, Direction direction) {
        var labelBlock = getStorageFor(level).labelBlocks.computeIfAbsent(blockPos, pos -> new LabelBlock(blockPos, level));
        labelBlock.remove(player, direction);
        if (labelBlock.getLabels().isEmpty()) {
            getStorageFor(level).labelBlocks.remove(blockPos);
        }
        getStorageFor(level).setDirty();
    }

    public List<LabelBlock> getLabelBlocks() {
        return labelBlocks.values().stream().collect(Collectors.toList());
    }

    public HashMap<BlockPos, LabelBlock> getLabelBlocksMap() {
        return labelBlocks;
    }

    private HashMap<BlockPos, LabelBlock> labelBlocks;
    private final Level level;

    public LabelStorage(Level level) {
        this.level = level;
        this.labelBlocks = new HashMap<>();
    }

    public void markDirty(){
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        CompoundTag labels = new CompoundTag();
        labelBlocks.forEach((pos, labelBlock) -> labels.put(pos.asLong() + "", labelBlock.serializeNBT(provider)));
        compoundTag.put("Labels", labels);
        return compoundTag;
    }

    public void load(CompoundTag compoundTag, HolderLookup.Provider provider) {
        //labelBlocks = new HashMap<>();
        CompoundTag labels = compoundTag.getCompound("Labels");
        List<BlockPos> visitedPositions = new java.util.ArrayList<>();
        labels.getAllKeys().forEach(s -> {
            var pos = BlockPos.of(Long.parseLong(s));
            visitedPositions.add(pos);
            if (this.labelBlocks.containsKey(pos)){
                this.labelBlocks.get(pos).deserializeNBT(provider, labels.getCompound(s));
            } else {
                var label = new LabelBlock(pos, level);
                label.deserializeNBT(provider, labels.getCompound(s));
                labelBlocks.put(pos, label);
            }
        });
        this.labelBlocks.keySet().removeIf(pos -> !visitedPositions.contains(pos));
    }
}