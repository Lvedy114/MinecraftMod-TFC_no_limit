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
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.minecraft.server.MinecraftServer;

import com.lvedy.tfc_no_limit.climate.TNLClimateModels;

@Mod(TfcNoLimit.MODID)
public class TfcNoLimit {
    public static final String MODID = "tnl";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TfcNoLimit(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onConfigLoading);
        modEventBus.addListener(this::onConfigReloading);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        TNLClimateModels.TYPES.register(modEventBus);
        VanillaClimateHandler.register();
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

            // 让原版地形气候的开关与温度设置立即生效（重新选择并同步各维度的气候模型）
            final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                VanillaClimateHandler.refreshClimateModels(server);
            }
        }
    }
}
