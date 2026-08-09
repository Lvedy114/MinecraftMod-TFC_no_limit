/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test.recipes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.Tags;
import org.junit.jupiter.api.Test;

import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.recipes.AdvancedShapedRecipe;
import net.dries007.tfc.common.recipes.AdvancedShapelessRecipe;
import net.dries007.tfc.common.recipes.outputs.DamageCraftingRemainderModifier;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.dries007.tfc.test.TestSetup;
import net.dries007.tfc.util.Helpers;

import static org.junit.jupiter.api.Assertions.*;

public class CraftingRecipesTest implements TestSetup
{
    @Test
    public void testCraftingRecipesWithToolsDamageInputs()
    {
        final Set<String> expectedDoNotDamageInputs = Stream.of(
            Stream.of(
                "minecraft:fire_charge",
                "tfc:crafting/add_large_bait",
                "tfc:crafting/add_small_bait"
            ),
            Arrays.stream(Wood.VALUES).map(wood -> "tfc:crafting/wood/sewing_table/" + wood.getSerializedName())
        ).flatMap(s -> s).collect(Collectors.toSet()); // Exclude recipes that consume the tool

        final RecipeManager manager = Helpers.getUnsafeRecipeManager();

        final List<String> recipes = manager
            .getAllRecipesFor(RecipeType.CRAFTING)
            .stream()
            .filter(holder -> {
                if (expectedDoNotDamageInputs.contains(holder.id().toString())) return false;
                final CraftingRecipe recipe = holder.value();
                final Optional<ItemStackProvider> remainder = recipe instanceof AdvancedShapedRecipe shaped ? shaped.getRemainder()
                    : recipe instanceof AdvancedShapelessRecipe shapeless ? shapeless.getRemainder() : Optional.empty();

                if (remainder.isPresent() && remainder.get().modifiers().stream().anyMatch(modifier ->  modifier instanceof DamageCraftingRemainderModifier))
                {
                    return false;
                }

                final Stream<ItemStack> stacks = recipe
                    .getIngredients()
                    .stream()
                    .flatMap(ingredient -> Arrays.stream(ingredient.getItems()));

                return stacks.anyMatch(stack -> stack.isDamageableItem() && stack.is(Tags.Items.TOOLS));
            })
            .map(holder -> holder.id().toString())
            .toList();

        assertTrue(recipes.isEmpty(), "Recipes with tools do not damage inputs: " + String.join("\n", recipes));
    }

    @Test
    public void testAdvancedShapelessRecipesHavePrimaryInput()
    {
        final RecipeManager manager = Helpers.getUnsafeRecipeManager();

        final List<String> recipes = manager
            .getAllRecipesFor(RecipeType.CRAFTING)
            .stream()
            .filter(holder -> {
                final CraftingRecipe recipe = holder.value();
                if (recipe instanceof AdvancedShapelessRecipe advancedShapeless)
                {
                    return advancedShapeless.getPrimaryIngredient().isEmpty();
                }
                else
                {
                    return false;
                }
            })
            .map(holder -> holder.id().toString())
            .toList();

        assertTrue(recipes.isEmpty(), "Advanced shapeless crafting recipes do not have primary inputs: " + String.join("\n", recipes));
    }

}
