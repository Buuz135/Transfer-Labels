package com.buuz135.transfer_labels.gui;

import com.hrznstudio.titanium.Titanium;
import com.hrznstudio.titanium.api.client.AssetTypes;
import com.hrznstudio.titanium.api.client.IAsset;
import com.hrznstudio.titanium.api.client.IAssetType;
import com.hrznstudio.titanium.api.filter.FilterSlot;
import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.addon.StateButtonInfo;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.network.locator.ILocatable;
import com.hrznstudio.titanium.network.messages.ButtonClickNetworkMessage;
import com.hrznstudio.titanium.util.AssetUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ScrollableScreenAddon extends BasicScreenAddon {

    private ScrollableSelectionHelper selectionHelper;
    private Supplier<IAssetType> assetType;
    private Rectangle area;

    public ScrollableScreenAddon(int posX, int posY, ScrollableSelectionHelper selectionHelper, Supplier<IAssetType> assetType) {
        super(posX, posY);
        this.selectionHelper = selectionHelper;
        this.assetType = assetType;
    }

    @Override
    public int getXSize() {
        return area != null ? area.width : 0;
    }

    @Override
    public int getYSize() {
        return area != null ? area.height : 0;
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider iAssetProvider,  int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        if (assetType != null) {
            IAsset asset = iAssetProvider.getAsset(assetType.get());
            area = asset.getArea();
            AssetUtil.drawAsset(guiGraphics, screen, asset, this.getPosX() + guiX, this.getPosY() + guiY);
        }
    }

    @Override
    public void drawForegroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider iAssetProvider,  int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        if (this.isMouseOver((double)(mouseX - guiX), (double)(mouseY - guiY))) {
            AssetUtil.drawSelectingOverlay(guiGraphics, this.getPosX() + 2, this.getPosY() + 2, this.getPosX() + this.getXSize() - 2, this.getPosY() + this.getYSize() - 3);
        }
    }

    public void setAssetType(Supplier<IAssetType> assetType) {
        this.assetType = assetType;
    }

    @Override
    public List<Component> getTooltipLines() {
        return selectionHelper.getFormattedOptions().stream().map(s -> Component.literal(s)).collect(Collectors.toList());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof AbstractContainerScreen && ((AbstractContainerScreen)screen).getMenu() instanceof ILocatable) {

            ILocatable locatable = (ILocatable)((AbstractContainerScreen)screen).getMenu();
            if (selectionHelper.mouseScrolled(mouseX, mouseY, scrollX, scrollY, locatable)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.TRIPWIRE_CLICK_ON,2));
                return true;
            }
        }

        return false;
    }

}
