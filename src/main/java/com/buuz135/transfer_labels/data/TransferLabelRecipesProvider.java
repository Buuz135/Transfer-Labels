package com.buuz135.transfer_labels.data;

import com.buuz135.transfer_labels.TransferLabels;
import com.hrznstudio.titanium.block.BasicBlock;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.Lazy;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TransferLabelRecipesProvider extends RecipeProvider {


    public TransferLabelRecipesProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        TitaniumShapedRecipeBuilder.shapedRecipe(TransferLabels.ITEMSTACK_EXTRACT_LABEL.get(),2)
                .pattern(" R ")
                .pattern(" C ")
                .pattern(" H ")
                .define('R', Items.REDSTONE)
                .define('H', Items.HOPPER)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .save(output);
        TitaniumShapedRecipeBuilder.shapedRecipe(TransferLabels.ITEMSTACK_INSERT_LABEL.get(),2)
                .pattern(" H ")
                .pattern(" C ")
                .pattern(" R ")
                .define('R', Items.REDSTONE)
                .define('H', Items.HOPPER)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .save(output);
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, TransferLabels.ITEMSTACK_EXTRACT_LABEL.get())
                .requires(TransferLabels.ITEMSTACK_INSERT_LABEL.get())
                .unlockedBy("has_item", has(TransferLabels.ITEMSTACK_INSERT_LABEL.get()))
                .save(output, "transfer_labels:itemstack_extract_label_to_itemstack_insert_label");
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, TransferLabels.ITEMSTACK_INSERT_LABEL.get())
                .requires(TransferLabels.ITEMSTACK_EXTRACT_LABEL.get())
                .unlockedBy("has_item", has(TransferLabels.ITEMSTACK_EXTRACT_LABEL.get()))
                .save(output, "transfer_labels:itemstack_insert_label_to_itemstack_extract_label");
        TitaniumShapedRecipeBuilder.shapedRecipe(TransferLabels.FLUIDSTACK_EXTRACT_LABEL.get(),2)
                .pattern(" R ")
                .pattern(" C ")
                .pattern(" H ")
                .define('R', Items.REDSTONE)
                .define('H', Items.HOPPER)
                .define('C', Items.BUCKET)
                .save(output);
        TitaniumShapedRecipeBuilder.shapedRecipe(TransferLabels.FLUIDSTACK_INSERT_LABEL.get(),2)
                .pattern(" H ")
                .pattern(" C ")
                .pattern(" R ")
                .define('R', Items.REDSTONE)
                .define('H', Items.HOPPER)
                .define('C', Items.BUCKET)
                .save(output);
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, TransferLabels.FLUIDSTACK_EXTRACT_LABEL.get())
                .requires(TransferLabels.FLUIDSTACK_INSERT_LABEL.get())
                .unlockedBy("has_item", has(TransferLabels.FLUIDSTACK_INSERT_LABEL.get()))
                .save(output, "transfer_labels:fluidstack_extract_label_to_fluidstack_insert_label");
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, TransferLabels.FLUIDSTACK_INSERT_LABEL.get())
                .requires(TransferLabels.FLUIDSTACK_EXTRACT_LABEL.get())
                .unlockedBy("has_item", has(TransferLabels.FLUIDSTACK_EXTRACT_LABEL.get()))
                .save(output, "transfer_labels:fluidstack_insert_label_to_fluidstack_extract_label");
        TitaniumShapedRecipeBuilder.shapedRecipe(TransferLabels.LABEL_ACCESSOR.get(),1)
                .pattern("RCR").pattern(" R ").pattern(" R ")
                .define('R', Tags.Items.INGOTS_COPPER)
                .define('C', Ingredient.of(TransferLabels.FLUIDSTACK_EXTRACT_LABEL.get(), TransferLabels.ITEMSTACK_EXTRACT_LABEL.get(), TransferLabels.ITEMSTACK_INSERT_LABEL.get(), TransferLabels.FLUIDSTACK_INSERT_LABEL.get()))
                .save(output);
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ResourceManager resources) {
            super(output, registries.thenApply(provider -> withItemTags(provider, resources)));
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private static HolderLookup.Provider withItemTags(HolderLookup.Provider provider, ResourceManager resources) {
            TagLoader.ElementLookup<Holder<Item>> itemElements = (TagLoader.ElementLookup) TagLoader.ElementLookup.fromFrozenRegistry(BuiltInRegistries.ITEM);
            var loadedTags = new HashMap<>(TagLoader.loadTagsForRegistry(resources, Registries.ITEM, itemElements));
            Registry.PendingTags<Item> pendingTags = BuiltInRegistries.ITEM.prepareTagReload(new TagLoader.LoadResult<>(Registries.ITEM, loadedTags));
            pendingTags.apply();
            return provider;
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new TransferLabelRecipesProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Transfer Labels Recipes";
        }
    }
}
