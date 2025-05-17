package com.buuz135.transfer_labels.gui;

import com.hrznstudio.titanium.client.screen.addon.StateButtonAddon;
import com.hrznstudio.titanium.client.screen.addon.StateButtonInfo;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.component.button.ButtonComponent;
import com.hrznstudio.titanium.util.AssetUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public abstract class WhitelistStateButtonAddon extends StateButtonAddon {

    public WhitelistStateButtonAddon(ButtonComponent buttonComponent, StateButtonInfo... buttonInfos) {
        super(buttonComponent, buttonInfos);
    }

    @Override
    public void drawForegroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        StateButtonInfo buttonInfo = this.getStateInfo();
        if (buttonInfo != null && this.isMouseOver((double)(mouseX - guiX), (double)(mouseY - guiY))) {
            AssetUtil.drawSelectingOverlay(guiGraphics, this.getPosX() + 2, this.getPosY() + 2, this.getPosX() + this.getXSize(), this.getPosY() + this.getYSize() - 1);
        }
    }
}
