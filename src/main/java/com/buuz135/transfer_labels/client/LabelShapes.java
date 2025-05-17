package com.buuz135.transfer_labels.client;

import net.minecraft.world.phys.AABB;

public class LabelShapes {

    public static AABB NORTH = new AABB(0.0D, 0.0D, -0.005D, 1.0D, 1.0D, 0.005D);
    public static AABB SOUTH = new AABB(0.0D, 0.0D, 0.995D, 1.0D, 1.0D, 1.005D);
    public static AABB EAST = new AABB(0.995D, 0.0D, 0.0D, 1.005D, 1.0D, 1.0D);
    public static AABB WEST = new AABB(-0.005D, 0.0D, 0.0D, 0.005D, 1.0D, 1.0D);
    public static AABB UP = new AABB(0.0D, 0.995D, 0.0D, 1.0D, 1.005D, 1.0D);
    public static AABB DOWN = new AABB(0.0D, -0.005D, 0.0D, 1.0D, 0.005D, 1.0D);

}
