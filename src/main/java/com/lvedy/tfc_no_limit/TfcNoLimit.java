package com.lvedy.tfc_no_limit;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.dries007.tfc.common.component.TFCComponents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(TfcNoLimit.MODID)
public class TfcNoLimit {
    public static final String MODID = "tnl";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TfcNoLimit(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onConfigLoading);
        modEventBus.addListener(this::onConfigReloading);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            WeightStackSizeManager.applyConfiguredWeights();
            TFCComponents.onModifyDefaultComponentsAfterResourceReload();
        });
    }

    private void onConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == Config.SPEC) {
            WeightStackSizeManager.applyConfiguredWeights();
        }
    }

    private void onConfigReloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == Config.SPEC) {
            WeightStackSizeManager.applyConfiguredWeights();
            TFCComponents.onModifyDefaultComponentsAfterResourceReload();
        }
    }
}
