package com.buuz135.transfer_labels.item;

import com.buuz135.transfer_labels.filter.ILabelFilter;
import com.buuz135.transfer_labels.filter.ItemFilter;
import com.hrznstudio.titanium.api.filter.FilterSlot;
import com.hrznstudio.titanium.api.filter.IFilter;
import net.minecraft.world.item.ItemStack;

public class ItemStackTransferLabelItem extends TransferLabelItem {

    public ItemStackTransferLabelItem(TransferLabelItem.Mode mode) {
        super("itemstack", new Properties(), mode);
    }

    @Override
    public ILabelFilter<ItemStack> createFilter() {
        var filter = new ItemFilter("item_filter", 5*4, this.getMode());

        int slotSize = 18;
        int startX = 43;
        int startY = 20;

        for (int i = 0; i < filter.getFilterSlots().length; i++) {
            int x = startX + (i % 5) * slotSize;
            int y = startY + (i / 5) * slotSize;
            FilterSlot<ItemStack> slot = new FilterSlot<>(x, y, i, ItemStack.EMPTY);
            filter.setFilter(i, slot);
        }

        return filter;
    }
}
