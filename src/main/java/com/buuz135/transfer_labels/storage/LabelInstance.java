package com.buuz135.transfer_labels.storage;

import com.buuz135.transfer_labels.TransferLabels;
import com.buuz135.transfer_labels.client.TLAssetProvider;
import com.buuz135.transfer_labels.filter.FilterType;
import com.buuz135.transfer_labels.filter.ILabelFilter;
import com.buuz135.transfer_labels.gui.ScrollableScreenAddon;
import com.buuz135.transfer_labels.gui.ScrollableSelectionHelper;
import com.buuz135.transfer_labels.gui.SmallTextScreenAddon;
import com.buuz135.transfer_labels.item.TransferLabelItem;
import com.buuz135.transfer_labels.packet.LabelSyncPacket;
import com.buuz135.transfer_labels.packet.SingleLabelSyncPacket;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.api.client.AssetTypes;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import com.hrznstudio.titanium.api.client.IScreenAddonProvider;
import com.hrznstudio.titanium.api.filter.IFilter;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.client.screen.asset.IHasAssetProvider;
import com.hrznstudio.titanium.component.IComponentHarness;
import com.hrznstudio.titanium.component.inventory.InventoryComponent;
import com.hrznstudio.titanium.container.BasicAddonContainer;
import com.hrznstudio.titanium.container.addon.IContainerAddon;
import com.hrznstudio.titanium.container.addon.IContainerAddonProvider;
import com.hrznstudio.titanium.network.IButtonHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LabelInstance implements IScreenAddonProvider, MenuProvider, IButtonHandler, IContainerAddonProvider, IHasAssetProvider, INBTSerializable<CompoundTag>, IComponentHarness {

    private ItemStack label;
    private Level level;
    private BlockPos pos;
    private Direction facing;
    private ILabelFilter filter;
    private final LabelBlock parent;
    private InventoryComponent<LabelInstance> amountFilter;
    private InventoryComponent<LabelInstance> speedFilter;

    public LabelInstance(ItemStack label, Level world, BlockPos pos, Direction direction, LabelBlock parent) {
        this.label = label;
        this.level = world;
        this.pos = pos;
        this.facing = direction;
        this.parent = parent;
        if (label.getItem() instanceof TransferLabelItem transferLabelItem){
            this.filter = transferLabelItem.createFilter();
        }
        this.amountFilter = new InventoryComponent<LabelInstance>("amountFilter", 145, 30, 1)
                .setSlotLimit(63)
                .setInputFilter((itemStack, integer) -> ItemStack.isSameItem(itemStack, label))
                .setSlotToItemStackRender(0, label)
                .setSlotToColorRender(0, DyeColor.PURPLE);
        this.speedFilter = new InventoryComponent<LabelInstance>("speedFilter", 145, 66, 1)
                .setSlotLimit(38)
                .setInputFilter((itemStack, integer) -> ItemStack.isSameItem(itemStack, label))
                .setSlotToItemStackRender(0, label)
                .setSlotToColorRender(0, DyeColor.LIME);
        this.amountFilter.setComponentHarness(this);
        this.speedFilter.setComponentHarness(this);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public @NotNull List<IFactory<? extends IScreenAddon>> getScreenAddons() {
        var arrayList = new ArrayList<IFactory<? extends IScreenAddon>>();
        if (this.filter != null){
            arrayList.addAll(this.filter.getScreenAddons());
        }
        arrayList.addAll(this.amountFilter.getScreenAddons());
        arrayList.addAll(this.speedFilter.getScreenAddons());
        arrayList.add(() -> new SmallTextScreenAddon(144 + 18/2, 30, "tooltip.transfer_labels.slot.amount"));
        arrayList.add(() -> new SmallTextScreenAddon(144 + 18/2, 66, "tooltip.transfer_labels.slot.speed"));

        return arrayList;
    }

    @Override
    public IAssetProvider getAssetProvider() {
        return TLAssetProvider.DEFAULT_PROVIDER;
    }

    @Override
    public @NotNull List<IFactory<? extends IContainerAddon>> getContainerAddons() {
        var arrayList = new ArrayList<IFactory<? extends IContainerAddon>>();
        arrayList.addAll(this.amountFilter.getContainerAddons());
        arrayList.addAll(this.speedFilter.getContainerAddons());
        return arrayList;
    }

    @Override
    public void handleButtonMessage(int i, Player player, CompoundTag compoundTag) {
        if (i == -2){
            var slot = compoundTag.getInt("Slot");
            var stack = ItemStack.parseOptional(player.level().registryAccess(), compoundTag.getCompound("Filter"));
            this.filter.setFilter(slot, stack);
        }
        if (i == -7) {
            this.filter.handleButtonMessage(i, player, compoundTag);
        }
        if (i == 54571) {
            this.filter.toggleFilterMode();
        }
        parent.updateToNearby(player);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Label " + this.facing.getName());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int menu, Inventory inventoryPlayer, Player entityPlayer) {
       return new BasicAddonContainer(this, new LabelLocatorInstance(this.pos, this.facing), ContainerLevelAccess.create(this.level, this.pos), inventoryPlayer, menu);
    }

    public ItemStack getLabel() {
        return label;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag compoundTag = new CompoundTag();
        if (this.filter != null){
            compoundTag.put("Filter", this.filter.serializeNBT(provider));
        }
        compoundTag.put("AmountFilter", this.amountFilter.serializeNBT(provider));
        compoundTag.put("SpeedFilter", this.speedFilter.serializeNBT(provider));
        return compoundTag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        if (this.filter != null){
            this.filter.deserializeNBT(provider, compoundTag.getCompound("Filter"));
        }
        this.amountFilter.deserializeNBT(provider, compoundTag.getCompound("AmountFilter"));
        this.speedFilter.deserializeNBT(provider, compoundTag.getCompound("SpeedFilter"));
    }

    public void work(Level level){
        if (this.filter != null && level.getGameTime() % (20 - getSpeed()) == 0) this.filter.work(level, this.pos, this.facing, 1 + getAmount());
    }

    public int getSpeed() {
        return this.speedFilter.getStackInSlot(0).getCount() / 2;
    }

    public int getAmount() {
        return this.amountFilter.getStackInSlot(0).getCount();
    }

    @Override
    public Level getComponentWorld() {
        return level;
    }

    @Override
    public void markComponentForUpdate(boolean b) {
        if (this.level instanceof ServerLevel serverLevel)LabelStorage.getStorageFor(serverLevel).markDirty();
    }

    @Override
    public void markComponentDirty() {
        if (this.level instanceof ServerLevel serverLevel)LabelStorage.getStorageFor(serverLevel).markDirty();
    }

    public InventoryComponent<LabelInstance> getAmountFilter() {
        return amountFilter;
    }

    public InventoryComponent<LabelInstance> getSpeedFilter() {
        return speedFilter;
    }
}
