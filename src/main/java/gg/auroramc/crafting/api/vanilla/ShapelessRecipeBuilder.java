package gg.auroramc.crafting.api.vanilla;

import org.bukkit.inventory.*;
import org.bukkit.inventory.recipe.CraftingBookCategory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShapelessRecipeBuilder extends RecipeBuilder<ShapelessRecipeBuilder> {
    private CraftingBookCategory category = CraftingBookCategory.MISC;
    private String group = null;
    private List<RecipeChoice> ingredients;

    public ShapelessRecipeBuilder(String id) {
        super(id);
    }

    public static ShapedRecipeBuilder shapelessRecipe(String id) {
        return new ShapedRecipeBuilder(id);
    }

    public ShapelessRecipeBuilder category(CraftingBookCategory category) {
        this.category = category;
        return this;
    }

    public ShapelessRecipeBuilder group(String group) {
        this.group = group;
        return this;
    }

    public ShapelessRecipeBuilder ingredients(List<ItemStack> ingredients) {
        for(var ingredient : ingredients) {
            if(!ingredient.isEmpty()) {
                this.ingredients.add(new RecipeChoice.MaterialChoice(ingredient.getType()));
            }
        }

        return this;
    }

    @Override
    public Recipe build() {
        var recipe = new ShapelessRecipe(key, result);

        if (group != null) {
            recipe.setGroup(group);
        }
        recipe.setCategory(category);

        for(var ingredient : ingredients) {
            recipe.addIngredient(ingredient);
        }

        return recipe;
    }
}
