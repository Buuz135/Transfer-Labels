package com.buuz135.transfer_labels.packet;

import com.buuz135.transfer_labels.storage.LabelStorage;
import com.buuz135.transfer_labels.storage.client.LabelClientStorage;
import com.hrznstudio.titanium.network.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;

public class LabelSyncPacket extends Message {

    public CompoundTag labels;
    public BlockPos anchor;
    public int distance;

    public LabelSyncPacket(CompoundTag compoundTag, BlockPos anchor, int distance) {
        this.labels = compoundTag;
        this.anchor = anchor;
        this.distance = distance;
    }

    public LabelSyncPacket() {
    }

    @Override
    public void handleClientMessage(IPayloadContext context) {
        context.enqueueWork(() -> {
            LabelClientStorage.getStorage(Minecraft.getInstance().level).loadNearby(anchor, distance, this.labels, Minecraft.getInstance().level.registryAccess());
        });
    }

    @Override
    protected void handleServerMessage(IPayloadContext context) {
    }
}
