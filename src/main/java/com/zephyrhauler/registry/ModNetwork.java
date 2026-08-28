package com.zephyrhauler.registry;

import com.zephyrhauler.block.entity.ZephyrDockBlockEntity;
import com.zephyrhauler.component.ZephyrDataComponents;
import com.zephyrhauler.network.ControllerActionPayload;
import com.zephyrhauler.network.DockStatusRequestPayload;
import com.zephyrhauler.network.DockStatusResponsePayload;
import com.zephyrhauler.network.SaveDockNamePayload;
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

                        if (be instanceof ZephyrDockBlockEntity dockBE) {
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
    }
}