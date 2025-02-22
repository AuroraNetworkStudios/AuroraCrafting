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
    private String[] shape;
    private final Map<Character, ItemStack> ingredients = new HashMap<>();
    private final boolean symmetrical;

    public ShapedRecipeBuilder(String id, ChoiceType choiceType, boolean symmetrical) {
        super(id, choiceType);
        this.symmetrical = symmetrical;
        if (!symmetrical) {
            this.shape = new String[]{"012", "345", "678"};
        }
    }

    public static ShapedRecipeBuilder shapedRecipe(String id, ChoiceType choiceType, boolean symmetrical) {
        return new ShapedRecipeBuilder(id, choiceType, symmetrical);
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
        if (!symmetrical) {
            for (int i = 0; i < ingredients.size(); i++) {
                this.ingredients.put(String.valueOf(i).toCharArray()[0], ingredients.get(i));
            }
        } else {
            // Find the smallest bounding box that contains all ingredients
            int minRow = 3, maxRow = 0, minCol = 3, maxCol = 0;
            for (int i = 0; i < ingredients.size(); i++) {
                if (ingredients.get(i) != null) {
                    int row = i / 3;
                    int col = i % 3;
                    minRow = Math.min(minRow, row);
                    maxRow = Math.max(maxRow, row);
                    minCol = Math.min(minCol, col);
                    maxCol = Math.max(maxCol, col);
                }
            }

            // Generate a compacted shape based on the bounding box
            int height = maxRow - minRow + 1;
            int width = maxCol - minCol + 1;
            String[] newShape = new String[height];
            Map<Character, ItemStack> ingredientMap = new HashMap<>();
            char nextChar = 'A'; // Start from 'A' for symmetrical patterns

            for (int row = 0; row < height; row++) {
                StringBuilder shapeRow = new StringBuilder();
                for (int col = 0; col < width; col++) {
                    int originalIndex = (minRow + row) * 3 + (minCol + col);
                    ItemStack item = ingredients.get(originalIndex);

                    if (item != null) {
                        if (!ingredientMap.containsValue(item)) {
                            ingredientMap.put(nextChar, item);
                            shapeRow.append(nextChar);
                            nextChar++;
                        } else {
                            // Reuse existing character for identical items
                            shapeRow.append(ingredientMap.entrySet().stream()
                                    .filter(entry -> entry.getValue().equals(item))
                                    .findFirst().get().getKey());
                        }
                    } else {
                        shapeRow.append(' ');
                    }
                }
                newShape[row] = shapeRow.toString();
            }

            this.shape = newShape;
            this.ingredients.putAll(ingredientMap);
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
            if (!entry.getValue().isEmpty()) {
                recipe.setIngredient(entry.getKey(), choiceSelector.apply(entry.getValue()));
            }
        }

        return recipe;
    }
}
