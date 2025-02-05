package gg.auroramc.crafting.api.vanilla;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;

import java.util.List;
import java.util.Map;

public class ShapedRecipeBuilder extends RecipeBuilder<ShapedRecipeBuilder> {
    private CraftingBookCategory category = CraftingBookCategory.MISC;
    private String group = null;
    private String[] shape = new String[3];
    private Map<Character, ItemStack> ingredients;


    public ShapedRecipeBuilder(String id) {
        super(id);
    }

    public static ShapedRecipeBuilder shapedRecipe(String id) {
        return new ShapedRecipeBuilder(id);
    }

    public ShapedRecipeBuilder category(CraftingBookCategory category) {
        this.category = category;
        return this;
    }

    public ShapedRecipeBuilder group(String group) {
        this.group = group;
        return this;
    }

    public ShapedRecipeBuilder ingredients(List<ItemStack> ingredients) {
        // TODO: compute shape and chocie map
        return this;
    }

    @Override
    public Recipe build() {
        var recipe = new ShapedRecipe(key, result);

        if(group != null) {
            recipe.setGroup(group);
        }
        recipe.setCategory(category);
        recipe.shape(shape);

        for(var entry : ingredients.entrySet()) {
            // We should use material choice here
            recipe.setIngredient(entry.getKey(), entry.getValue());
        }

        return recipe;
    }
}
