package com.buuz135.transfer_labels.filter;

import com.buuz135.transfer_labels.Config;
import com.buuz135.transfer_labels.client.TLAssetTypes;
import com.buuz135.transfer_labels.filter.extras.FluidTagFilterExtra;
import com.buuz135.transfer_labels.filter.extras.NumberFilterExtra;
import com.buuz135.transfer_labels.filter.extras.TagFilterExtra;
import com.buuz135.transfer_labels.gui.ScrollableScreenAddon;
import com.buuz135.transfer_labels.gui.ScrollableSelectionHelper;
import com.buuz135.transfer_labels.gui.WhitelistStateButtonAddon;
import com.buuz135.transfer_labels.gui.filter.FluidFilterScreenAddon;
import com.buuz135.transfer_labels.item.TransferLabelItem;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.api.client.AssetTypes;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import com.hrznstudio.titanium.api.filter.FilterSlot;
import com.hrznstudio.titanium.client.screen.addon.StateButtonInfo;
import com.hrznstudio.titanium.component.button.ButtonComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import com.hrznstudio.titanium.nbthandler.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FluidFilter implements ILabelFilter<FluidStack>{

    private final FilterSlot<FluidStack>[] filter;

    private Type type;
    private int pointer;
    private final String name;
    private final HashMap<String, INBTSerializable<CompoundTag>> savedFilters;
    private FilterType filterType;
    private ButtonComponent toggleFilterModeButton;
    private TransferLabelItem.Mode mode;

    public FluidFilter(String name, int filterSize, TransferLabelItem.Mode mode) {
        this.name = name;
        this.filter = new FilterSlot[filterSize];
        this.mode = mode;
        this.type = Type.BLACKLIST;
        this.pointer = 0;
        this.filterType = FilterType.NORMAL;
        this.savedFilters = new HashMap<>();
        this.savedFilters.put(FilterType.REGULATING.getName(), new NumberFilterExtra(filterSize));
        this.savedFilters.put(FilterType.EXACT_COUNT.getName(), new NumberFilterExtra(filterSize));
        this.savedFilters.put(FilterType.TAG.getName(), new FluidTagFilterExtra(filterSize));
        this.toggleFilterModeButton = new ButtonComponent(13, 64, 18, 18);
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

        FluidStack fluidStack = FluidStack.EMPTY;
        var capability = ItemAccess.forStack(stack).getCapability(Capabilities.Fluid.ITEM);
        if (capability != null && capability.size() > 0) {
            fluidStack = capability.getResource(0).toStack(capability.getAmountAsInt(0));
        }

        filter[slot].setFilter(fluidStack.copy());
        NumberFilterExtra numberFilterExtra = (NumberFilterExtra) this.savedFilters.get(FilterType.EXACT_COUNT.getName());
        numberFilterExtra.getExtra().set(slot, fluidStack.getAmount());
        numberFilterExtra = (NumberFilterExtra) this.savedFilters.get(FilterType.REGULATING.getName());
        numberFilterExtra.getExtra().set(slot, fluidStack.getAmount());
        FluidTagFilterExtra extra = (FluidTagFilterExtra) this.savedFilters.get(FilterType.TAG.getName());
        extra.initTag(slot, fluidStack);
    }

    @Override
    public void setFilter(int slot, FilterSlot<FluidStack> filterSlot) {
        if (slot < 0 || slot >= filter.length) {
            throw new RuntimeException("Filter slot " + slot + " not in valid range - [0," + filter.length + ")");
        }
        this.filter[slot] = filterSlot;
    }

    @Override
    public FilterSlot<FluidStack>[] getFilterSlots() {
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
        if (compoundTag.contains("Scrollable_Name") && compoundTag.getStringOr("Scrollable_Name", "").equals("filter_selector")) {
            double scroll = compoundTag.getDoubleOr("Scroll", 0);
            if (scroll > 0) {
                this.filterType = FilterType.getPrevious(this.filterType.getName());
            } else if (scroll < 0) {
                this.filterType = FilterType.getNext(this.filterType.getName());
            }
        }
        if (compoundTag.contains("FilterAmount")) {
            var slot = compoundTag.getIntOr("FilterAmount", 0);
            var amount = compoundTag.getIntOr("Scroll", 0);
            if (this.filterType == FilterType.EXACT_COUNT || this.filterType == FilterType.REGULATING) {
                NumberFilterExtra numberFilterExtra = (NumberFilterExtra) this.savedFilters.get(this.filterType.getName());
                numberFilterExtra.add(slot, amount);
            }
        }
        if (compoundTag.contains("FilterTag")) {
            var slot = compoundTag.getIntOr("FilterTag", 0);
            var scroll = compoundTag.getIntOr("Scroll", 0);
            if (this.filterType == FilterType.TAG) {
                FluidTagFilterExtra tagFilterExtra = (FluidTagFilterExtra) this.savedFilters.get(this.filterType.getName());
                if (scroll > 0) {
                    tagFilterExtra.previousTag(slot, this.filter[slot].getFilter());
                } else if (scroll < 0) {
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
        for (FilterSlot<FluidStack> fluidStackFilterSlot : this.filter) {
            if (fluidStackFilterSlot != null) {
                filter.put(fluidStackFilterSlot.getFilterID() + "", saveFluid(provider, fluidStackFilterSlot.getFilter()));
            }
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
        pointer = nbt.getIntOr("Pointer", 0);
        CompoundTag filter = nbt.getCompoundOrEmpty("Filter");
        for (FilterSlot<FluidStack> filterSlot : this.filter) {
            filterSlot.setFilter(FluidStack.EMPTY);
        }
        for (String key : filter.keySet()) {
            int slot = Integer.parseInt(key);
            if (slot >= 0 && slot < this.filter.length) {
                this.filter[slot].setFilter(loadFluid(provider, filter.getCompoundOrEmpty(key)));
            }
        }
        this.type = Type.valueOf(nbt.getStringOr("Type", Type.BLACKLIST.name()));
        this.filterType = FilterType.getByName(nbt.getStringOr("FilterType", FilterType.NORMAL.getName()));
        CompoundTag savedFiltersNBT = nbt.getCompoundOrEmpty("SavedFilters");
        this.savedFilters.forEach((key, value) -> value.deserializeNBT(provider, savedFiltersNBT.getCompoundOrEmpty(key)));
    }

    @Override
    public List<IFactory<?>> getScreenAddons() {
        List<IFactory<?>> list = new ArrayList<>();
        list.add(() -> new FluidFilterScreenAddon(this));
        list.add(() -> {
            var lines = new ArrayList<String>();
            FilterType.FILTERS.forEach(filterType1 -> lines.addAll(filterType1.getTooltip()));
            lines.add(Component.translatable("filter.type.scroll").withStyle(ChatFormatting.DARK_GRAY).getString());
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
                return FluidFilter.this.type.equals(Type.WHITELIST) ? 0 : 1;
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
        if (!level.isLoaded(pos) || !level.isLoaded(oppositePos)) return;
        ResourceHandler<FluidResource> sourceHandler;
        ResourceHandler<FluidResource> targetHandler;

        if (this.mode == TransferLabelItem.Mode.EXTRACT) {
            targetHandler = level.getCapability(Capabilities.Fluid.BLOCK, oppositePos, oppositeDirection);
            sourceHandler = level.getCapability(Capabilities.Fluid.BLOCK, pos, direction);
        } else {
            sourceHandler = level.getCapability(Capabilities.Fluid.BLOCK, oppositePos, oppositeDirection);
            targetHandler = level.getCapability(Capabilities.Fluid.BLOCK, pos, direction);
        }

        if (sourceHandler == null || targetHandler == null) return;
        transferFluids(sourceHandler, targetHandler, (int) Math.min(Integer.MAX_VALUE, (long) amount * Config.fluidTransferMultiplier));
    }

    private void transferFluids(ResourceHandler<FluidResource> sourceHandler, ResourceHandler<FluidResource> targetHandler, int defaultMaxAmount) {
        for (int sourceIndex = 0; sourceIndex < sourceHandler.size(); sourceIndex++) {
            FluidResource sourceResource = sourceHandler.getResource(sourceIndex);
            int sourceAmount = sourceHandler.getAmountAsInt(sourceIndex);
            if (sourceResource.isEmpty() || sourceAmount == 0 || !passesFilter(sourceResource.toStack(sourceAmount))) continue;

            int maxAmount = calculateMaxTransferAmount(sourceResource, sourceHandler, targetHandler, sourceIndex, defaultMaxAmount);
            if (maxAmount <= 0) continue;

            if (this.filterType == FilterType.EXACT_COUNT) {
                try (Transaction transaction = Transaction.openRoot()) {
                    int moved = ResourceHandlerUtil.moveStacking(sourceHandler, targetHandler, sourceResource::equals, maxAmount, transaction);
                    if (moved == maxAmount) {
                        transaction.commit();
                        return;
                    }
                }
            } else if (ResourceHandlerUtil.moveStacking(sourceHandler, targetHandler, sourceResource::equals, maxAmount, null) > 0) {
                return;
            }
        }
    }

    private int calculateMaxTransferAmount(FluidResource sourceResource, ResourceHandler<FluidResource> sourceHandler, ResourceHandler<FluidResource> targetHandler, int sourceIndex, int defaultMaxAmount) {
        int maxAmount = defaultMaxAmount;
        FluidStack sourceStack = sourceResource.toStack(sourceHandler.getAmountAsInt(sourceIndex));

        if (this.filterType == FilterType.REGULATING) {
            for (FilterSlot<FluidStack> filterSlot : this.filter) {
                if (filterSlot != null && !filterSlot.getFilter().isEmpty() && areFluidStacksEqual(filterSlot.getFilter(), sourceStack)) {
                    int currentAmount = 0;
                    for (int targetIndex = 0; targetIndex < targetHandler.size(); targetIndex++) {
                        FluidStack targetStack = targetHandler.getResource(targetIndex).toStack(targetHandler.getAmountAsInt(targetIndex));
                        if (!targetStack.isEmpty() && areFluidStacksEqual(targetStack, filterSlot.getFilter())) {
                            currentAmount += targetStack.getAmount();
                        }
                    }

                    NumberFilterExtra numberFilterExtra = (NumberFilterExtra) this.savedFilters.get(FilterType.REGULATING.getName());
                    int desiredAmount = numberFilterExtra.getExtra().get(filterSlot.getFilterID());
                    int neededAmount = Math.max(0, desiredAmount - currentAmount);
                    maxAmount = Math.min(neededAmount, defaultMaxAmount);
                    break;
                }
            }
        } else if (this.filterType == FilterType.EXACT_COUNT) {
            for (FilterSlot<FluidStack> filterSlot : this.filter) {
                if (filterSlot != null && !filterSlot.getFilter().isEmpty() && areFluidStacksEqual(filterSlot.getFilter(), sourceStack)) {
                    NumberFilterExtra numberFilterExtra = (NumberFilterExtra) this.savedFilters.get(FilterType.EXACT_COUNT.getName());
                    int exactAmount = numberFilterExtra.getExtra().get(filterSlot.getFilterID());
                    if (exactAmount > defaultMaxAmount) return 0;
                    int availableAmount = 0;
                    for (int otherIndex = 0; otherIndex < sourceHandler.size(); otherIndex++) {
                        if (sourceResource.equals(sourceHandler.getResource(otherIndex))) {
                            availableAmount += sourceHandler.getAmountAsInt(otherIndex);
                        }
                    }
                    maxAmount = availableAmount >= exactAmount ? exactAmount : 0;
                    break;
                }
            }
        }

        return maxAmount;
    }

    /**
     * Helper method to compare two FluidStacks
     * Replaces the deprecated isFluidEqual method
     */
    private boolean areFluidStacksEqual(FluidStack stack1, FluidStack stack2) {
        return FluidStack.isSameFluidSameComponents(stack1, stack2);
    }

    private static CompoundTag saveFluid(HolderLookup.Provider provider, FluidStack stack) {
        return (CompoundTag) FluidStack.OPTIONAL_CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), stack).result().orElseGet(CompoundTag::new);
    }

    private static FluidStack loadFluid(HolderLookup.Provider provider, CompoundTag tag) {
        return FluidStack.OPTIONAL_CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag).result().orElse(FluidStack.EMPTY);
    }

    private boolean passesFilter(FluidStack stack) {
        // If there are no filter slots, allow all fluids
        if (this.filter.length == 0) return true;

        // Check if the fluid matches any of the filter slots
        boolean matches = false;
        for (FilterSlot<FluidStack> filterSlot : this.filter) {
            if (filterSlot == null || filterSlot.getFilter().isEmpty()) continue;

            // Check if the fluid matches the filter based on the filter type
            boolean fluidMatches = false;

            if (this.filterType == FilterType.NORMAL) {
                // Normal filter: exact match
                fluidMatches = areFluidStacksEqual(filterSlot.getFilter(), stack);
            } else if (this.filterType == FilterType.REGULATING) {
                // Regulating filter: keep the defined amount in the destination
                fluidMatches = areFluidStacksEqual(filterSlot.getFilter(), stack);
            } else if (this.filterType == FilterType.EXACT_COUNT) {
                // Exact Count filter: match fluid only (amount is checked separately in transferFluids)
                fluidMatches = areFluidStacksEqual(filterSlot.getFilter(), stack);
            } else if (this.filterType == FilterType.MOD) {
                // Mod filter: match mod ID
                String stackModId = BuiltInRegistries.FLUID.getKey(stack.getFluid()).getNamespace();
                String filterModId = BuiltInRegistries.FLUID.getKey(filterSlot.getFilter().getFluid()).getNamespace();
                fluidMatches = stackModId.equals(filterModId);
            } else if (this.filterType == FilterType.TAG) {
                // Tag filter: match tag
                FluidTagFilterExtra tagFilterExtra = (FluidTagFilterExtra) this.savedFilters.get(FilterType.TAG.getName());
                TagKey tagKey = tagFilterExtra.getExtra().get(filterSlot.getFilterID());
                if (tagKey != null) {
                    fluidMatches = stack.getFluid().defaultFluidState().is(tagKey);
                }
            }

            if (fluidMatches) {
                matches = true;
                break;
            }
        }
        // Apply whitelist/blacklist logic
        return this.type.getFilter().test(matches);
    }
}
