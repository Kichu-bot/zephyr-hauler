package com.zephyrhauler.block.entity;

import com.zephyrhauler.block.ZephyrCableBlock;
import com.zephyrhauler.block.ZephyrDockBlock;
import com.zephyrhauler.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ZephyrHubBlockEntity extends BlockEntity implements GeoBlockEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final List<BlockPos> connectedDocks = new ArrayList<>();
    private UUID hubId;
    private String customName = "Zephyr Hub";

    public ZephyrHubBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ZEPHYR_HUB_BE.get(), pos, state);
        this.hubId = UUID.randomUUID();
    }

    public List<BlockPos> getConnectedDocks() { return connectedDocks; }
    public UUID getHubId() { return this.hubId; }
    public String getCustomName() { return this.customName; }
    public void setCustomName(String name) { this.customName = name; setChanged(); }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public BlockPos requestAvailableDock() {
        for (BlockPos dPos : this.connectedDocks) {
            BlockState state = this.level.getBlockState(dPos);
            if (state.getBlock() instanceof ZephyrDockBlock) {
                BlockEntity be = this.level.getBlockEntity(dPos);
                if (be instanceof ZephyrDockBlockEntity dockBE) {

                    boolean obstructed = com.zephyrhauler.entity.ZephyrHaulerEntity.isAirspaceObstructed(this.level, dPos);
                    boolean physicallyOccupied = !this.level.getEntitiesOfClass(
                            com.zephyrhauler.entity.ZephyrHaulerEntity.class,
                            new net.minecraft.world.phys.AABB(dPos.above()).inflate(0.1)
                    ).isEmpty();

                    boolean isReserved = state.hasProperty(ZephyrDockBlock.RESERVED) && state.getValue(ZephyrDockBlock.RESERVED);

                    if (!dockBE.isOccupied() && !physicallyOccupied && !isReserved && !obstructed) {

                        if (state.hasProperty(ZephyrDockBlock.RESERVED)) {
                            this.level.setBlock(dPos, state.setValue(ZephyrDockBlock.RESERVED, true), 3);
                        }

                        dockBE.setChanged();
                        this.level.sendBlockUpdated(dPos, state, this.level.getBlockState(dPos), 3);

                        return dPos;
                    }
                }
            }
        }
        return null;
    }

    public void scanNetwork() {
        if (this.level == null || this.level.isClientSide()) return;

        List<BlockPos> newDocks = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        for (BlockPos oldPos : this.connectedDocks) {
            BlockState oldState = this.level.getBlockState(oldPos);
            if (oldState.getBlock() instanceof ZephyrDockBlock && oldState.hasProperty(ZephyrDockBlock.HUB_MODE)) {
                if (oldState.getValue(ZephyrDockBlock.HUB_MODE)) {
                    this.level.setBlock(oldPos, oldState.setValue(ZephyrDockBlock.HUB_MODE, false), 3);
                }
            }
        }

        queue.add(this.getBlockPos());
        visited.add(this.getBlockPos());

        int maxBlocksToScan = 256;
        int scanned = 0;

        while (!queue.isEmpty() && scanned < maxBlocksToScan) {
            BlockPos current = queue.poll();
            scanned++;

            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = current.relative(dir);

                if (!visited.contains(neighborPos)) {
                    BlockState state = this.level.getBlockState(neighborPos);
                    Block block = state.getBlock();

                    if (block instanceof ZephyrCableBlock || block instanceof ZephyrDockBlock) {
                        visited.add(neighborPos);
                        queue.add(neighborPos);

                        if (block instanceof ZephyrDockBlock) {
                            newDocks.add(neighborPos);

                            if (state.hasProperty(ZephyrDockBlock.HUB_MODE) && !state.getValue(ZephyrDockBlock.HUB_MODE)) {
                                this.level.setBlock(neighborPos, state.setValue(ZephyrDockBlock.HUB_MODE, true), 3);
                            }
                        }
                    }
                }
            }
        }

        this.connectedDocks.clear();
        this.connectedDocks.addAll(newDocks);
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.hubId != null) tag.putUUID("HubId", this.hubId);
        tag.putString("CustomName", this.customName);

        long[] docksArray = new long[this.connectedDocks.size()];
        for (int i = 0; i < this.connectedDocks.size(); i++) {
            docksArray[i] = this.connectedDocks.get(i).asLong();
        }
        tag.putLongArray("ConnectedDocks", docksArray);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("HubId")) this.hubId = tag.getUUID("HubId");
        if (tag.contains("CustomName")) this.customName = tag.getString("CustomName");

        if (tag.contains("ConnectedDocks")) {
            this.connectedDocks.clear();
            for (long posLong : tag.getLongArray("ConnectedDocks")) {
                this.connectedDocks.add(BlockPos.of(posLong));
            }
        }
    }
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}