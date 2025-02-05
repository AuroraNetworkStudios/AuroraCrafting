package gg.auroramc.crafting.api.vanilla;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShapedRecipeBuilder extends RecipeBuilder<ShapedRecipeBuilder> {
    private CraftingBookCategory category = CraftingBookCategory.MISC;
    private String group = null;
    private String[] shape = new String[3];
    private Map<Character, RecipeChoice> ingredients;


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
        var airChar = 'N';
        var chars = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};
        var charIndex = 0;
        var map = new HashMap<ItemStack, Character>();

        for (int i = 0; i < 9; i++) {
            var ingredient = ingredients.get(i);
            if (map.containsKey(ingredient)) continue;
            if (ingredient.isEmpty()) {
                map.put(ingredient, airChar);
            } else {
                map.put(ingredient, chars[charIndex]);
                charIndex++;
            }
        }

        var shapeString = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            var ingredient = ingredients.get(i);
            shapeString.append(map.get(ingredient));
            if (i == 3 || i == 6) {
                shapeString.append(";");
            }
        }
        this.shape = Arrays.stream(shapeString.toString().split(";")).toArray(String[]::new);

        for (var entry : map.entrySet()) {
            var choice = entry.getKey().isEmpty() ? EmptyRecipeChoice.get() : new RecipeChoice.MaterialChoice(entry.getKey().getType());
            this.ingredients.put(entry.getValue(), choice);
        }

        return this;
    }

    @Override
    public Recipe build() {
        var recipe = new ShapedRecipe(key, result);

        if (group != null) {
            recipe.setGroup(group);
        }
        recipe.setCategory(category);
        recipe.shape(shape);

        for (var entry : ingredients.entrySet()) {
            recipe.setIngredient(entry.getKey(), entry.getValue());
        }

        return recipe;
    }
}
