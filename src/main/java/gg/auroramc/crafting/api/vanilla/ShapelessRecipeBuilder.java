package gg.auroramc.crafting.api.vanilla;

import gg.auroramc.crafting.api.blueprint.ChoiceType;
import org.bukkit.inventory.*;

import java.util.List;

public class ShapelessRecipeBuilder extends CraftingRecipeBuilder<ShapelessRecipe, ShapelessRecipeBuilder> {
    private List<ItemStack> ingredients;

    public ShapelessRecipeBuilder(String id, ChoiceType choiceType) {
        super(id, choiceType);
    }

    public static ShapelessRecipeBuilder shapelessRecipe(String id, ChoiceType choiceType) {
        return new ShapelessRecipeBuilder(id, choiceType);
    }

    public ShapelessRecipeBuilder ingredients(List<ItemStack> ingredients) {
        this.ingredients = List.copyOf(ingredients);
        return this;
    }

    @Override
    public ShapelessRecipe build() {
        var recipe = new ShapelessRecipe(key, result);

        if (group != null) {
            recipe.setGroup(group);
        }
        recipe.setCategory(category);

        for (var ingredient : ingredients) {
            if (!ingredient.isEmpty()) {
                recipe.addIngredient(choiceSelector.apply(ingredient));
            }
        }

        return recipe;
    }
}
