package com.lvedy.tfc_no_limit.mixin;

import com.lvedy.tfc_no_limit.Config;
import net.dries007.tfc.common.component.TFCComponents;
import net.dries007.tfc.common.component.size.ItemSizeManager;
import net.dries007.tfc.common.component.size.Weight;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(TFCComponents.class)
public class TFCComponentsMixin {

    @Inject(
            method = "lambda$onModifyDefaultComponentsAfterResourceReload$2",
            at = @At("TAIL"),
            remap = false
    )
    private static void overwriteMaxStackSize(
            Item item,
            int currentMaxStackSize,
            int tfcStackSize,
            DataComponentPatch.Builder patchBuilder,
            CallbackInfo ci
    ) {
        if (!Config.ENABLE_CUSTOM_STACK_SIZES.get()) return;
        
        ItemStack stack = new ItemStack(item);
        Weight weight = ItemSizeManager.get(stack).getWeight(stack);

        int newStackSize = 64;
        if (weight == Weight.VERY_LIGHT) {
            newStackSize = Config.VERY_LIGHT_STACK_SIZE.get();
        } else if (weight == Weight.LIGHT) {
            newStackSize = Config.LIGHT_STACK_SIZE.get();
        } else if (weight == Weight.MEDIUM) {
            newStackSize = Config.MEDIUM_STACK_SIZE.get();
        } else if (weight == Weight.HEAVY) {
            newStackSize = Config.HEAVY_STACK_SIZE.get();
        } else if (weight == Weight.VERY_HEAVY) {
            newStackSize = Config.VERY_HEAVY_STACK_SIZE.get();
        }

        patchBuilder.set(DataComponents.MAX_STACK_SIZE, newStackSize);
    }
}

