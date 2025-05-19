package com.buuz135.transfer_labels.packet;

import com.buuz135.transfer_labels.storage.LabelBlock;
import com.buuz135.transfer_labels.storage.LabelStorage;
import com.buuz135.transfer_labels.storage.client.LabelClientStorage;
import com.hrznstudio.titanium.network.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SingleLabelSyncPacket extends Message {


    public ResourceLocation location;
    public BlockPos pos;
    public CompoundTag label;

    public SingleLabelSyncPacket(ResourceLocation location, BlockPos pos, CompoundTag compoundTag) {
        this.location = location;
        this.pos = pos;
        this.label = compoundTag;
    }

    public SingleLabelSyncPacket() {
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected void handleMessage(IPayloadContext context) {
        context.enqueueWork(() -> {
            LabelClientStorage.LABELS.computeIfAbsent(location, dimensionType -> new LabelStorage(Minecraft.getInstance().level)) //TODO IMPROVE LEVEL
                    .getLabelBlocksMap().computeIfAbsent(pos, blockPos -> new LabelBlock(blockPos, Minecraft.getInstance().level)).deserializeNBT(Minecraft.getInstance().level.registryAccess(), label);
        });
    }
}
