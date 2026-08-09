package com.lvedy.tfc_no_limit;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = TfcNoLimit.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = TfcNoLimit.MODID, value = Dist.CLIENT)
public class TfcNoLimitClient {
    public TfcNoLimitClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        TfcNoLimit.LOGGER.debug("Registered TFC No Limit client config screen.");
    }
}
