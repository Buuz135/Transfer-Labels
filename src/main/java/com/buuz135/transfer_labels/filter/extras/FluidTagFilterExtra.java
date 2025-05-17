package com.buuz135.transfer_labels.filter.extras;

import com.hrznstudio.titanium.util.TagUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class FluidTagFilterExtra implements INBTSerializable<CompoundTag> {

    private final int amount;
    private List<TagKey<Fluid>> extra;

    public FluidTagFilterExtra(int amount) {
        this.amount = amount;
        this.extra = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            this.extra.add(null);
        }
    }

    public void initTag(int slot, FluidStack item){
        var tags = item.getTags().toList();
        if (tags.isEmpty()){
            this.extra.set(slot, null);
        } else {
            this.extra.set(slot, tags.get(0));
        }
    }

    public void nextTag(int slot, FluidStack item){
        var tags = item.getTags().toList();
        if (tags.isEmpty()){
            this.extra.set(slot, null);
            return;
        }

        TagKey<Fluid> currentTag = this.extra.get(slot);
        if (currentTag == null) {
            this.extra.set(slot, tags.get(0));
            return;
        }

        int currentIndex = -1;
        for (int i = 0; i < tags.size(); i++) {
            if (tags.get(i).location().equals(currentTag.location())) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex == -1 || currentIndex == tags.size() - 1) {
            // If tag not found or it's the last one, go to the first tag
            this.extra.set(slot, tags.get(0));
        } else {
            // Go to the next tag
            this.extra.set(slot, tags.get(currentIndex + 1));
        }
    }

    public void previousTag(int slot, FluidStack item){
        var tags = item.getTags().toList();
        if (tags.isEmpty()){
            this.extra.set(slot, null);
            return;
        }

        TagKey<Fluid> currentTag = this.extra.get(slot);
        if (currentTag == null) {
            this.extra.set(slot, tags.get(tags.size() - 1));
            return;
        }

        int currentIndex = -1;
        for (int i = 0; i < tags.size(); i++) {
            if (tags.get(i).location().equals(currentTag.location())) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex == -1 || currentIndex == 0) {
            // If tag not found or it's the first one, go to the last tag
            this.extra.set(slot, tags.get(tags.size() - 1));
        } else {
            // Go to the previous tag
            this.extra.set(slot, tags.get(currentIndex - 1));
        }
    }

    public List<TagKey<Fluid>> getExtra() {
        return extra;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag compoundTag = new CompoundTag();
        for (int i = 0; i < this.extra.size(); i++) {
            if (this.extra.get(i) != null) compoundTag.putString(i + "", this.extra.get(i).location().toString());
        }
        return compoundTag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        for (int i = 0; i < this.extra.size(); i++) {
            if (compoundTag.contains(i + "")) {
                this.extra.set(i, TagUtil.getFluidTag(ResourceLocation.parse(compoundTag.getString(i + ""))));
            } else {
                this.extra.set(i, null);
            }
        }
    }
}
