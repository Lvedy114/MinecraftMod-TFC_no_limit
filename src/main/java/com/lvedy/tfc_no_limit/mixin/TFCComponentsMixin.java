package com.lvedy.tfc_no_limit.mixin;

import com.lvedy.tfc_no_limit.Config;
import net.dries007.tfc.common.component.TFCComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TFCComponents.class)
public class TFCComponentsMixin {

    @ModifyVariable(
            method = "lambda$onModifyDefaultComponentsAfterResourceReload$2(Lnet/minecraft/core/component/DataComponentPatch$Builder;Lnet/minecraft/world/item/Item;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/component/DataComponentPatch$Builder;set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Lnet/minecraft/core/component/DataComponentPatch$Builder;",
                    ordinal = 1
            ),
            ordinal = 3,
            remap = false,
            require = 0
    )
    private static int modifyRequestedStackSize(int originalRequestedSize) {
        if (!Config.ENABLE_CUSTOM_STACK_SIZES.get()) return originalRequestedSize;
        return 64;
    }
}

