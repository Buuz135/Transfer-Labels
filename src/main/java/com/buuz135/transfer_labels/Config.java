package com.buuz135.transfer_labels;


import com.hrznstudio.titanium.annotation.config.ConfigFile;
import com.hrznstudio.titanium.annotation.config.ConfigVal;

@ConfigFile
public class Config {

    @ConfigVal(comment = "How many amount upgrades a transfer label can accept.")
    @ConfigVal.InRangeInt(min = 0, max = 63)
    public static int amountUpgrades = 63;

    @ConfigVal(comment = "How many speed upgrades a transfer label can accept.")
    @ConfigVal.InRangeInt(min = 0, max = 38)
    public static int speedUpgrades = 38;

    @ConfigVal(comment = "Base item amount a transfer label moves before amount upgrades are applied.")
    @ConfigVal.InRangeInt(min = 0)
    public static int baseItemTransferAmount = 1;

    @ConfigVal(comment = "How many millibuckets a fluid transfer label moves per item transfer amount.")
    @ConfigVal.InRangeInt(min = 0)
    public static int fluidTransferMultiplier = 100;

    @ConfigVal(comment = "How many filter slots transfer labels have.")
    @ConfigVal.InRangeInt(min = 1, max = 20)
    public static int filterSlots = 20;

}
