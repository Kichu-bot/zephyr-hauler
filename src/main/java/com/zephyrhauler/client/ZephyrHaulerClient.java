package com.zephyrhauler.client;

import com.zephyrhauler.ZephyrHauler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = ZephyrHauler.MOD_ID, dist = Dist.CLIENT)
public class ZephyrHaulerClient {

    public ZephyrHaulerClient(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(ClientModEvents::registerParticleProviders);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}