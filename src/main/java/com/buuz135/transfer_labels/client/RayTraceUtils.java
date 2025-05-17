/*
 * This file is part of Titanium
 * Copyright (C) 2025, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.buuz135.transfer_labels.client;

import com.buuz135.transfer_labels.storage.LabelBlock;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

public class RayTraceUtils {

    public static HitResult rayTraceSimple(Level world, LivingEntity living, double blockReachDistance, float partialTicks) {
        Vec3 vec3d = living.getEyePosition(partialTicks);
        Vec3 vec3d1 = living.getViewVector(partialTicks);
        Vec3 vec3d2 = vec3d.add(vec3d1.x * blockReachDistance, vec3d1.y * blockReachDistance, vec3d1.z * blockReachDistance);
        return world.clip(new ClipContext(vec3d, vec3d2, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, living));
    }

    @Nullable
    public static Pair<LabelBlock, Direction> rayTraceVoxelShape(List<LabelBlock> nearbyLabels, Level world, LivingEntity living, float partialTicks, BlockPos pos) {
            //var original = rayTraceSimple(world, living, blockReachDistance, partialTicks);
            var blockReachDistance = living.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).getValue();
            Vec3 vec3d = living.getEyePosition(partialTicks);
            Vec3 vec3d1 = living.getViewVector(partialTicks);
            Vec3 vec3d2 = vec3d.add(vec3d1.x * blockReachDistance, vec3d1.y * blockReachDistance, vec3d1.z * blockReachDistance);
            LabelBlock closest = null;
            Direction direction = null;
            double distance = Double.MAX_VALUE;
            if (pos != null) {
                var defaultShape = world.getBlockState(pos).getShape(world, pos);
                var result = defaultShape.clip(vec3d, vec3d2, pos);
                if (result != null) distance = vec3d.distanceTo(result.getLocation());
            }
            for (LabelBlock nearbyLabel : nearbyLabels) {
                for (Direction direction2 : Direction.values()) {
                    var voxelShape = nearbyLabel.collectShapes(direction2);
                    if (voxelShape != null){
                        var result = voxelShape.clip(vec3d, vec3d2);
                        if (result.isPresent() && vec3d.distanceTo(result.get()) < distance) {
                            closest = nearbyLabel;
                            direction = direction2;
                            distance = vec3d.distanceTo(result.get());
                        }
                    }
                }
            }
            if (closest != null) {
                return Pair.of(closest, direction);
            }
            return null;
    }
}
