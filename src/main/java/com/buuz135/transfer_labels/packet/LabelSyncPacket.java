package com.buuz135.transfer_labels.packet;

import com.buuz135.transfer_labels.storage.LabelStorage;
import com.buuz135.transfer_labels.storage.client.LabelClientStorage;
import com.hrznstudio.titanium.network.CompoundSerializableDataHandler;
import com.hrznstudio.titanium.network.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class LabelSyncPacket extends Message {

    static {
        CompoundSerializableDataHandler.map(DimensionType.class,
                registryFriendlyByteBuf -> registryFriendlyByteBuf.readJsonWithCodec(DimensionType.DIRECT_CODEC),
                (buf, type) -> buf.writeJsonWithCodec(DimensionType.DIRECT_CODEC, type));
    }

    public ResourceLocation location;
    public CompoundTag labels;
    public BlockPos anchor;
    public int distance;

    public LabelSyncPacket(ResourceLocation location, CompoundTag compoundTag, BlockPos anchor, int distance) {
        this.location = location;
        this.labels = compoundTag;
        this.anchor = anchor;
        this.distance = distance;
    }

    public LabelSyncPacket() {
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected void handleMessage(IPayloadContext context) {
        context.enqueueWork(() -> {
            LabelClientStorage.LABELS.computeIfAbsent(location, dimensionType -> new LabelStorage(Minecraft.getInstance().level)).loadNearby(anchor, distance, this.labels, Minecraft.getInstance().level.registryAccess()); //TODO IMPROVE LEVEL
        });
    }
}
