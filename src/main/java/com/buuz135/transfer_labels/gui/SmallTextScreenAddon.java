package com.buuz135.transfer_labels.gui;

import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;


public class SmallTextScreenAddon extends BasicScreenAddon {

    private final String langKey;

    public SmallTextScreenAddon(int posX, int posY, String langKey) {
        super(posX, posY);
        this.langKey = langKey;
    }

    @Override
    public int getXSize() {
        return 0;
    }

    @Override
    public int getYSize() {
        return 0;
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider iAssetProvider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public void drawForegroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider iAssetProvider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.pose().pushPose();

        var scaling = 0.5f;
        guiGraphics.pose().translate(0,0, 300);
        guiGraphics.pose().scale(scaling, scaling, scaling);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable(langKey), (int) (getPosX() / scaling), (int) (getPosY() / scaling), 0xFFFFFF);
        guiGraphics.pose().popPose();
    }
}
