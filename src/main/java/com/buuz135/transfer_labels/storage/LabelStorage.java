package com.buuz135.transfer_labels.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class LabelStorage extends SavedData {

    public static final String DATA_NAME = "TRANSFER_LABELS_LABEL_STORAGE";
    public static final SavedDataType<LabelStorage> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("transfer_labels", "labels"),
            LabelStorage::new,
            level -> CompoundTag.CODEC.xmap(
                    tag -> createFromCompound(level, tag, level.registryAccess()),
                    storage -> storage.save(new CompoundTag(), level.registryAccess())));

    public static LabelStorage getStorageFor(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
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

    public CompoundTag saveNearby(BlockPos pos, int distance, HolderLookup.Provider provider){
        CompoundTag compoundTag = new CompoundTag();
        getLabelBlocks().stream().filter(labelBlock -> labelBlock.getPos().distSqr(pos) <= distance).forEach(labelBlock -> {
            compoundTag.put(labelBlock.getPos().asLong() + "", labelBlock.serializeNBT(provider));
        });
        return compoundTag;
    }

    public void loadNearby(BlockPos anchor, int distance, CompoundTag compoundTag, HolderLookup.Provider provider){
        var nearby = getLabelBlocks().stream().filter(labelBlock -> labelBlock.getPos().distSqr(anchor) <= distance).toList();
        List<BlockPos> visitedPositions = new ArrayList<>();
        compoundTag.keySet().forEach(s -> {
           var pos = BlockPos.of(Long.parseLong(s));
            visitedPositions.add(pos);
           if (labelBlocks.containsKey(pos)) {
               labelBlocks.get(pos).deserializeNBT(provider, compoundTag.getCompoundOrEmpty(s));
           } else {
               var label = new LabelBlock(pos, level);
               label.deserializeNBT(provider, compoundTag.getCompoundOrEmpty(s));
               labelBlocks.put(pos, label);
           }
        });
        nearby.forEach(labelBlock -> {
            if (!visitedPositions.contains(labelBlock.getPos())) {
                labelBlocks.remove(labelBlock.getPos());
            }
        });
    }

    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        CompoundTag labels = new CompoundTag();
        labelBlocks.forEach((pos, labelBlock) -> labels.put(pos.asLong() + "", labelBlock.serializeNBT(provider)));
        compoundTag.put("Labels", labels);
        return compoundTag;
    }

    public void load(CompoundTag compoundTag, HolderLookup.Provider provider) {
        //labelBlocks = new HashMap<>();
        CompoundTag labels = compoundTag.getCompoundOrEmpty("Labels");
        List<BlockPos> visitedPositions = new ArrayList<>();
        labels.keySet().forEach(s -> {
            var pos = BlockPos.of(Long.parseLong(s));
            visitedPositions.add(pos);
            if (this.labelBlocks.containsKey(pos)){
                this.labelBlocks.get(pos).deserializeNBT(provider, labels.getCompoundOrEmpty(s));
            } else {
                var label = new LabelBlock(pos, level);
                label.deserializeNBT(provider, labels.getCompoundOrEmpty(s));
                labelBlocks.put(pos, label);
            }
        });
        this.labelBlocks.keySet().removeIf(pos -> !visitedPositions.contains(pos));
    }

    public Level getLevel() {
        return level;
    }
}
