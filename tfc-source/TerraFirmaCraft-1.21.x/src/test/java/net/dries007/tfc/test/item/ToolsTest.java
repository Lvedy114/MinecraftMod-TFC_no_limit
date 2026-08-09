/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test.item;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.dries007.tfc.common.component.size.IItemSize;
import net.dries007.tfc.common.component.size.ItemSizeManager;
import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.test.TestSetup;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;

public class ToolsTest implements TestSetup
{
    @Test
    public void testToolsAreSmallToolsOrLargeTools()
    {
        final List<String> items = TFCItems.ITEMS.getEntries()
            .stream()
            .filter(holder -> {
                final ItemStack stack = new ItemStack(holder.get());
                if (!stack.is(Tags.Items.TOOLS)) { return false; }
                final IItemSize sizeManager = ItemSizeManager.get(stack);
                final Size size = sizeManager.getSize(stack);
                final Weight weight = sizeManager.getWeight(stack);
                
                return !(size == Size.LARGE && weight == Weight.MEDIUM)
                    && !(size == Size.VERY_LARGE && weight == Weight.VERY_HEAVY);
            })
            .map(holder -> holder.getId().toString())
            .toList();
        
        assertTrue(items.isEmpty(), "Tool items are not marked as small tools or large tools: " + String.join("\n", items));
    }
}
