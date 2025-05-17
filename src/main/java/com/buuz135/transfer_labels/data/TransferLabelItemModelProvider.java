package com.buuz135.transfer_labels.data;

import com.buuz135.transfer_labels.TransferLabels;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Arrays;
import java.util.List;

public class TransferLabelItemModelProvider extends ItemModelProvider {


    public TransferLabelItemModelProvider(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        generateItem(TransferLabels.ITEMSTACK_INSERT_LABEL.get());
        generateItem(TransferLabels.ITEMSTACK_EXTRACT_LABEL.get());
        generateItem(TransferLabels.FLUIDSTACK_INSERT_LABEL.get());
        generateItem(TransferLabels.FLUIDSTACK_EXTRACT_LABEL.get());
    }

    private void generateItem(Item item) {
        getBuilder(BuiltInRegistries.ITEM.getKey(item).getPath())
                    .parent(new ModelFile.UncheckedModelFile("item/generated"))
                    .texture("layer0", modLoc("item/" + BuiltInRegistries.ITEM.getKey(item).getPath()));
    }
}
