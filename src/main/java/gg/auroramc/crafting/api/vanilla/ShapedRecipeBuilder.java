package gg.auroramc.crafting.api.vanilla;

import gg.auroramc.crafting.api.blueprint.ChoiceType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShapedRecipeBuilder extends CraftingRecipeBuilder<ShapedRecipe, ShapedRecipeBuilder> {
    private CraftingBookCategory category = CraftingBookCategory.MISC;
    private String group = null;
    private final String[] shape = new String[]{"012", "345", "678"};
    private final Map<Character, ItemStack> ingredients = new HashMap<>();

    public ShapedRecipeBuilder(String id, ChoiceType choiceType) {
        super(id, choiceType);
    }

    public static ShapedRecipeBuilder shapedRecipe(String id, ChoiceType choiceType) {
        return new ShapedRecipeBuilder(id, choiceType);
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
        for (int i = 0; i < ingredients.size(); i++) {
            this.ingredients.put(String.valueOf(i).toCharArray()[0], ingredients.get(i));
        }

        return this;
    }

    @Override
    public ShapedRecipe build() {
        var recipe = new ShapedRecipe(key, result);

        if (group != null) {
            recipe.setGroup(group);
        }
        recipe.setCategory(category);
        recipe.shape(shape);

        for (var entry : ingredients.entrySet()) {
            recipe.setIngredient(entry.getKey(), choiceSelector.apply(entry.getValue()));
        }

        return recipe;
    }
}
