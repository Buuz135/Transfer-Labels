package com.buuz135.transfer_labels.item;

import com.buuz135.transfer_labels.TransferLabels;
import com.buuz135.transfer_labels.filter.ILabelFilter;
import com.buuz135.transfer_labels.packet.LabelSyncPacket;
import com.buuz135.transfer_labels.storage.LabelStorage;
import com.hrznstudio.titanium.api.filter.IFilter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

public abstract class TransferLabelItem extends Item {

    private final Mode mode;
    private ResourceLocation texture;

    public TransferLabelItem(String type, Properties properties, Mode mode) {
        super(properties);
        this.mode = mode;
        this.texture = ResourceLocation.fromNamespaceAndPath(TransferLabels.MODID, "textures/item/" + type + "_" + this.mode.name().toLowerCase(Locale.ROOT) + "_transfer_label.png");
        TransferLabels.TAB.getTabList().add(this);
    }

    public Mode getMode() {
        return mode;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel() instanceof ServerLevel serverLevel) {
            LabelStorage.addLabel(serverLevel, context.getClickedPos(), context.getClickedFace(), context.getItemInHand().copyWithCount(1));
            context.getItemInHand().shrink(1);
            TransferLabels.NETWORK.sendTo(new LabelSyncPacket(serverLevel.getLevel().dimension().location(), LabelStorage.getStorageFor(serverLevel).saveNearby(context.getPlayer().getOnPos(), 16, serverLevel.registryAccess()), context.getPlayer().getOnPos(), 16), (ServerPlayer) context.getPlayer());
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public abstract ILabelFilter<?> createFilter();

    public ResourceLocation getTexture() {
        return texture;
    }

    public static enum Mode{
        INSERT, EXTRACT;
    }
}
