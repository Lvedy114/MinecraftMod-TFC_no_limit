package com.lvedy.tfc_no_limit;

import net.dries007.tfc.common.component.size.Weight;

import com.lvedy.tfc_no_limit.mixin.WeightAccessor;

public final class WeightStackSizeManager {
    private WeightStackSizeManager() {
    }

    public static void applyConfiguredWeights() {
        setStackSize(Weight.VERY_LIGHT, Config.getStackSize(Weight.VERY_LIGHT));
        setStackSize(Weight.LIGHT, Config.getStackSize(Weight.LIGHT));
        setStackSize(Weight.MEDIUM, Config.getStackSize(Weight.MEDIUM));
        setStackSize(Weight.HEAVY, Config.getStackSize(Weight.HEAVY));
        setStackSize(Weight.VERY_HEAVY, Config.getStackSize(Weight.VERY_HEAVY));
    }

    private static void setStackSize(Weight weight, int stackSize) {
        if (weight.stackSize != stackSize) {
            ((WeightAccessor) (Object) weight).tnl$setStackSize(stackSize);
            TfcNoLimit.LOGGER.info("Applied TFC weight stack size override: {} -> {}", weight, stackSize);
        }
    }
}
