package com.buuz135.transfer_labels.data;

import com.buuz135.transfer_labels.TransferLabels;
import com.hrznstudio.titanium.block.BasicBlock;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapelessRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TransferLabelRecipesProvider extends RecipeProvider {


    public TransferLabelRecipesProvider(DataGenerator generator, CompletableFuture<HolderLookup.Provider> prov) {
        super(generator.getPackOutput(), prov);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        TitaniumShapedRecipeBuilder.shapedRecipe(TransferLabels.ITEMSTACK_EXTRACT_LABEL.get(),2)
                .pattern("RCH")
                .define('R', Items.REDSTONE)
                .define('H', Items.HOPPER)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .save(output);
        TitaniumShapedRecipeBuilder.shapedRecipe(TransferLabels.ITEMSTACK_INSERT_LABEL.get(),2)
                .pattern("HCR")
                .define('R', Items.REDSTONE)
                .define('H', Items.HOPPER)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .save(output);
        TitaniumShapelessRecipeBuilder.shapelessRecipe(TransferLabels.ITEMSTACK_EXTRACT_LABEL.get(),1)
                .requires(TransferLabels.ITEMSTACK_INSERT_LABEL.get(),1)
                .save(output, "transfer_labels:itemstack_extract_label_to_itemstack_insert_label");
        TitaniumShapelessRecipeBuilder.shapelessRecipe(TransferLabels.ITEMSTACK_INSERT_LABEL.get(),1)
                .requires(TransferLabels.ITEMSTACK_EXTRACT_LABEL.get(),1)
                .save(output, "transfer_labels:itemstack_insert_label_to_itemstack_extract_label");
        TitaniumShapedRecipeBuilder.shapedRecipe(TransferLabels.FLUIDSTACK_EXTRACT_LABEL.get(),2)
                .pattern("RCH")
                .define('R', Items.REDSTONE)
                .define('H', Items.HOPPER)
                .define('C', Items.BUCKET)
                .save(output);
        TitaniumShapedRecipeBuilder.shapedRecipe(TransferLabels.FLUIDSTACK_INSERT_LABEL.get(),2)
                .pattern("HCR")
                .define('R', Items.REDSTONE)
                .define('H', Items.HOPPER)
                .define('C', Items.BUCKET)
                .save(output);
        TitaniumShapelessRecipeBuilder.shapelessRecipe(TransferLabels.FLUIDSTACK_EXTRACT_LABEL.get(),1)
                .requires(TransferLabels.FLUIDSTACK_INSERT_LABEL.get(),1)
                .save(output, "transfer_labels:fluidstack_extract_label_to_fluidstack_insert_label");
        TitaniumShapelessRecipeBuilder.shapelessRecipe(TransferLabels.FLUIDSTACK_INSERT_LABEL.get(),1)
                .requires(TransferLabels.FLUIDSTACK_EXTRACT_LABEL.get(),1)
                .save(output, "transfer_labels:fluidstack_insert_label_to_fluidstack_extract_label");
        TitaniumShapedRecipeBuilder.shapedRecipe(TransferLabels.LABEL_ACCESSOR.get(),1)
                .pattern("RCR").pattern(" R ").pattern(" R ")
                .define('R', Items.REDSTONE)
                .define('C', Ingredient.of(TransferLabels.FLUIDSTACK_EXTRACT_LABEL.get(), TransferLabels.ITEMSTACK_EXTRACT_LABEL.get(), TransferLabels.ITEMSTACK_INSERT_LABEL.get(), TransferLabels.FLUIDSTACK_INSERT_LABEL.get()))
                .save(output);
    }
}
