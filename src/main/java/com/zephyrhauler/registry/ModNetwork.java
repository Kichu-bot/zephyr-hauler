package com.zephyrhauler.registry;

import com.zephyrhauler.block.entity.ZephyrDockBlockEntity;
import com.zephyrhauler.component.ZephyrDataComponents;
import com.zephyrhauler.network.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
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

                        if (player.level().getBlockEntity(payload.pos()) instanceof ZephyrDockBlockEntity dockBE) {
                            dockBE.setCustomName(payload.name());
                            dockBE.setChanged();
                            player.level().sendBlockUpdated(payload.pos(), dockBE.getBlockState(), dockBE.getBlockState(), 3);
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

                        if (be instanceof ZephyrDockBlockEntity dockBE) {

                            if (!dockBE.getDockId().equals(payload.dockId())) {
                                PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(2, "message.zephyr_hauler.controller.error_dock_replaced"));
                                return;
                            }

                            ItemStack haulerStack = controllerMenu.haulerContainer.getItem(0);

                            if (payload.action() == 0) {
                                if (haulerStack.isEmpty() || !(haulerStack.getItem() instanceof com.zephyrhauler.item.ZephyrHaulerItem)) {
                                    PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(-1, "message.zephyr_hauler.controller.error_no_hauler"));
                                    return;
                                }
                                if (haulerStack.has(ZephyrDataComponents.TARGET_POS.get())) {
                                    PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(-1, "message.zephyr_hauler.controller.error_already_linked") );
                                    return;
                                }
                                if (dockBE.getLinkId() != null || dockBE.isOccupied()) {
                                    PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(1, "message.zephyr_hauler.controller.error_dock_busy"));
                                    return;
                                }

                                UUID newLinkId = UUID.randomUUID();
                                dockBE.setLinkId(newLinkId);
                                dockBE.setChanged();

                                haulerStack.set(ZephyrDataComponents.TARGET_POS.get(), payload.dockPos());
                                haulerStack.set(ZephyrDataComponents.TARGET_NAME.get(), payload.dockName());
                                haulerStack.set(ZephyrDataComponents.LINK_ID.get(), newLinkId);

                                PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(1, "message.zephyr_hauler.controller.success_linked"));
                            }
                            else if (payload.action() == 1) {
                                dockBE.setOccupied(false);
                                dockBE.setPendingDeliveryData(null);
                                dockBE.setLinkId(null);
                                dockBE.setChanged();

                                if (!haulerStack.isEmpty() && haulerStack.getItem() instanceof com.zephyrhauler.item.ZephyrHaulerItem) {
                                    haulerStack.remove(ZephyrDataComponents.TARGET_POS.get());
                                    haulerStack.remove(ZephyrDataComponents.TARGET_NAME.get());
                                    haulerStack.remove(ZephyrDataComponents.LINK_ID.get());
                                }

                                PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(0, "message.zephyr_hauler.controller.success_reset"));
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
                            PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(2, ""));
                            return;
                        }

                        targetLevel.getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.POST_TELEPORT, new ChunkPos(payload.pos().pos()), 3, player.getId());
                        BlockEntity be = targetLevel.getBlockEntity(payload.pos().pos());

                        if (be instanceof ZephyrDockBlockEntity dockBE && dockBE.getDockId().equals(payload.dockId())) {
                            int status = (dockBE.getLinkId() != null || dockBE.isOccupied()) ? 1 : 0;
                            PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(status, ""));
                        } else {
                            PacketDistributor.sendToPlayer((ServerPlayer) player, new DockStatusResponsePayload(2, ""));
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
                                    if (be instanceof com.zephyrhauler.block.entity.ZephyrDockBlockEntity dockBE) {
                                        if (dockBE.getDockId().equals(entry.dockId())) {
                                            String realName = dockBE.getCustomName().isEmpty() ? net.minecraft.network.chat.Component.translatable("dock.zephyr_hauler.generic_dock").getString() : dockBE.getCustomName();

                                            if (!realName.equals(entry.name())) {
                                                docks.set(i, new com.zephyrhauler.component.DockEntry(entry.dockId(), realName, entry.pos()));
                                                updated = true;
                                            }
                                        }
                                    }
                                }
                            }

                            if (updated) {
                                controllerStack.set(com.zephyrhauler.component.ZephyrDataComponents.SAVED_DOCKS.get(), docks);
                                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer((net.minecraft.server.level.ServerPlayer) player, new DockStatusResponsePayload(-1, "message.zephyr_hauler.controller.success_refresh"));
                            } else {
                                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer((net.minecraft.server.level.ServerPlayer) player, new DockStatusResponsePayload(-1, "message.zephyr_hauler.controller.no_changes"));
                            }
                        }
                    });
                }
        );
    }
}