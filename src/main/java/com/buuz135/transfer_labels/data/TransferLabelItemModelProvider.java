package com.buuz135.transfer_labels.data;

import com.buuz135.transfer_labels.TransferLabels;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.Holder;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.Block;

import java.util.stream.Stream;

public class TransferLabelItemModelProvider extends ModelProvider {

    public TransferLabelItemModelProvider(DataGenerator generator, String modId) {
        super(generator.getPackOutput(), modId);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(TransferLabels.ITEMSTACK_INSERT_LABEL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TransferLabels.ITEMSTACK_EXTRACT_LABEL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TransferLabels.FLUIDSTACK_INSERT_LABEL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TransferLabels.FLUIDSTACK_EXTRACT_LABEL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TransferLabels.LABEL_ACCESSOR.get(), ModelTemplates.FLAT_ITEM);
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.empty();
    }
}
