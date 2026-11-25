package com.buuz135.transfer_labels.packet;

import com.buuz135.transfer_labels.storage.LabelBlock;
import com.buuz135.transfer_labels.storage.LabelStorage;
import com.buuz135.transfer_labels.storage.client.LabelClientStorage;
import com.hrznstudio.titanium.network.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SingleLabelSyncPacket extends Message {


    public BlockPos pos;
    public CompoundTag label;

    public SingleLabelSyncPacket(BlockPos pos, CompoundTag compoundTag) {
        this.pos = pos;
        this.label = compoundTag;
    }

    public SingleLabelSyncPacket() {
    }

    @Override
    public void handleClientMessage(IPayloadContext context) {
        context.enqueueWork(() -> {
            LabelClientStorage.getStorage(Minecraft.getInstance().level)
                    .getLabelBlocksMap().computeIfAbsent(pos, blockPos -> new LabelBlock(blockPos, Minecraft.getInstance().level)).deserializeNBT(Minecraft.getInstance().level.registryAccess(), label);
        });
    }

    @Override
    protected void handleServerMessage(IPayloadContext context) {
    }
}
