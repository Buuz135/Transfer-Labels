package com.buuz135.transfer_labels.gui.filter;

import com.buuz135.transfer_labels.filter.FilterType;
import com.buuz135.transfer_labels.filter.FluidFilter;
import com.buuz135.transfer_labels.filter.extras.FluidTagFilterExtra;
import com.buuz135.transfer_labels.filter.extras.NumberFilterExtra;
import com.buuz135.transfer_labels.filter.extras.TagFilterExtra;
import com.buuz135.transfer_labels.gui.ScrollableSelectionHelper;
import com.buuz135.transfer_labels.util.NumberUtils;
import com.hrznstudio.titanium.Titanium;
import com.hrznstudio.titanium.api.client.AssetTypes;
import com.hrznstudio.titanium.api.client.IAsset;
import com.hrznstudio.titanium.api.filter.FilterSlot;
import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.network.locator.ILocatable;
import com.hrznstudio.titanium.network.messages.ButtonClickNetworkMessage;
import com.hrznstudio.titanium.util.AssetUtil;
import com.hrznstudio.titanium.util.TagUtil;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.checkerframework.checker.units.qual.C;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FluidFilterScreenAddon extends BasicScreenAddon {
    private final FluidFilter filter;

    public FluidFilterScreenAddon(FluidFilter filter) {
        super(filter.getFilterSlots()[0].getX(), filter.getFilterSlots()[0].getY());
        this.filter = filter;
    }

    public int getXSize() {
        return this.filter.getFilterSlots()[this.filter.getFilterSlots().length - 1].getX() + 17;
    }

    public int getYSize() {
        return this.filter.getFilterSlots()[this.filter.getFilterSlots().length - 1].getY() + 17;
    }

    public void drawBackgroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        var i = 0;
        for(FilterSlot<FluidStack> filterSlot : this.filter.getFilterSlots()) {
            if (filterSlot != null) {
                // Draw the slot background
                AssetUtil.drawAsset(guiGraphics, screen, (IAsset) Objects.requireNonNull(provider.getAsset(AssetTypes.SLOT)), guiX + filterSlot.getX(), guiY + filterSlot.getY());

                // Use different colors for different filter types
                Color color;
                switch (filter.getFilterType().getName()) {
                    case "normal":
                        color = new Color(DyeColor.BLUE.getFireworkColor());
                        break;
                    case "regulating":
                        color = new Color(DyeColor.GREEN.getFireworkColor());
                        break;
                    case "exact_count":
                        color = new Color(DyeColor.RED.getFireworkColor());
                        break;
                    case "mod":
                        color = new Color(DyeColor.PURPLE.getFireworkColor());
                        break;
                    case "tag":
                        color = new Color(DyeColor.YELLOW.getFireworkColor());
                        break;
                    default:
                        color = new Color(DyeColor.BLUE.getFireworkColor());
                        break;
                }

                guiGraphics.fill(guiX + filterSlot.getX() + 1, guiY + filterSlot.getY() + 1, guiX + filterSlot.getX() + 17, guiY + filterSlot.getY() + 17, (new Color(color.getRed(), color.getGreen(), color.getBlue(), 128)).getRGB());
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                if (!filterSlot.getFilter().isEmpty()) {
                    // Render fluid
                    FluidStack fluidStack = filterSlot.getFilter();
                    IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
                    int fluidColor = fluidTypeExtensions.getTintColor(fluidStack);

                    // Draw fluid texture
                    ResourceLocation stillTexture = fluidTypeExtensions.getStillTexture(fluidStack);
                    if (stillTexture != null) {
                        AbstractTexture texture = screen.getMinecraft().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS); //getAtlasSprite
                        if (texture instanceof TextureAtlas) {
                            TextureAtlasSprite sprite = ((TextureAtlas) texture).getSprite(stillTexture);
                            if (sprite != null) {
                                RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
                                RenderSystem.setShaderColor(
                                        ((fluidColor >> 16) & 0xFF) / 255F,
                                        ((fluidColor >> 8) & 0xFF) / 255F,
                                        (fluidColor & 0xFF) / 255F,
                                        1.0F
                                );
                                guiGraphics.blit(guiX + filterSlot.getX() + 1, guiY + filterSlot.getY() + 1, 0, 16, 16, sprite);
                                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                            }
                        }
                    }

                    if (filter.getFilterType() == FilterType.EXACT_COUNT || filter.getFilterType() == FilterType.REGULATING){
                        NumberFilterExtra extra = (NumberFilterExtra) filter.getSavedFilters().get(filter.getFilterType().getName());
                        guiGraphics.pose().pushPose();
                        var amount = NumberUtils.getFormatedBigNumber(extra.getExtra().get(i));
                        guiGraphics.pose().translate(guiX + filterSlot.getX() + 17 - Minecraft.getInstance().font.width(amount) / 2D, guiY + filterSlot.getY() +  13, 200.0D);
                        guiGraphics.pose().scale(0.5F, 0.5F, 0.5F);
                        guiGraphics.drawString(Minecraft.getInstance().font, amount, 0,0, 0xFFFFFF);
                        guiGraphics.pose().popPose();
                    }
                    if (filter.getFilterType() == FilterType.TAG){
                        FluidTagFilterExtra extra = (FluidTagFilterExtra) filter.getSavedFilters().get(filter.getFilterType().getName());
                        guiGraphics.pose().pushPose();
                        guiGraphics.enableScissor(guiX + filterSlot.getX() + 2 ,guiY + filterSlot.getY(), guiX + filterSlot.getX() +16 ,guiY + filterSlot.getY()+17);
                        var stackTags = filterSlot.getFilter().getTags().map(TagKey::location).map(ResourceLocation::toString).toList();
                        var text = extra.getExtra().get(i) != null ? extra.getExtra().get(i).location().toString() : (stackTags.isEmpty() ? "" : stackTags.get(0));
                        guiGraphics.pose().translate(guiX + filterSlot.getX() + 1 ,guiY + filterSlot.getY() + 6 , 200.0D);
                        guiGraphics.pose().scale(0.5F, 0.5F, 0.5F);
                        renderScrollingString(guiGraphics, Minecraft.getInstance().font, Component.literal(text), 2  ,0, 30, 32, 0xFFFFFF);
                        guiGraphics.disableScissor();
                        guiGraphics.pose().popPose();
                    }
                }
                ++i;
            }
        }
    }

    public void drawForegroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        var i = 0;
        for(FilterSlot<FluidStack> filterSlot : this.filter.getFilterSlots()) {
            if (filterSlot != null && mouseX > guiX + filterSlot.getX() + 1 && mouseX < guiX + filterSlot.getX() + 16 && mouseY > guiY + filterSlot.getY() + 1 && mouseY < guiY + filterSlot.getY() + 16) {
                guiGraphics.pose().translate(0.0D, 0.0D, 200);
                guiGraphics.fill(filterSlot.getX() + 1, filterSlot.getY() + 1, filterSlot.getX() + 17, filterSlot.getY() + 17, -2130706433);
                if (!filterSlot.getFilter().isEmpty() && Minecraft.getInstance().player.containerMenu.getCarried().isEmpty()) {
                    // Create tooltip for fluid
                    FluidStack fluidStack = filterSlot.getFilter();
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(Component.literal(fluidStack.getFluid().getFluidType().getDescription().getString()));

                    if (filter.getFilterType() == FilterType.EXACT_COUNT || filter.getFilterType() == FilterType.REGULATING){
                        NumberFilterExtra extra = (NumberFilterExtra) filter.getSavedFilters().get(filter.getFilterType().getName());
                        tooltip.add(Component.literal("Amount: " + extra.getExtra().get(i) + " mb").withStyle(ChatFormatting.GRAY));
                    }
                    if (filter.getFilterType() == FilterType.TAG) {
                        FluidTagFilterExtra extra = (FluidTagFilterExtra) filter.getSavedFilters().get(filter.getFilterType().getName());
                        var stackTags = filterSlot.getFilter().getTags().map(TagKey::location).map(ResourceLocation::toString).toList();
                        int finalI = i;
                        ScrollableSelectionHelper selectionHelper = new ScrollableSelectionHelper("", 0, stackTags,
                                () -> extra.getExtra().get(finalI) != null ? extra.getExtra().get(finalI).location().toString() : (stackTags.isEmpty() ? "null" : stackTags.get(0)), true);
                        tooltip.add(Component.literal("Selected Tag: ").withStyle(ChatFormatting.GRAY));
                        if (!stackTags.isEmpty()) {
                            tooltip.addAll(selectionHelper.getFormattedOptions().stream().map(s -> Component.literal(" " + s)).toList());
                        } else {
                            tooltip.add(Component.literal(" None").withStyle(ChatFormatting.GRAY));
                        }
                        tooltip.add(Component.translatable("filter.type.scroll").withStyle(ChatFormatting.DARK_GRAY));
                    }
                    guiGraphics.renderComponentTooltip(screen.getMinecraft().font, tooltip, mouseX - guiX, mouseY - guiY);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                }
                guiGraphics.pose().translate(0.0D, 0.0D, -200);
            }
            ++i;
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof AbstractContainerScreen && ((AbstractContainerScreen)screen).getMenu() instanceof ILocatable) {
            if (!this.isMouseOver(mouseX - (double)((AbstractContainerScreen)screen).getGuiLeft(), mouseY - (double)((AbstractContainerScreen)screen).getGuiTop())) {
                return false;
            }

            ILocatable locatable = (ILocatable)((AbstractContainerScreen)screen).getMenu();

            for(FilterSlot<FluidStack> filterSlot : this.filter.getFilterSlots()) {
                if (filterSlot != null && mouseX > (double)(((AbstractContainerScreen)screen).getGuiLeft() + filterSlot.getX() + 1) && mouseX < (double)(((AbstractContainerScreen)screen).getGuiLeft() + filterSlot.getX() + 16) && mouseY > (double)(((AbstractContainerScreen)screen).getGuiTop() + filterSlot.getY() + 1) && mouseY < (double)(((AbstractContainerScreen)screen).getGuiTop() + filterSlot.getY() + 16)) {
                    CompoundTag compoundNBT = new CompoundTag();
                    compoundNBT.putString("Name", this.filter.getName());
                    compoundNBT.putInt("Slot", filterSlot.getFilterID());

                    // Handle fluid selection from carried item
                    ItemStack carriedItem = Minecraft.getInstance().player.containerMenu.getCarried();
                    if (!carriedItem.isEmpty()) {

                        compoundNBT.put("Filter", carriedItem.saveOptional(screen.getMinecraft().level.registryAccess()));
                    }

                    Titanium.NETWORK.sendToServer(new ButtonClickNetworkMessage(locatable.getLocatorInstance(), -2, compoundNBT));
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof AbstractContainerScreen && ((AbstractContainerScreen)screen).getMenu() instanceof ILocatable) {

            ILocatable locatable = (ILocatable)((AbstractContainerScreen)screen).getMenu();

            var i = 0;
            for(FilterSlot<FluidStack> filterSlot : this.filter.getFilterSlots()) {
                if (filterSlot != null && mouseX > (filterSlot.getX() + 1) && mouseX < (filterSlot.getX() + 16) && mouseY > (filterSlot.getY() + 1) && mouseY <  (filterSlot.getY() + 16)) {
                    if (filter.getFilterType() == FilterType.EXACT_COUNT || filter.getFilterType() == FilterType.REGULATING){
                        CompoundTag compoundNBT = new CompoundTag();
                        compoundNBT.putInt("FilterAmount", i);
                        compoundNBT.putDouble("Scroll", scrollY * (Screen.hasShiftDown() ? 10 : 1) * (Screen.hasControlDown() ? 10 : 1) * (Screen.hasAltDown() ? 10 : 1));

                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.TRIPWIRE_CLICK_ON,2));
                        Titanium.NETWORK.sendToServer(new ButtonClickNetworkMessage(locatable.getLocatorInstance(), -7, compoundNBT));
                        return true;
                    }
                    if (filter.getFilterType() == FilterType.TAG){
                        CompoundTag compoundNBT = new CompoundTag();
                        compoundNBT.putInt("FilterTag", i);
                        compoundNBT.putDouble("Scroll", scrollY);

                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.TRIPWIRE_CLICK_ON,2));
                        Titanium.NETWORK.sendToServer(new ButtonClickNetworkMessage(locatable.getLocatorInstance(), -7, compoundNBT));
                        return true;
                    }
                }
                ++i;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public static void renderScrollingString(GuiGraphics guiGraphics, Font font, Component text, int minX, int minY, int maxX, int maxY, int color) {
        renderScrollingString(guiGraphics, font, text, (minX + maxX) / 2, minX, minY, maxX, maxY, color);
    }

    public static void renderScrollingString(GuiGraphics guiGraphics, Font font, Component text, int centerX, int minX, int minY, int maxX, int maxY, int color) {
        int i = font.width(text);
        int j = (minY + maxY - 9) / 2 + 1;
        int k = maxX - minX;
        if (i > k) {
            int l = i - k;
            double d0 = (double) Util.getMillis() / (double)1000.0F;
            double d1 = Math.max((double)l * (double)0.5F, (double)3.0F);
            double d2 = Math.sin((Math.PI / 2D) * Math.cos((Math.PI * 2D) * d0 / d1)) / (double)2.0F + (double)0.5F;
            double d3 = Mth.lerp(d2, (double)0.0F, (double)l);
            guiGraphics.drawString(font, text, minX - (int)d3, j, color);
        } else {
            int i1 = Mth.clamp(centerX, minX + i / 2, maxX - i / 2);
            guiGraphics.drawCenteredString(font, text, i1, j, color);
        }
    }
}
