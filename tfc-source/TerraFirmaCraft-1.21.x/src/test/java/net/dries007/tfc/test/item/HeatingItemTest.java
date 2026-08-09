/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test.item;

import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.component.heat.Heat;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.heat.IHeat;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.dries007.tfc.test.TestSetup;

import static org.junit.jupiter.api.Assertions.*;

public class HeatingItemTest implements TestSetup
{
    @Test
    public void testCookMeat()
    {
        BuiltInRegistries.ITEM.getTag(TFCTags.Items.RAW_MEATS).orElseThrow().stream()
            .filter(holder -> !Objects.requireNonNull(holder.getKey()).location().getNamespace().equals("minecraft"))
            .map(Holder::value)
            .forEach(item -> {
            final ItemStack stack = item.getDefaultInstance();
            final IHeat heat = HeatCapability.get(stack);
            assertNotNull(heat, "Meat not heatable: " + stack);

            heat.setTemperature(Heat.maxVisibleTemperature());
            assertEquals(Heat.maxVisibleTemperature(), heat.getTemperature(), "Heat not achieved for stack: " + stack);

            final HeatingRecipe recipe = HeatingRecipe.getRecipe(stack);
            assertNotNull(recipe, "No heating recipe found for meat: " + stack);
        });
    }
}
