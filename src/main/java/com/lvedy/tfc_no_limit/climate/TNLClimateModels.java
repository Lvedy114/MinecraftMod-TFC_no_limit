package com.lvedy.tfc_no_limit.climate;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.dries007.tfc.util.climate.ClimateModelType;
import net.dries007.tfc.util.climate.ClimateModels;

import com.lvedy.tfc_no_limit.TfcNoLimit;

/**
 * 注册到 TFC 气候模型注册表（{@code tfc:climate_model}）的自定义气候模型类型。
 * 只有注册过的模型才能通过 TFC 自带的 {@code UpdateClimateModelPacket} 同步到客户端。
 */
public final class TNLClimateModels
{
    public static final DeferredRegister<ClimateModelType<?>> TYPES = DeferredRegister.create(ClimateModels.KEY, TfcNoLimit.MODID);

    public static final DeferredHolder<ClimateModelType<?>, ClimateModelType<VanillaOverworldClimateModel>> VANILLA_OVERWORLD =
        TYPES.register("vanilla_overworld", () -> new ClimateModelType<>(VanillaOverworldClimateModel.STREAM_CODEC));

    private TNLClimateModels() {}
}
