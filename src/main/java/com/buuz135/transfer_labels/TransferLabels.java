package com.buuz135.transfer_labels;

import com.buuz135.transfer_labels.client.LabelClientEvents;
import com.buuz135.transfer_labels.data.TransferLabelItemModelProvider;
import com.buuz135.transfer_labels.data.TransferLabelLangItemProvider;
import com.buuz135.transfer_labels.data.TransferLabelRecipesProvider;
import com.buuz135.transfer_labels.item.FluidStackTransferLabelItem;
import com.buuz135.transfer_labels.item.ItemStackTransferLabelItem;
import com.buuz135.transfer_labels.item.TransferLabelItem;
import com.buuz135.transfer_labels.packet.LabelSyncPacket;
import com.buuz135.transfer_labels.packet.SingleLabelSyncPacket;
import com.buuz135.transfer_labels.storage.LabelLocatorInstance;
import com.hrznstudio.titanium.event.handler.EventManager;
import com.hrznstudio.titanium.module.ModuleController;
import com.hrznstudio.titanium.network.CompoundSerializableDataHandler;
import com.hrznstudio.titanium.network.NetworkHandler;
import com.hrznstudio.titanium.network.locator.LocatorFactory;
import com.hrznstudio.titanium.tab.TitaniumTab;
import com.mojang.logging.LogUtils;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slf4j.Logger;


@Mod(TransferLabels.MODID)
public class TransferLabels extends ModuleController {

    public static final String MODID = "transfer_labels";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static TitaniumTab TAB = new TitaniumTab(ResourceLocation.fromNamespaceAndPath(MODID, "main"));
    public static NetworkHandler NETWORK = new NetworkHandler(MODID);

    static {
        NETWORK.registerMessage("label_sync_packet", LabelSyncPacket.class);
        NETWORK.registerMessage("single_label_sync_packet", SingleLabelSyncPacket.class);
        LocatorFactory.registerLocatorType(LabelLocatorInstance.LABEL);
    }

    public static DeferredHolder<Item, Item> ITEMSTACK_INSERT_LABEL;
    public static DeferredHolder<Item, Item> ITEMSTACK_EXTRACT_LABEL;
    public static DeferredHolder<Item, Item> FLUIDSTACK_INSERT_LABEL;
    public static DeferredHolder<Item, Item> FLUIDSTACK_EXTRACT_LABEL;
    public static DeferredHolder<Item, Item> LABEL_ACCESSOR;

    public TransferLabels(Dist dist, IEventBus modEventBus, ModContainer modContainer) {
        super(modContainer);

        if (dist.isClient()) NeoForge.EVENT_BUS.register(new LabelClientEvents());
        NeoForge.EVENT_BUS.register(new LabelInteractEvents());

        EventManager.mod(GatherDataEvent.class).process(this::dataGen).subscribe();

        CompoundSerializableDataHandler.map(Direction.class, Direction.STREAM_CODEC);
    }

    @Override
    protected void initModules() {
        this.addCreativeTab("main", () -> new ItemStack(ITEMSTACK_EXTRACT_LABEL.get()), "transfer_labels", TAB);

        ITEMSTACK_INSERT_LABEL = getRegistries().registerGeneric(Registries.ITEM, "itemstack_insert_transfer_label", () ->  new ItemStackTransferLabelItem(TransferLabelItem.Mode.INSERT));
        ITEMSTACK_EXTRACT_LABEL = getRegistries().registerGeneric(Registries.ITEM, "itemstack_extract_transfer_label", () ->  new ItemStackTransferLabelItem(TransferLabelItem.Mode.EXTRACT));
        FLUIDSTACK_INSERT_LABEL = getRegistries().registerGeneric(Registries.ITEM, "fluidstack_insert_transfer_label", () ->  new FluidStackTransferLabelItem(TransferLabelItem.Mode.INSERT));
        FLUIDSTACK_EXTRACT_LABEL = getRegistries().registerGeneric(Registries.ITEM, "fluidstack_extract_transfer_label", () ->  new FluidStackTransferLabelItem(TransferLabelItem.Mode.EXTRACT));
        LABEL_ACCESSOR = getRegistries().registerGeneric(Registries.ITEM, "label_accessor", () -> {
            var item = new Item(new Item.Properties().stacksTo(1));
            TAB.getTabList().add(item);
            return item;
        });
    }

    public void dataGen(GatherDataEvent event) {
        event.getGenerator().addProvider(true, new TransferLabelLangItemProvider(event.getGenerator(), MODID, "en_us"));
        event.getGenerator().addProvider(true, new TransferLabelItemModelProvider(event.getGenerator(), MODID, event.getExistingFileHelper()));
        event.getGenerator().addProvider(true, new TransferLabelRecipesProvider(event.getGenerator(), event.getLookupProvider()));
    }
}
