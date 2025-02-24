package gg.auroramc.crafting.api.workbench.vanilla;

import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.crafting.api.ItemPair;
import gg.auroramc.crafting.api.blueprint.*;
import org.bukkit.Bukkit;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SmithingTable extends VanillaWorkbench<SmithingBlueprint> {
    private final Map<String, Blueprint> vanillaLookup = new HashMap<>();
    private final List<SmithingRecipeWrapper> vanillaRecipes = new ArrayList<>();

    public SmithingTable() {
        super("vanilla-smithing-table", 3, List.of(0, 1, 2), VanillaType.SMITHING_TABLE);

        for (@NotNull Iterator<Recipe> it = Bukkit.recipeIterator(); it.hasNext(); ) {
            var recipe = it.next();
            if (recipe instanceof SmithingTransformRecipe smithingRecipe) {
                if (smithingRecipe.getKey().getNamespace().equals("minecraft")) {
                    vanillaRecipes.add(new SmithingTransformRecipeWrapper(smithingRecipe));
                }
            } else if (recipe instanceof SmithingTrimRecipe smithingRecipe) {
                if (smithingRecipe.getKey().getNamespace().equals("minecraft")) {
                    vanillaRecipes.add(new SmithingTrimRecipeWrapper(smithingRecipe));
                }
            }
        }
    }

    @Override
    public void addBlueprint(BlueprintType type, Blueprint blueprint) {
        super.addBlueprint(type, blueprint);
        if (blueprint instanceof SmithingBlueprint smithingBlueprint && smithingBlueprint.getVanillaOptions().getChoiceType() == ChoiceType.ITEM_TYPE) {
            if (getVanillaRecipe(smithingBlueprint.getIngredientItems()) == null) {
                vanillaLookup.put(
                        BlueprintLookupGenerator.toShapedKey(blueprint.getIngredientItems().stream().map(i -> new ItemPair(TypeId.from(i.getType()), 1)).toArray(ItemPair[]::new)),
                        blueprint
                );
            }
        }
    }

    public boolean matchesRegisteredVanillaRecipe(BlueprintContext context) {
        return vanillaLookup.containsKey(context.getShapedLookupKey());
    }

    @Override
    protected boolean shouldRegisterVanillaRecipeFor(Blueprint blueprint) {
        if (blueprint instanceof SmithingBlueprint smithingBlueprint) {
            return getVanillaRecipe(smithingBlueprint.getIngredientItems()) == null;
        }
        return false;
    }

    private SmithingRecipeWrapper getVanillaRecipe(List<ItemStack> items) {
        for (var recipe : vanillaRecipes) {
            if (recipe.matches(items)) {
                return recipe;
            }
        }
        return null;
    }

    public interface SmithingRecipeWrapper {
        boolean matches(List<ItemStack> items);
    }

    public static class SmithingTransformRecipeWrapper implements SmithingRecipeWrapper {
        private final SmithingTransformRecipe recipe;

        public SmithingTransformRecipeWrapper(SmithingTransformRecipe recipe) {
            this.recipe = recipe;
        }

        @Override
        public boolean matches(List<ItemStack> items) {
            return matchesChoice(recipe.getTemplate(), items.get(0)) &&
                    matchesChoice(recipe.getBase(), items.get(1)) &&
                    matchesChoice(recipe.getAddition(), items.get(2));
        }
    }

    public static class SmithingTrimRecipeWrapper implements SmithingRecipeWrapper {
        private final SmithingTrimRecipe recipe;

        public SmithingTrimRecipeWrapper(SmithingTrimRecipe recipe) {
            this.recipe = recipe;
        }

        @Override
        public boolean matches(List<ItemStack> items) {
            return matchesChoice(recipe.getTemplate(), items.get(0)) &&
                    matchesChoice(recipe.getBase(), items.get(1)) &&
                    matchesChoice(recipe.getAddition(), items.get(2));
        }
    }

    private static boolean matchesChoice(RecipeChoice choice, ItemStack item) {
        if (choice == null || item == null) {
            return false;
        }
        return choice.test(item);
    }
}
