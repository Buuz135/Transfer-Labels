package com.buuz135.transfer_labels.filter;

import com.buuz135.transfer_labels.client.TLAssetTypes;
import com.buuz135.transfer_labels.filter.extras.NumberFilterExtra;
import com.buuz135.transfer_labels.filter.extras.TagFilterExtra;
import com.buuz135.transfer_labels.gui.ScrollableScreenAddon;
import com.buuz135.transfer_labels.gui.ScrollableSelectionHelper;
import com.buuz135.transfer_labels.gui.WhitelistStateButtonAddon;
import com.buuz135.transfer_labels.gui.filter.ItemFilterScreenAddon;
import com.buuz135.transfer_labels.item.TransferLabelItem;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.api.client.AssetTypes;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import com.hrznstudio.titanium.api.filter.FilterSlot;
import com.hrznstudio.titanium.client.screen.addon.StateButtonInfo;
import com.hrznstudio.titanium.component.button.ButtonComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemFilter implements ILabelFilter<ItemStack>{

    private final FilterSlot<ItemStack>[] filter;

    private Type type;
    private int pointer;
    private final String name;
    private final HashMap<String, INBTSerializable<CompoundTag>> savedFilters;
    private FilterType filterType;
    private ButtonComponent toggleFilterModeButton;
    private TransferLabelItem.Mode mode;

    public ItemFilter(String name, int filterSize, TransferLabelItem.Mode mode) {
        this.name = name;
        this.filter = new FilterSlot[filterSize];
        this.mode = mode;
        this.type = Type.BLACKLIST;
        this.pointer = 0;
        this.filterType = FilterType.NORMAL;
        this.savedFilters = new HashMap<>();
        this.savedFilters.put(FilterType.REGULATING.getName(), new NumberFilterExtra(filterSize));
        this.savedFilters.put(FilterType.EXACT_COUNT.getName(), new NumberFilterExtra(filterSize));
        this.savedFilters.put(FilterType.TAG.getName(), new TagFilterExtra(filterSize));
        this.toggleFilterModeButton = new ButtonComponent( 13,64, 18,18);
        this.toggleFilterModeButton.setId(54571);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean acceptsAsFilter(ItemStack filter) {
        return true;
    }

    @Override
    public void setFilter(int slot, ItemStack stack) {
        if (slot < 0 || slot >= filter.length) {
            throw new RuntimeException("Filter slot " + slot + " not in valid range - [0," + filter.length + ")");
        }
        filter[slot].setFilter(stack);
        NumberFilterExtra numberFilterExtra = (NumberFilterExtra) this.savedFilters.get(FilterType.EXACT_COUNT.getName());
        numberFilterExtra.getExtra().set(slot, stack.getCount());
        numberFilterExtra = (NumberFilterExtra) this.savedFilters.get(FilterType.REGULATING.getName());
        numberFilterExtra.getExtra().set(slot, stack.getCount());
        TagFilterExtra extra = (TagFilterExtra) this.savedFilters.get(FilterType.TAG.getName());
        extra.initTag(slot, stack);

    }

    @Override
    public void setFilter(int slot, FilterSlot<ItemStack> filterSlot) {
        if (slot < 0 || slot >= filter.length) {
            throw new RuntimeException("Filter slot " + slot + " not in valid range - [0," + filter.length + ")");
        }
        this.filter[slot] = filterSlot;
    }

    @Override
    public FilterSlot<ItemStack>[] getFilterSlots() {
        return filter;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void toggleFilterMode() {
        if (this.type.equals(Type.WHITELIST)) {
            this.type = Type.BLACKLIST;
        } else {
            this.type = Type.WHITELIST;
        }
    }

    @Override
    public void handleButtonMessage(int i, Player player, CompoundTag compoundTag) {
        if (compoundTag.contains("Scrollable_Name") && compoundTag.getString("Scrollable_Name").equals("filter_selector")) {
            double scroll = compoundTag.getDouble("Scroll");
            if (scroll > 0) {
                this.filterType = FilterType.getPrevious(this.filterType.getName());
            } else if (scroll < 0) {
                this.filterType = FilterType.getNext(this.filterType.getName());
            }
        }
        if (compoundTag.contains("FilterAmount")) {
            var slot = compoundTag.getInt("FilterAmount");
            var amount = compoundTag.getInt("Scroll");
            if (this.filterType == FilterType.EXACT_COUNT || this.filterType == FilterType.REGULATING) {
                NumberFilterExtra numberFilterExtra = (NumberFilterExtra) this.savedFilters.get(this.filterType.getName());
                numberFilterExtra.add(slot, amount);
            }
        }
        if (compoundTag.contains("FilterTag")) {
            var slot = compoundTag.getInt("FilterTag");
            var scroll = compoundTag.getInt("Scroll");
            if (this.filterType == FilterType.TAG) {
                TagFilterExtra tagFilterExtra = (TagFilterExtra) this.savedFilters.get(this.filterType.getName());
                if (scroll > 0) {
                    // Previous tag
                    tagFilterExtra.previousTag(slot, this.filter[slot].getFilter());
                } else if (scroll < 0) {
                    // Next tag
                    tagFilterExtra.nextTag(slot, this.filter[slot].getFilter());
                }
            }
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag compoundNBT = new CompoundTag();
        compoundNBT.putInt("Pointer", pointer);
        CompoundTag filter = new CompoundTag();
        for (FilterSlot<ItemStack> itemStackFilterSlot : this.filter) {
            if (itemStackFilterSlot != null)
                filter.put(itemStackFilterSlot.getFilterID() + "", itemStackFilterSlot.getFilter().saveOptional(provider));
        }
        compoundNBT.put("Filter", filter);
        compoundNBT.putString("Type", type.name());
        compoundNBT.putString("FilterType", this.filterType.getName());
        CompoundTag savedFiltersNBT = new CompoundTag();
        this.savedFilters.forEach((key, value) -> savedFiltersNBT.put(key, value.serializeNBT(provider)));
        compoundNBT.put("SavedFilters", savedFiltersNBT);
        return compoundNBT;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        pointer = nbt.getInt("Pointer");
        CompoundTag filter = nbt.getCompound("Filter");
        for (FilterSlot<ItemStack> filterSlot : this.filter) {
            filterSlot.setFilter(ItemStack.EMPTY);
        }
        for (String key : filter.getAllKeys()) {
            this.filter[Integer.parseInt(key)].setFilter(ItemStack.parseOptional(provider, filter.getCompound(key)));
        }
        this.type = Type.valueOf(nbt.getString("Type"));
        this.filterType = FilterType.getByName(nbt.getString("FilterType"));
        CompoundTag savedFiltersNBT = nbt.getCompound("SavedFilters");
        this.savedFilters.forEach((key, value) -> value.deserializeNBT(provider, savedFiltersNBT.getCompound(key)));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<IFactory<? extends IScreenAddon>> getScreenAddons() {
        List<IFactory<? extends IScreenAddon>> list = new ArrayList<>();
        list.add(() -> new ItemFilterScreenAddon(this));
        list.add(() -> {
            var lines = new ArrayList<String>();
            FilterType.FILTERS.forEach(filterType1 -> lines.addAll(filterType1.getTooltip()));
            lines.add(Component.translatable("filter.type.scroll").getString());
            return new ScrollableScreenAddon(13, 28, new ScrollableSelectionHelper("filter_selector", -7, lines, () -> Component.translatable(this.filterType.getDisplayName()).getString(), false), () -> {
                switch (this.filterType.getName()) {
                    case "normal":
                        return TLAssetTypes.FILTER_NORMAL;
                    case "regulating":
                        return TLAssetTypes.FILTER_REGULATING;
                    case "exact_count":
                        return TLAssetTypes.FILTER_EXACT_COUNT;
                    case "mod":
                        return TLAssetTypes.FILTER_MOD;
                    case "tag":
                        return TLAssetTypes.FILTER_TAG;
                    default:
                        return AssetTypes.BUTTON_LOCKED;
                }
            });
        });
        list.add(() -> new WhitelistStateButtonAddon(this.toggleFilterModeButton, new StateButtonInfo(0, TLAssetTypes.WHITELIST_BUTTON, "tooltip.transfer_labels.whitelist"), new StateButtonInfo(1, TLAssetTypes.BLACKLIST_BUTTON, "tooltip.transfer_labels.blacklist")) {
            @Override
            public int getState() {
                return ItemFilter.this.type.equals(Type.WHITELIST) ? 0 : 1;
            }
        });
        return list;
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public HashMap<String, INBTSerializable<CompoundTag>> getSavedFilters() {
        return savedFilters;
    }

    public void onContentChanged() {

    }

    public void work(Level level, BlockPos pos, Direction direction, int amount) {
        Direction oppositeDirection = direction.getOpposite();
        BlockPos oppositePos = pos.relative(direction);

        // Get the item handlers for both block entities
        IItemHandler sourceHandler = null;
        IItemHandler targetHandler = null;

        if (this.mode == TransferLabelItem.Mode.EXTRACT) {
            targetHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, oppositePos, oppositeDirection);
            sourceHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, direction);
        } else {
            sourceHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, oppositePos, oppositeDirection);
            targetHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, direction);
        }


        // If we couldn't get the item handlers, return
        if (sourceHandler == null || targetHandler == null) return;

        // Transfer items from the source to the target
        transferItems(sourceHandler, targetHandler, amount);
    }

    private void transferItems(IItemHandler sourceHandler, IItemHandler targetHandler, int defaultMaxAmount) {
        for (int sourceSlot = 0; sourceSlot < sourceHandler.getSlots(); sourceSlot++) {
            var maxAmount = defaultMaxAmount;
            ItemStack sourceStack = sourceHandler.extractItem(sourceSlot, maxAmount, true);
            if (sourceStack.isEmpty()) continue;
            if (!passesFilter(sourceStack)) continue;

            maxAmount = calculateMaxTransferAmount(sourceStack, sourceHandler, targetHandler, sourceSlot, defaultMaxAmount);

            // If maxAmount is 0, skip this item
            if (maxAmount <= 0) continue;

            // For EXACT_COUNT filter type with multiple slots
            if (this.filterType == FilterType.EXACT_COUNT) {
                // Find the matching filter slot to get the exact amount
                int exactAmount = 0;
                for (FilterSlot<ItemStack> filterSlot : this.filter) {
                    if (filterSlot != null && !filterSlot.getFilter().isEmpty() && ItemStack.isSameItem(sourceStack, filterSlot.getFilter())) {
                        NumberFilterExtra numberFilterExtra = (NumberFilterExtra) this.savedFilters.get(FilterType.EXACT_COUNT.getName());
                        exactAmount = numberFilterExtra.getExtra().get(filterSlot.getFilterID());
                        break;
                    }
                }
                // If the current slot doesn't have enough items, extract from multiple slots
                if (sourceStack.getCount() < exactAmount) {
                    handleExactCountMultiSlotTransfer(sourceHandler, targetHandler, sourceStack, sourceSlot, exactAmount);
                    return;
                }
            }

            // Standard extraction for single-slot cases
            sourceStack = sourceHandler.extractItem(sourceSlot, maxAmount, true);

            ItemStack simulatedResult = ItemHandlerHelper.insertItem(targetHandler, sourceStack.copy(), true);
            if (!simulatedResult.isEmpty() && this.filterType == FilterType.EXACT_COUNT){
                continue;
            }
            int amountToExtract = sourceStack.getCount() - simulatedResult.getCount();
            if (amountToExtract > 0) {
                sourceHandler.extractItem(sourceSlot, amountToExtract, false);
                ItemHandlerHelper.insertItem(targetHandler, sourceStack.copy().split(amountToExtract), false);
                break;
            }
        }
    }

    /**
     * Calculates the maximum amount of items to transfer based on the current filter type.
     *
     * @param sourceStack The item stack being transferred
     * @param sourceHandler The source inventory
     * @param targetHandler The target inventory
     * @param sourceSlot The source slot index
     * @param defaultMaxAmount The default maximum transfer amount
     * @return The calculated maximum transfer amount
     */
    private int calculateMaxTransferAmount(ItemStack sourceStack, IItemHandler sourceHandler, IItemHandler targetHandler, int sourceSlot, int defaultMaxAmount) {
        int maxAmount = defaultMaxAmount;

        // If the filter is regulating, calculate the max amount based on what's already in the target
        if (this.filterType == FilterType.REGULATING) {
            for (FilterSlot<ItemStack> filterSlot : this.filter) {
                if (filterSlot != null && !filterSlot.getFilter().isEmpty() && ItemStack.isSameItem(sourceStack, filterSlot.getFilter())) {
                    // Count how many of this item are already in the target
                    int currentCount = 0;
                    for (int targetSlot = 0; targetSlot < targetHandler.getSlots(); targetSlot++) {
                        ItemStack targetStack = targetHandler.getStackInSlot(targetSlot);
                        if (!targetStack.isEmpty() && ItemStack.isSameItem(targetStack, filterSlot.getFilter())) {
                            currentCount += targetStack.getCount();
                        }
                    }

                    NumberFilterExtra numberFilterExtra = (NumberFilterExtra) this.savedFilters.get(FilterType.REGULATING.getName());
                    int desiredAmount = numberFilterExtra.getExtra().get(filterSlot.getFilterID());
                    int neededAmount = Math.max(0, desiredAmount - currentCount);
                    maxAmount = Math.min(neededAmount, 4);
                    break;
                }
            }
        }
        // If the filter is exact count, only transfer if the source stack count matches the filter count exactly
        // or if we can combine items from multiple slots to match the exact count
        else if (this.filterType == FilterType.EXACT_COUNT) {
            for (FilterSlot<ItemStack> filterSlot : this.filter) {
                if (filterSlot != null && !filterSlot.getFilter().isEmpty() && ItemStack.isSameItem(sourceStack, filterSlot.getFilter())) {
                    NumberFilterExtra numberFilterExtra = (NumberFilterExtra) this.savedFilters.get(FilterType.EXACT_COUNT.getName());
                    int exactAmount = numberFilterExtra.getExtra().get(filterSlot.getFilterID());
                    if (exactAmount > defaultMaxAmount){ // If the exact amount is too high, we can't transfer anything
                        maxAmount = 0;
                        break;
                    }
                    if (sourceStack.getCount() >= exactAmount) {
                        maxAmount = exactAmount;
                    } else if (sourceStack.getCount() < exactAmount) { // If the source stack count is less than the exact amount, check if we can combine items from multiple slots
                        // Calculate how many more items we need
                        int availableAmount = sourceStack.getCount();
                        // Look for additional items in other slots
                        for (int otherSlot = 0; otherSlot < sourceHandler.getSlots(); otherSlot++) {
                            if (otherSlot == sourceSlot) continue;
                            // Check if this slot has the same item
                            ItemStack otherStack = sourceHandler.getStackInSlot(otherSlot);
                            if (!otherStack.isEmpty() && ItemStack.isSameItem(otherStack, sourceStack)) {
                                availableAmount += otherStack.getCount();
                                if (availableAmount >= exactAmount) {
                                    break;
                                }
                            }
                        }
                        // If we have enough items across multiple slots, set maxAmount to the exact amount
                        if (availableAmount >= exactAmount) {
                            maxAmount = exactAmount;
                        } else {
                            maxAmount = 0;
                        }
                    }
                    break;
                }
            }
        }

        return maxAmount;
    }

    /**
     * Handles the transfer of items from multiple source slots to match an exact count requirement.
     * 
     * @param sourceHandler The source inventory
     * @param targetHandler The target inventory
     * @param sourceStack The initial source stack
     * @param sourceSlot The initial source slot
     * @param exactAmount The exact amount required
     * @return true if the transfer was successful, false otherwise
     */
    private boolean handleExactCountMultiSlotTransfer(IItemHandler sourceHandler, IItemHandler targetHandler, ItemStack sourceStack, int sourceSlot, int exactAmount) {
        // First, simulate the entire operation to make sure everything fits
        ItemStack combinedStack = sourceStack.copy();
        int remainingNeeded = exactAmount - combinedStack.getCount();
        // Track which slots we'll extract from and how much
        HashMap<Integer, Integer> extractionPlan = new HashMap<>();
        extractionPlan.put(sourceSlot, combinedStack.getCount());
        // Simulate extraction from other slots as needed
        for (int otherSlot = 0; otherSlot < sourceHandler.getSlots() && remainingNeeded > 0; otherSlot++) {
            if (otherSlot == sourceSlot) continue;
            ItemStack otherStack = sourceHandler.getStackInSlot(otherSlot);
            if (!otherStack.isEmpty() && ItemStack.isSameItem(otherStack, combinedStack)) {
                // Calculate how many to extract from this slot
                int toExtract = Math.min(remainingNeeded, otherStack.getCount());
                extractionPlan.put(otherSlot, toExtract);
                remainingNeeded -= toExtract;
            }
        }
        // If we couldn't gather enough items, skip this transfer
        if (remainingNeeded > 0) {
            return false;
        }
        // Simulate insertion into target
        combinedStack.setCount(exactAmount);
        ItemStack simulatedResult = ItemHandlerHelper.insertItem(targetHandler, combinedStack.copy(), true);
        // If we can't insert all items, skip this transfer
        if (!simulatedResult.isEmpty()) {
            return false;
        }
        // Now perform the actual extraction and insertion
        combinedStack = ItemStack.EMPTY;
        // Extract from the planned slots
        for (var entry : extractionPlan.entrySet()) {
            int slot = entry.getKey();
            int amount = entry.getValue();

            ItemStack extracted = sourceHandler.extractItem(slot, amount, false);

            if (combinedStack.isEmpty()) {
                combinedStack = extracted;
            } else {
                combinedStack.grow(extracted.getCount());
            }
        }
        combinedStack.setCount(exactAmount);
        ItemHandlerHelper.insertItem(targetHandler, combinedStack, false);
        return true;
    }

    private boolean passesFilter(ItemStack stack) {
        // If there are no filter slots, allow all items
        if (this.filter.length == 0) return true;

        // Check if the item matches any of the filter slots
        boolean matches = false;
        for (FilterSlot<ItemStack> filterSlot : this.filter) {
            if (filterSlot == null || filterSlot.getFilter().isEmpty()) continue;

            // Check if the item matches the filter based on the filter type
            boolean itemMatches = false;

            if (this.filterType == FilterType.NORMAL) {
                // Normal filter: exact match
                itemMatches = ItemStack.isSameItem(stack, filterSlot.getFilter());
            } else if (this.filterType == FilterType.REGULATING) {
                // Regulating filter: keep the defined amount in the destination
                itemMatches = ItemStack.isSameItem(stack, filterSlot.getFilter());
                // Note: The actual regulating logic would be more complex and would need to count items in the target inventory
            } else if (this.filterType == FilterType.EXACT_COUNT) {
                // Exact Count filter: match item only (count is checked separately in transferItems)
                itemMatches = ItemStack.isSameItem(stack, filterSlot.getFilter());
            } else if (this.filterType == FilterType.MOD) {
                // Mod filter: match mod ID
                String stackModId = BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
                String filterModId = BuiltInRegistries.ITEM.getKey(filterSlot.getFilter().getItem()).getNamespace();
                itemMatches = stackModId.equals(filterModId);
            } else if (this.filterType == FilterType.TAG) {
                // Tag filter: match tag
                TagFilterExtra tagFilterExtra = (TagFilterExtra) this.savedFilters.get(FilterType.TAG.getName());
                TagKey<Item> tagKey = tagFilterExtra.getExtra().get(filterSlot.getFilterID());
                if (tagKey != null) {
                    itemMatches = stack.is(tagKey);
                }
            }

            if (itemMatches) {
                matches = true;
                break;
            }
        }
        // Apply whitelist/blacklist logic
        return this.type.getFilter().test(matches);
    }

}
