package com.buuz135.transfer_labels.filter.extras;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

public class NumberFilterExtra implements INBTSerializable<CompoundTag> {

    private final int amount;
    private List<Integer> extra;

    public NumberFilterExtra(int amount) {
        this.amount = amount;
        this.extra = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            this.extra.add(1);
        }
    }

    public void add(int slot, int amount){
        this.extra.set(slot, Math.max(0, Math.min(this.extra.get(slot) + amount, Integer.MAX_VALUE - 1)));
    }

    public List<Integer> getExtra() {
        return extra;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag compoundTag = new CompoundTag();
        for (int i = 0; i < this.extra.size(); i++) {
            compoundTag.putInt(i + "", this.extra.get(i));
        }
        return compoundTag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        compoundTag.getAllKeys().forEach(s -> {
            this.extra.set(Integer.parseInt(s), compoundTag.getInt(s));
        });
    }
}
