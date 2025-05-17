package com.buuz135.transfer_labels.client;

import com.buuz135.transfer_labels.TransferLabels;
import com.hrznstudio.titanium.api.client.AssetTypes;
import com.hrznstudio.titanium.api.client.GenericAssetType;
import com.hrznstudio.titanium.api.client.IAsset;
import com.hrznstudio.titanium.api.client.IAssetType;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

/**
 * Asset types for Transfer Labels mod
 */
public class TLAssetTypes {

    // Filter types
    public static final IAssetType<IAsset> FILTER_NORMAL = new GenericAssetType<>(TLAssetProvider.DEFAULT_PROVIDER::getAsset, IAsset.class);
    public static final IAssetType<IAsset> FILTER_REGULATING = new GenericAssetType<>(TLAssetProvider.DEFAULT_PROVIDER::getAsset, IAsset.class);
    public static final IAssetType<IAsset> FILTER_EXACT_COUNT = new GenericAssetType<>(TLAssetProvider.DEFAULT_PROVIDER::getAsset, IAsset.class);
    public static final IAssetType<IAsset> FILTER_MOD = new GenericAssetType<>(TLAssetProvider.DEFAULT_PROVIDER::getAsset, IAsset.class);
    public static final IAssetType<IAsset> FILTER_TAG = new GenericAssetType<>(TLAssetProvider.DEFAULT_PROVIDER::getAsset, IAsset.class);

    // Whitelist/Blacklist buttons
    public static final IAssetType<IAsset> WHITELIST_BUTTON = new GenericAssetType<>(TLAssetProvider.DEFAULT_PROVIDER::getAsset, IAsset.class);
    public static final IAssetType<IAsset> BLACKLIST_BUTTON = new GenericAssetType<>(TLAssetProvider.DEFAULT_PROVIDER::getAsset, IAsset.class);

}
