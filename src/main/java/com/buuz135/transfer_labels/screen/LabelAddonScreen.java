package com.buuz135.transfer_labels.screen;

import com.hrznstudio.titanium.client.screen.container.BasicAddonScreen;
import com.hrznstudio.titanium.container.BasicAddonContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class LabelAddonScreen extends BasicAddonScreen {

    public LabelAddonScreen(BasicAddonContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return this.getChildAt(mouseX, mouseY).filter((p_293596_) -> p_293596_.mouseScrolled(mouseX, mouseY, scrollX, scrollY)).isPresent();
    }
}
