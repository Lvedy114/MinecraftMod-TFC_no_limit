package com.lvedy.tfc_no_limit.mixin;

import com.lvedy.tfc_no_limit.TfcNoLimit;
import net.dries007.tfc.common.component.TFCComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TFCComponents.class)
public class TFCComponentsMixin {

    @Inject(method = "onModifyDefaultComponentsAfterResourceReload", at = @At("TAIL"), remap = false)
    private static void injectCustomStackSizes(CallbackInfo ci) {
        TfcNoLimit.applyCustomStackSizes();
    }
}
