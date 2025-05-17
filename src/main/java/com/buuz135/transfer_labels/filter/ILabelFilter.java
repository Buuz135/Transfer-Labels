package com.buuz135.transfer_labels.filter;

import com.hrznstudio.titanium.api.client.IScreenAddonProvider;
import com.hrznstudio.titanium.api.filter.FilterSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.function.Predicate;

public interface ILabelFilter<T> extends INBTSerializable<CompoundTag>, IScreenAddonProvider {
    String getName();

    boolean acceptsAsFilter(ItemStack var1);

    void setFilter(int var1, ItemStack var2);

    void setFilter(int var1, FilterSlot<T> var2);

    FilterSlot<T>[] getFilterSlots();

    Type getType();

    void toggleFilterMode();

    void handleButtonMessage(int i, Player player, CompoundTag compoundTag);

    void work(Level level, BlockPos pos, Direction direction, int amount);

    public static enum Type {
        WHITELIST((filter) -> filter),
        BLACKLIST((filter) -> !filter);

        private final Predicate<Boolean> filter;

        private Type(Predicate<Boolean> filter) {
            this.filter = filter;
        }

        public Predicate<Boolean> getFilter() {
            return this.filter;
        }
    }
}