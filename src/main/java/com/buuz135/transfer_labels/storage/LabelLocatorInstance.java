package com.buuz135.transfer_labels.storage;

import com.buuz135.transfer_labels.storage.client.LabelClientStorage;
import com.hrznstudio.titanium.network.locator.LocatorFactory;
import com.hrznstudio.titanium.network.locator.LocatorInstance;
import com.hrznstudio.titanium.network.locator.LocatorType;
import com.hrznstudio.titanium.network.locator.instance.TileEntityLocatorInstance;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class LabelLocatorInstance extends LocatorInstance {

    public static final LocatorType LABEL = new LocatorType("transfer_label", LabelLocatorInstance::new);

    private BlockPos pos;
    private Direction direction;

    public LabelLocatorInstance(BlockPos pos, Direction direction) {
        super(LABEL);
        this.pos = pos;
        this.direction = direction;
    }

    public LabelLocatorInstance() {
        super(LABEL);
    }

    @Override
    public Optional<?> locale(Player player) {
        var level = player.getCommandSenderWorld();
        if (level instanceof ServerLevel serverLevel){
            var storage = LabelStorage.getStorageFor(serverLevel).getLabelBlocksMap();
            if (storage.containsKey(this.pos) && storage.get(this.pos).getLabels().containsKey(this.direction)){
                return Optional.of(storage.get(this.pos).getLabels().get(this.direction));
            }
        } else if (level instanceof ClientLevel clientLevel) {
            var storage = LabelClientStorage.getStorage(clientLevel).getLabelBlocksMap();
            if (storage.containsKey(this.pos) && storage.get(this.pos).getLabels().containsKey(this.direction)){
                return Optional.of(storage.get(this.pos).getLabels().get(this.direction));
            }
        }
        return Optional.empty();
    }

    public ContainerLevelAccess getWorldPosCallable(Level world) {
        return ContainerLevelAccess.create(world, this.pos);
    }
}
