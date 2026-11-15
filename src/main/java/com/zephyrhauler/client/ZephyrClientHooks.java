package com.zephyrhauler.client;

import com.zephyrhauler.client.gui.screen.ZephyrControllerScreen;
import com.zephyrhauler.client.gui.screen.ZephyrDockNamingScreen;
import com.zephyrhauler.client.gui.screen.ZephyrHubMonitorScreen;
import com.zephyrhauler.client.gui.screen.ZephyrHubNamingScreen;
import com.zephyrhauler.network.DockStatusResponsePayload;
import com.zephyrhauler.network.HubMonitorPayload;
import com.zephyrhauler.network.HubStatsResponsePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public final class ZephyrClientHooks {

    private ZephyrClientHooks() {}

    public static void openDockNaming(BlockPos pos, String currentName) {
        Minecraft.getInstance().setScreen(new ZephyrDockNamingScreen(pos, currentName));
    }

    public static void openHubNaming(BlockPos pos, String currentName) {
        Minecraft.getInstance().setScreen(new ZephyrHubNamingScreen(pos, currentName));
    }

    public static void openHubMonitor(HubMonitorPayload payload) {
        Minecraft.getInstance().setScreen(new ZephyrHubMonitorScreen(payload));
    }

    public static void handleDockStatus(DockStatusResponsePayload payload) {
        if (Minecraft.getInstance().screen instanceof ZephyrControllerScreen screen) {
            screen.receiveStatusUpdate(payload.status(), payload.message());
        }
    }

    public static void handleHubStats(HubStatsResponsePayload payload) {
        if (Minecraft.getInstance().screen instanceof ZephyrControllerScreen screen) {
            screen.receiveHubStatsUpdate(payload.total(), payload.free(), payload.reserved(), payload.occupied(), payload.inaccessible());
        }
    }
}