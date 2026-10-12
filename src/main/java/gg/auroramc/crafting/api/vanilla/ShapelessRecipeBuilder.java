package gg.auroramc.crafting.api.vanilla;

import gg.auroramc.aurora.api.item.TypeId;
import org.bukkit.inventory.*;

import java.util.List;

public class ShapelessRecipeBuilder extends CraftingRecipeBuilder<ShapelessRecipe, ShapelessRecipeBuilder> {
    private List<TypeId> ingredients;

    public ShapelessRecipeBuilder(String id) {
        super(id);
    }

    public static ShapelessRecipeBuilder shapelessRecipe(String id) {
        return new ShapelessRecipeBuilder(id);
    }

    public ShapelessRecipeBuilder ingredients(List<TypeId> ingredients) {
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
            if (!isEmpty(ingredient)) {
                recipe.addIngredient(predicateChoiceFor(ingredient));
            }
        }

        return recipe;
    }
}
