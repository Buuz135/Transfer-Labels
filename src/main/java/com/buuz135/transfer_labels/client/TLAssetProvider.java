package com.buuz135.transfer_labels.client;

import com.buuz135.transfer_labels.TransferLabels;
import com.hrznstudio.titanium.api.client.IAsset;
import com.hrznstudio.titanium.api.client.IAssetType;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class TLAssetProvider implements IAssetProvider {

    public static final TLAssetProvider DEFAULT_PROVIDER = new TLAssetProvider();

    // Filter types
    public static final IAsset FILTER_NORMAL = new IAsset() {
        @Override
        public Rectangle getArea() {
            return  new Rectangle(40,0, 20, 20);
        }

        @Override
        public ResourceLocation getResourceLocation() {
            return ResourceLocation.fromNamespaceAndPath(TransferLabels.MODID, "textures/gui/textures.png" );
        }
    };
    public static final IAsset FILTER_REGULATING = new IAsset() {
        @Override
        public Rectangle getArea() {
            return  new Rectangle(60,0, 20, 20);
        }

        @Override
        public ResourceLocation getResourceLocation() {
            return ResourceLocation.fromNamespaceAndPath(TransferLabels.MODID, "textures/gui/textures.png" );
        }
    };
    public static final IAsset FILTER_EXACT_COUNT = new IAsset() {
        @Override
        public Rectangle getArea() {
            return  new Rectangle(80,0, 20, 20);
        }

        @Override
        public ResourceLocation getResourceLocation() {
            return ResourceLocation.fromNamespaceAndPath(TransferLabels.MODID, "textures/gui/textures.png" );
        }
    };
    public static final IAsset FILTER_MOD = new IAsset() {
        @Override
        public Rectangle getArea() {
            return  new Rectangle(100,0, 20, 20);
        }

        @Override
        public ResourceLocation getResourceLocation() {
            return ResourceLocation.fromNamespaceAndPath(TransferLabels.MODID, "textures/gui/textures.png" );
        }
    };
    public static final IAsset FILTER_TAG = new IAsset() {
        @Override
        public Rectangle getArea() {
            return  new Rectangle(120,0, 20, 20);
        }

        @Override
        public ResourceLocation getResourceLocation() {
            return ResourceLocation.fromNamespaceAndPath(TransferLabels.MODID, "textures/gui/textures.png" );
        }
    };

    // Whitelist/Blacklist buttons
    public static final IAsset WHITELIST_BUTTON = new IAsset() {
        @Override
        public Rectangle getArea() {
            return  new Rectangle(0,0, 20, 20);
        }

        @Override
        public ResourceLocation getResourceLocation() {
            return ResourceLocation.fromNamespaceAndPath(TransferLabels.MODID, "textures/gui/textures.png");
        }
    };
    public static final IAsset BLACKLIST_BUTTON = new IAsset() {
        @Override
        public Rectangle getArea() {
            return  new Rectangle(20,0, 20, 20);
        }

        @Override
        public ResourceLocation getResourceLocation() {
            return ResourceLocation.fromNamespaceAndPath(TransferLabels.MODID, "textures/gui/textures.png" );
        }
    };

    @Override
    public @Nullable <T extends IAsset> T getAsset(IAssetType<T> iAssetType) {
        if (iAssetType == TLAssetTypes.FILTER_NORMAL) {
            return iAssetType.castOrDefault(FILTER_NORMAL);
        } else if (iAssetType == TLAssetTypes.FILTER_REGULATING) {
            return iAssetType.castOrDefault(FILTER_REGULATING);
        } else if (iAssetType == TLAssetTypes.FILTER_EXACT_COUNT) {
            return iAssetType.castOrDefault(FILTER_EXACT_COUNT);
        } else if (iAssetType == TLAssetTypes.FILTER_MOD) {
            return iAssetType.castOrDefault(FILTER_MOD);
        } else if (iAssetType == TLAssetTypes.FILTER_TAG) {
            return iAssetType.castOrDefault(FILTER_TAG);
        } else if (iAssetType == TLAssetTypes.WHITELIST_BUTTON) {
            return iAssetType.castOrDefault(WHITELIST_BUTTON);
        } else if (iAssetType == TLAssetTypes.BLACKLIST_BUTTON) {
            return iAssetType.castOrDefault(BLACKLIST_BUTTON);
        }
        return IAssetProvider.DEFAULT_PROVIDER.getAsset(iAssetType);
    }
}
