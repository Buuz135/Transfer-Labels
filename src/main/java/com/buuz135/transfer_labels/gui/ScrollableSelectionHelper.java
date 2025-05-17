package com.buuz135.transfer_labels.gui;

import com.hrznstudio.titanium.Titanium;
import com.hrznstudio.titanium.network.locator.ILocatable;
import com.hrznstudio.titanium.network.messages.ButtonClickNetworkMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ScrollableSelectionHelper {

    private List<String> options;
    private Supplier<String> current;
    private String name;
    private int id;
    private final boolean exactMatch;

    public ScrollableSelectionHelper(String name, int id, List<String> options, Supplier<String> current, boolean exactMatch) {
        this.options = options;
        this.current = current;
        this.name = name;
        this.id = id;
        this.exactMatch = exactMatch;
    }

    public List<String> getFormattedOptions() {
        var formattedOptions = new ArrayList<String>();
        this.options.forEach(option -> {
            formattedOptions.add((exactMatch ? option.equalsIgnoreCase(current.get()) : option.contains(current.get())) ? ChatFormatting.GOLD + "[" + option + ChatFormatting.GOLD + "]" :  ChatFormatting.GRAY + option);
        });
        return formattedOptions;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, ILocatable locatable) {
        CompoundTag compoundNBT = new CompoundTag();
        compoundNBT.putString("Scrollable_Name", name);
        compoundNBT.putDouble("Scroll", scrollY);

        Titanium.NETWORK.sendToServer(new ButtonClickNetworkMessage(locatable.getLocatorInstance(), id, compoundNBT));
        return true;

    }

}
