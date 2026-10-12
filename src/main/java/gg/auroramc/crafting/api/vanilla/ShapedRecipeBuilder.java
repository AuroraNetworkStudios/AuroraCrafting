package gg.auroramc.crafting.api.vanilla;

import gg.auroramc.aurora.api.item.TypeId;
import org.bukkit.inventory.ShapedRecipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShapedRecipeBuilder extends CraftingRecipeBuilder<ShapedRecipe, ShapedRecipeBuilder> {
    private String[] shape;
    private final Map<Character, TypeId> ingredients = new HashMap<>();
    private final boolean symmetrical;

    public ShapedRecipeBuilder(String id, boolean symmetrical) {
        super(id);
        this.symmetrical = symmetrical;
        if (!symmetrical) {
            this.shape = new String[]{"012", "345", "678"};
        }
    }

    public static ShapedRecipeBuilder shapedRecipe(String id, boolean symmetrical) {
        return new ShapedRecipeBuilder(id, symmetrical);
    }

    public ShapedRecipeBuilder ingredients(List<TypeId> ingredients) {
        if (!symmetrical) {
            for (int i = 0; i < ingredients.size(); i++) {
                this.ingredients.put(String.valueOf(i).charAt(0), ingredients.get(i));
            }
        } else {
            // Find the smallest bounding box that contains all ingredients
            int minRow = 3, maxRow = 0, minCol = 3, maxCol = 0;
            for (int i = 0; i < ingredients.size(); i++) {
                if (!isEmpty(ingredients.get(i))) {
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
            Map<TypeId, Character> charMap = new HashMap<>();
            char nextChar = 'A'; // Start from 'A' for symmetrical patterns

            for (int row = 0; row < height; row++) {
                StringBuilder shapeRow = new StringBuilder();
                for (int col = 0; col < width; col++) {
                    int originalIndex = (minRow + row) * 3 + (minCol + col);
                    TypeId item = originalIndex < ingredients.size() ? ingredients.get(originalIndex) : null;

                    if (!isEmpty(item)) {
                        // Reuse existing character for identical items
                        var character = charMap.get(item);
                        if (character == null) {
                            character = nextChar++;
                            charMap.put(item, character);
                            this.ingredients.put(character, item);
                        }
                        shapeRow.append(character);
                    } else {
                        shapeRow.append(' ');
                    }
                }
                newShape[row] = shapeRow.toString();
            }

            this.shape = newShape;
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
            if (!isEmpty(entry.getValue())) {
                recipe.setIngredient(entry.getKey(), predicateChoiceFor(entry.getValue()));
            }
        }

        return recipe;
    }
}
