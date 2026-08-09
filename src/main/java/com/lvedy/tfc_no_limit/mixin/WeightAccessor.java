package com.lvedy.tfc_no_limit.mixin;

import net.dries007.tfc.common.component.size.Weight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Weight.class, remap = false)
public interface WeightAccessor {
    @Accessor("stackSize")
    @Mutable
    void tnl$setStackSize(int stackSize);
}
