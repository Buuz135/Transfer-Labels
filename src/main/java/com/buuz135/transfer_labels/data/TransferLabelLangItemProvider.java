package com.buuz135.transfer_labels.data;

import com.buuz135.transfer_labels.TransferLabels;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.apache.commons.lang3.text.WordUtils;

import java.util.List;

public class TransferLabelLangItemProvider extends LanguageProvider {

    public TransferLabelLangItemProvider(DataGenerator gen, String modid, String locale) {
        super(gen.getPackOutput(), modid, locale);
    }

    @Override
    protected void addTranslations() {
        this.add("itemGroup.transfer_labels", "Transfer Labels");
        formatItem(TransferLabels.ITEMSTACK_INSERT_LABEL.get());
        formatItem(TransferLabels.ITEMSTACK_EXTRACT_LABEL.get());
        formatItem(TransferLabels.FLUIDSTACK_INSERT_LABEL.get());
        formatItem(TransferLabels.FLUIDSTACK_EXTRACT_LABEL.get());
        this.add("filter.type.normal", "Normal Filter");
        this.add("filter.type.normal.tooltip", "Transfer only if it is the same as the filter");
        this.add("filter.type.regulating", "Regulating Filter");
        this.add("filter.type.regulating.tooltip", "Transfer to keep the defined filter amount on the destination");
        this.add("filter.type.exact_count", "Exact Count Filter");
        this.add("filter.type.exact_count.tooltip", "Transfer only if it has same count as the defined filter");
        this.add("filter.type.mod", "Mod Filter");
        this.add("filter.type.mod.tooltip", "Transfer only if it has the same mod id as the defined filter");
        this.add("filter.type.tag", "Tag Filter");
        this.add("filter.type.tag.tooltip", "Transfer only if it has the same tag as the defined in the filter");
        this.add("filter.type.scroll", "*Scroll to change the filter type*");
        this.add("tooltip.transfer_labels.whitelist", "Whitelist");
        this.add("tooltip.transfer_labels.blacklist", "Blacklist");
        this.add("tooltip.transfer_labels.slot.speed", "Speed");
        this.add("tooltip.transfer_labels.slot.amount", "Amount");

    }

    private void formatItem(Item item){
        this.add(item, WordUtils.capitalize(BuiltInRegistries.ITEM.getKey(item).getPath().replace("_", " ")));
    }
}
