package com.zephyrhauler.registry;

import com.zephyrhauler.block.entity.ZephyrDockBlockEntity;
import com.zephyrhauler.component.ZephyrDataComponents;
import com.zephyrhauler.item.WindSensorItem;
import com.zephyrhauler.item.ZephyrHaulerItem;
import com.zephyrhauler.network.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.UUID;

@EventBusSubscriber(modid = "zephyr_hauler")
public class ModNetwork {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("zephyr_hauler");

        registrar.playToServer(
                SaveDockNamePayload.TYPE,
                SaveDockNamePayload.CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        Player player = context.player();
                        if (player == null) return;
                        BlockEntity be = player.level().getBlockEntity(payload.pos());

                        if (be instanceof com.zephyrhauler.block.entity.ZephyrDockBlockEntity dockBE) {
                            dockBE.setCustomName(payload.name());
                            dockBE.setChanged();
                            player.level().sendBlockUpdated(payload.pos(), dockBE.getBlockState(), dockBE.getBlockState(), 3);
                        } else if (be instanceof com.zephyrhauler.block.entity.ZephyrHubBlockEntity hubBE) {
                            hubBE.setCustomName(payload.name());
                            hubBE.setChanged();
                            player.level().sendBlockUpdated(payload.pos(), hubBE.getBlockState(), hubBE.getBlockState(), 3);
                        }
                    });
                }
        );

        registrar.playToServer(
                ControllerActionPayload.TYPE,
                ControllerActionPayload.CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        Player player = context.player();
                        if (player == null || !(player.containerMenu instanceof com.zephyrhauler.menu.ZephyrControllerMenu controllerMenu)) return;

                        ServerLevel targetLevel = player.getServer().getLevel(payload.dockPos().dimension());
                        if (targetLevel == null) return;

                        targetLevel.getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.POST_TELEPORT, new ChunkPos(payload.dockPos().pos()), 3, player.getId());
                        BlockEntity be = targetLevel.getBlockEntity(payload.dockPos().pos());

                        ItemStack targetStack = controllerMenu.haulerContainer.getItem(0);

                        if (be instanceof ZephyrDockBlockEntity dockBE) {
                            if (!dockBE.getDockId().equals(payload.dockId())) {
                                PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(2, "message.zephyr_hauler.controller.error_dock_replaced"));
                                return;
                            }
                            if (payload.action() == 0) {
                                if (targetStack.isEmpty()) {
                                    PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(-1, "message.zephyr_hauler.controller.error_no_hauler"));
                                    return;
                                }
                                if (targetStack.getItem() instanceof ZephyrHaulerItem) {
                                    if (targetStack.has(ZephyrDataComponents.TARGET_POS.get())) {
                                        PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(-1, "message.zephyr_hauler.controller.error_already_linked"));
                                        return;
                                    }
                                    if (dockBE.getLinkId() != null || dockBE.isOccupied()) {
                                        PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(1, "message.zephyr_hauler.controller.error_dock_busy"));
                                        return;
                                    }
                                    UUID newLinkId = UUID.randomUUID();
                                    dockBE.setLinkId(newLinkId);
                                    dockBE.setChanged();
                                    targetStack.set(ZephyrDataComponents.TARGET_POS.get(), payload.dockPos());
                                    targetStack.set(ZephyrDataComponents.TARGET_NAME.get(), payload.dockName());
                                    targetStack.set(ZephyrDataComponents.LINK_ID.get(), newLinkId);
                                    PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(1, "message.zephyr_hauler.controller.success_linked"));
                                } else if (targetStack.getItem() instanceof WindSensorItem) {
                                    CustomData customData = targetStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                                    CompoundTag tag = customData.copyTag();
                                    tag.putLong("LinkedDockPos", payload.dockPos().pos().asLong());
                                    tag.putString("LinkedDockName", payload.dockName());
                                    tag.putString("LinkedDockDim", payload.dockPos().dimension().location().toString());
                                    targetStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                                    PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(-1, "message.zephyr_hauler.controller.success_linked"));
                                }
                            } else if (payload.action() == 1) { // DESVINCULAR
                                dockBE.setOccupied(false);
                                dockBE.setPendingDeliveryData(null);
                                dockBE.setLinkId(null);
                                dockBE.setChanged();
                                if (!targetStack.isEmpty()) {
                                    if (targetStack.getItem() instanceof ZephyrHaulerItem) {
                                        targetStack.remove(ZephyrDataComponents.TARGET_POS.get());
                                        targetStack.remove(ZephyrDataComponents.TARGET_NAME.get());
                                        targetStack.remove(ZephyrDataComponents.LINK_ID.get());
                                    } else if (targetStack.getItem() instanceof WindSensorItem) {
                                        CustomData customData = targetStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                                        CompoundTag tag = customData.copyTag();
                                        tag.remove("LinkedDockPos");
                                        tag.remove("LinkedDockName");
                                        tag.remove("LinkedDockDim");
                                        if (tag.isEmpty()) targetStack.remove(DataComponents.CUSTOM_DATA);
                                        else targetStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                                    }
                                }
                                PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(0, "message.zephyr_hauler.controller.success_reset"));
                            }
                        }
                        else if (be instanceof com.zephyrhauler.block.entity.ZephyrHubBlockEntity hubBE) {
                            if (payload.action() == 0) {
                                if (targetStack.isEmpty()) {
                                    PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(-1, "message.zephyr_hauler.controller.error_no_hauler"));
                                    return;
                                }
                                if (targetStack.getItem() instanceof ZephyrHaulerItem) {
                                    targetStack.set(ZephyrDataComponents.TARGET_POS.get(), payload.dockPos());
                                    targetStack.set(ZephyrDataComponents.TARGET_NAME.get(), payload.dockName());
                                    targetStack.set(ZephyrDataComponents.LINK_ID.get(), hubBE.getHubId());
                                    PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(-1, "message.zephyr_hauler.controller.success_linked"));
                                }
                            } else if (payload.action() == 1) {
                                if (!targetStack.isEmpty() && targetStack.getItem() instanceof ZephyrHaulerItem) {
                                    targetStack.remove(ZephyrDataComponents.TARGET_POS.get());
                                    targetStack.remove(ZephyrDataComponents.TARGET_NAME.get());
                                    targetStack.remove(ZephyrDataComponents.LINK_ID.get());
                                }
                                PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(-1, "message.zephyr_hauler.controller.success_reset"));
                            }
                        } else {
                            PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(2, "message.zephyr_hauler.controller.error_not_found"));
                        }
                    });
                }
        );

        registrar.playToServer(
                DockStatusRequestPayload.TYPE,
                DockStatusRequestPayload.CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        Player player = context.player();
                        if (player == null) return;

                        ServerLevel targetLevel = player.getServer().getLevel(payload.pos().dimension());
                        if (targetLevel == null) {
                            PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(3, ""));
                            return;
                        }

                        targetLevel.getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.POST_TELEPORT, new ChunkPos(payload.pos().pos()), 3, player.getId());
                        BlockEntity be = targetLevel.getBlockEntity(payload.pos().pos());

                        if (be instanceof ZephyrDockBlockEntity dockBE && dockBE.getDockId().equals(payload.dockId())) {

                            if (com.zephyrhauler.entity.ZephyrHaulerEntity.isAirspaceObstructed(targetLevel, payload.pos().pos())) {
                                PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(3, ""));
                                return;
                            }

                            boolean physicallyOccupied = !targetLevel.getEntitiesOfClass(
                                    com.zephyrhauler.entity.ZephyrHaulerEntity.class,
                                    new net.minecraft.world.phys.AABB(payload.pos().pos().above()).inflate(1.0, 0.0, 1.0)
                            ).isEmpty();

                            int status = 0;

                            if (dockBE.isOccupied() || physicallyOccupied) {
                                status = 2;
                            } else {
                                BlockState state = targetLevel.getBlockState(payload.pos().pos());
                                boolean isReservedByHub = state.hasProperty(com.zephyrhauler.block.ZephyrDockBlock.RESERVED) && state.getValue(com.zephyrhauler.block.ZephyrDockBlock.RESERVED);

                                if (isReservedByHub || dockBE.getLinkId() != null) {
                                    status = 1;
                                }
                            }

                            PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(status, ""));
                        } else {
                            PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(3, ""));
                        }
                    });
                }
        );

        registrar.playToClient(
                DockStatusResponsePayload.TYPE,
                DockStatusResponsePayload.CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        if (net.minecraft.client.Minecraft.getInstance().screen instanceof com.zephyrhauler.client.gui.screen.ZephyrControllerScreen screen) {
                            screen.receiveStatusUpdate(payload.status(), payload.message());
                        }
                    });
                }
        );

        registrar.playToServer(
                RemoveDockPayload.TYPE,
                RemoveDockPayload.CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        Player player = context.player();
                        if (player == null) return;
                        ItemStack controllerStack = player.getMainHandItem();
                        if (!(controllerStack.getItem() instanceof com.zephyrhauler.item.ZephyrControllerItem)) {
                            controllerStack = player.getOffhandItem();
                        }
                        if (controllerStack.getItem() instanceof com.zephyrhauler.item.ZephyrControllerItem) {
                            java.util.List<com.zephyrhauler.component.DockEntry> docks = new java.util.ArrayList<>(
                                    controllerStack.getOrDefault(com.zephyrhauler.component.ZephyrDataComponents.SAVED_DOCKS.get(), java.util.List.of())
                            );
                            if (payload.index() >= 0 && payload.index() < docks.size()) {
                                docks.remove(payload.index());
                                controllerStack.set(com.zephyrhauler.component.ZephyrDataComponents.SAVED_DOCKS.get(), docks);
                            }
                        }
                    });
                }
        );

        registrar.playToServer(
                RefreshDocksPayload.TYPE,
                RefreshDocksPayload.CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        net.minecraft.world.entity.player.Player player = context.player();
                        if (player == null) return;
                        net.minecraft.world.item.ItemStack controllerStack = player.getMainHandItem();
                        if (!(controllerStack.getItem() instanceof com.zephyrhauler.item.ZephyrControllerItem)) {
                            controllerStack = player.getOffhandItem();
                        }
                        if (controllerStack.getItem() instanceof com.zephyrhauler.item.ZephyrControllerItem) {
                            java.util.List<com.zephyrhauler.component.DockEntry> docks = new java.util.ArrayList<>(
                                    controllerStack.getOrDefault(com.zephyrhauler.component.ZephyrDataComponents.SAVED_DOCKS.get(), java.util.List.of())
                            );
                            boolean updated = false;

                            for (int i = 0; i < docks.size(); i++) {
                                com.zephyrhauler.component.DockEntry entry = docks.get(i);
                                net.minecraft.server.level.ServerLevel targetLevel = player.getServer().getLevel(entry.pos().dimension());

                                if (targetLevel != null) {
                                    net.minecraft.world.level.block.entity.BlockEntity be = targetLevel.getBlockEntity(entry.pos().pos());
                                    if (be instanceof com.zephyrhauler.block.entity.ZephyrDockBlockEntity dockBE && !entry.isHub()) {
                                        if (dockBE.getDockId().equals(entry.dockId())) {
                                            String realName = dockBE.getCustomName().isEmpty() ? net.minecraft.network.chat.Component.translatable("dock.zephyr_hauler.generic_dock").getString() : dockBE.getCustomName();
                                            if (!realName.equals(entry.name())) {
                                                docks.set(i, new com.zephyrhauler.component.DockEntry(entry.dockId(), realName, entry.pos(), false));
                                                updated = true;
                                            }
                                        }
                                    } else if (be instanceof com.zephyrhauler.block.entity.ZephyrHubBlockEntity hubBE && entry.isHub()) {
                                        if (hubBE.getHubId().equals(entry.dockId())) {
                                            String realName = hubBE.getCustomName().isEmpty() ? "Zephyr Hub" : hubBE.getCustomName();
                                            if (!realName.equals(entry.name())) {
                                                docks.set(i, new com.zephyrhauler.component.DockEntry(entry.dockId(), realName, entry.pos(), true));
                                                updated = true;
                                            }
                                        }
                                    }
                                }
                            }
                            if (updated) {
                                controllerStack.set(com.zephyrhauler.component.ZephyrDataComponents.SAVED_DOCKS.get(), docks);
                                PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(-1, "message.zephyr_hauler.controller.success_refresh"));
                            } else {
                                PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(-1, "message.zephyr_hauler.controller.no_changes"));
                            }
                        }
                    });
                }
        );

        registrar.playToServer(
                ToggleAutoRepairPayload.TYPE,
                ToggleAutoRepairPayload.CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        net.minecraft.world.entity.player.Player player = context.player();
                        if (player == null) return;
                        net.minecraft.world.level.block.entity.BlockEntity be = player.level().getBlockEntity(payload.pos());
                        if (be instanceof com.zephyrhauler.block.entity.ZephyrAutoStationBlockEntity autoStationBE) {
                            autoStationBE.setAutoRepairEnabled(!autoStationBE.isAutoRepairEnabled());
                        }
                    });
                }
        );

        registrar.playToServer(WindSensorUpdatePayload.TYPE, WindSensorUpdatePayload.STREAM_CODEC, WindSensorUpdatePayload::handle);

        registrar.playToClient(
                HubMonitorPayload.TYPE,
                HubMonitorPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        net.minecraft.client.Minecraft.getInstance().setScreen(
                                new com.zephyrhauler.client.gui.screen.ZephyrHubMonitorScreen(payload)
                        );
                    });
                }
        );

        registrar.playToServer(
                HubStatsRequestPayload.TYPE,
                HubStatsRequestPayload.CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        Player player = context.player();
                        if (player == null) return;
                        ServerLevel targetLevel = player.getServer().getLevel(payload.pos().dimension());
                        if (targetLevel != null) {
                            targetLevel.getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.POST_TELEPORT, new ChunkPos(payload.pos().pos()), 3, player.getId());
                            BlockEntity be = targetLevel.getBlockEntity(payload.pos().pos());

                            if (be instanceof com.zephyrhauler.block.entity.ZephyrHubBlockEntity hubBE && hubBE.getHubId().equals(payload.hubId())) {
                                int total = 0, free = 0, reserved = 0, occupied = 0, inaccessible = 0;

                                for (BlockPos dPos : hubBE.getConnectedDocks()) {
                                    total++;

                                    if (com.zephyrhauler.entity.ZephyrHaulerEntity.isAirspaceObstructed(targetLevel, dPos)) {
                                        inaccessible++;
                                        continue;
                                    }

                                    BlockState dState = targetLevel.getBlockState(dPos);
                                    BlockEntity dockBe = targetLevel.getBlockEntity(dPos);

                                    if (dockBe instanceof com.zephyrhauler.block.entity.ZephyrDockBlockEntity dockEntity) {

                                        boolean physicallyOccupied = !targetLevel.getEntitiesOfClass(
                                                com.zephyrhauler.entity.ZephyrHaulerEntity.class,
                                                new net.minecraft.world.phys.AABB(dPos.above()).inflate(0.1)
                                        ).isEmpty();

                                        if (dockEntity.isOccupied() || physicallyOccupied) {
                                            occupied++;
                                        } else if (dState.hasProperty(com.zephyrhauler.block.ZephyrDockBlock.RESERVED) && dState.getValue(com.zephyrhauler.block.ZephyrDockBlock.RESERVED)) {
                                            reserved++;
                                        } else {
                                            free++;
                                        }
                                    } else {
                                        inaccessible++;
                                    }
                                }
                                PacketDistributor.sendToPlayer((ServerPlayer) player, new HubStatsResponsePayload(total, free, reserved, occupied, inaccessible));
                            }
                        }
                    });
                }
        );

        registrar.playToClient(
                HubStatsResponsePayload.TYPE,
                HubStatsResponsePayload.CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        if (net.minecraft.client.Minecraft.getInstance().screen instanceof com.zephyrhauler.client.gui.screen.ZephyrControllerScreen screen) {
                            screen.receiveHubStatsUpdate(payload.total(), payload.free(), payload.reserved(), payload.occupied(), payload.inaccessible());
                        }
                    });
                }
        );
    }
}