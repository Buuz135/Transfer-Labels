package com.buuz135.transfer_labels.container;

import com.hrznstudio.titanium.Titanium;
import com.hrznstudio.titanium.container.BasicAddonContainer;
import com.hrznstudio.titanium.network.locator.LocatorFactory;
import com.hrznstudio.titanium.network.locator.LocatorInstance;
import com.hrznstudio.titanium.network.locator.instance.EmptyLocatorInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

public class TransferLabelBasicAddonContainer extends BasicAddonContainer {

    public static DeferredHolder<MenuType<?>, MenuType<?>> LABEL_TYPE;

    public TransferLabelBasicAddonContainer(Object provider, LocatorInstance locatorInstance, ContainerLevelAccess worldPosCallable, Inventory playerInventory, int containerId) {
        this(provider, locatorInstance, (MenuType<BasicAddonContainer>) LABEL_TYPE.get(), worldPosCallable, playerInventory, containerId);
    }

    public TransferLabelBasicAddonContainer(Object provider, LocatorInstance locatorInstance, MenuType<BasicAddonContainer> containerType, ContainerLevelAccess worldPosCallable, Inventory playerInventory, int containerId) {
        super(provider, locatorInstance, containerType, worldPosCallable, playerInventory, containerId);
    }

    public static TransferLabelBasicAddonContainer create(int id, Inventory inventory, RegistryFriendlyByteBuf packetBuffer) {
        LocatorInstance instance = LocatorFactory.readPacketBuffer(packetBuffer);
        if (instance != null) {
            Player playerEntity = inventory.player;
            Level world = playerEntity.getCommandSenderWorld();
            TransferLabelBasicAddonContainer container = instance.locale(playerEntity).map((located) -> new TransferLabelBasicAddonContainer(located, instance, instance.getWorldPosCallable(world), inventory, id)).orElse(null);
            if (container != null) {
                return container;
            }
        }

        Titanium.LOGGER.error("Failed to find locate instance to create Container for");
        return new TransferLabelBasicAddonContainer(new Object(), new EmptyLocatorInstance(), ContainerLevelAccess.NULL, inventory, id);
    }
}
