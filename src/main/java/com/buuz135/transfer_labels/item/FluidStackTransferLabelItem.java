package com.buuz135.transfer_labels.item;

import com.buuz135.transfer_labels.filter.FluidFilter;
import com.buuz135.transfer_labels.filter.ILabelFilter;
import com.buuz135.transfer_labels.filter.ItemFilter;
import com.hrznstudio.titanium.api.filter.FilterSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidStackTransferLabelItem extends TransferLabelItem {

    public FluidStackTransferLabelItem(Mode mode) {
        super("fluidstack", new Properties(), mode);
    }

    @Override
    public ILabelFilter<FluidStack> createFilter() {
        var filter = new FluidFilter("fluid_filter", 5*4, this.getMode());

        int slotSize = 18;
        int startX = 43;
        int startY = 20;

        for (int i = 0; i < filter.getFilterSlots().length; i++) {
            int x = startX + (i % 5) * slotSize;
            int y = startY + (i / 5) * slotSize;
            FilterSlot<FluidStack> slot = new FilterSlot<FluidStack>(x, y, i, FluidStack.EMPTY);
            filter.setFilter(i, slot);
        }

        return filter;
    }
}
