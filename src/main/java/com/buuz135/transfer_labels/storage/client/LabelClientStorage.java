package com.buuz135.transfer_labels.storage.client;

import com.buuz135.transfer_labels.client.DistanceRayTraceResult;
import com.buuz135.transfer_labels.client.LabelShapes;
import com.buuz135.transfer_labels.storage.LabelBlock;
import com.buuz135.transfer_labels.storage.LabelStorage;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class LabelClientStorage {

    public static LabelStorage LABELS;

    public static List<LabelBlock> getLabelBlocks(ClientLevel clientLevel) {
        return getStorage(clientLevel).getLabelBlocks();
    }

    public static LabelStorage getStorage(ClientLevel clientLevel) {
        if (LABELS == null || !LABELS.getLevel().equals(clientLevel)) {
            LABELS = new LabelStorage(clientLevel);
        }
        return LABELS;
    }

    public static List<LabelBlock> getNearbyLabels(ClientLevel level, BlockPos pos, int distance) {
        return getLabelBlocks(level).stream().filter(labelBlock -> labelBlock.getPos().distSqr(pos) <= distance).collect(Collectors.toList());
    }

    public static List<VoxelShape> collectShapes(List<LabelBlock> labelBlocks) {
        List<VoxelShape> shapes = new ArrayList<>();
        labelBlocks.forEach(labelBlock -> {
            labelBlock.getLabels().forEach((direction, itemStack) -> {
                AABB aabb = null;
                if (direction == Direction.NORTH) aabb = LabelShapes.NORTH;
                else if (direction == Direction.SOUTH) aabb = LabelShapes.SOUTH;
                else if (direction == Direction.EAST) aabb = LabelShapes.EAST;
                else if (direction == Direction.WEST) aabb = LabelShapes.WEST;
                else if (direction == Direction.UP) aabb = LabelShapes.UP;
                else if (direction == Direction.DOWN) aabb = LabelShapes.DOWN;

                if (aabb != null) {
                    shapes.add(Shapes.create(aabb.move(labelBlock.getPos())));
                }
            });
        });
        return shapes;
    }

    @Nullable
    public static HitResult rayTraceBoxesClosest(Vec3 start, Vec3 end, BlockPos pos, List<VoxelShape> boxes) {
        List<DistanceRayTraceResult> results = new ArrayList<>();
        for (VoxelShape box : boxes) {
            DistanceRayTraceResult hit = rayTraceBox(pos, start, end, box);
            if (hit != null)
                results.add(hit);
        }
        HitResult closestHit = null;
        double curClosest = Double.MAX_VALUE;
        for (DistanceRayTraceResult hit : results) {
            if (curClosest > hit.getDistance()) {
                closestHit = hit;
                curClosest = hit.getDistance();
            }
        }
        return closestHit;
    }


    @Nullable
    protected static DistanceRayTraceResult rayTraceBox(BlockPos pos, Vec3 start, Vec3 end, VoxelShape shape) {
        BlockHitResult bbResult = shape.clip(start, end, pos);
        if (bbResult != null) {
            Vec3 hitVec = bbResult.getLocation();
            Direction sideHit = bbResult.getDirection();
            double dist = start.distanceTo(hitVec);
            return new DistanceRayTraceResult(hitVec, sideHit, pos, shape, dist);
        }
        return null;
    }

}
