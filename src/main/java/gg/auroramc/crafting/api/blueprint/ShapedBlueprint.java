package gg.auroramc.crafting.api.blueprint;

import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.crafting.api.ItemPair;
import gg.auroramc.crafting.api.workbench.Workbench;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Getter
public class ShapedBlueprint extends CraftingBlueprint<ShapedBlueprint> {
    private Map<String, List<Ingredient>> variations = new HashMap<>();
    private boolean symmetrical = false;

    public ShapedBlueprint(Workbench workbench, String id) {
        super(workbench, id);
    }

    public static ShapedBlueprint shapedBlueprint(Workbench workbench, String id) {
        return new ShapedBlueprint(workbench, id);
    }

    public ShapedBlueprint symmetrical(boolean symmetry) {
        this.symmetrical = symmetry;
        return this;
    }

    @Override
    public int getTimesCraftable(BlueprintContext context) {
        var ingredients = symmetrical ? variations.get(context.getShapedLookupKey()) : this.ingredients;

        int maxCraftable = Integer.MAX_VALUE;

        var matches = true;
        var items = context.getMatrix();

        for (int i = 0; i < items.length; i++) {
            var ingredient = ingredients.size() > i ? ingredients.get(i).getItemPair() : BlueprintContext.AIR;
            var item = items[i];
            var itemTypeId = item.isEmpty() ? TypeId.from(Material.AIR) : context.getIdMatrix()[i].id();
            if (!itemTypeId.equals(ingredient.id())) {
                matches = false;
                break;
            } else if (item.getAmount() < ingredient.amount()) {
                matches = false;
                break;
            } else if (!ingredient.id().id().equals("air")) {
                maxCraftable = Math.min(maxCraftable, Math.max(1, item.getAmount()) / Math.max(1, ingredient.amount()));
            }
        }

        if (!matches) return 0;

        return maxCraftable;
    }

    @Override
    public ItemStack[] calcRemainingIngredientMatrix(BlueprintContext context, int timesCrafted) {
        var ingredients = symmetrical ? variations.get(context.getShapedLookupKey()) : this.ingredients;
        var items = new ItemStack[context.getMatrix().length];
        var currentMatrix = context.getMatrix();

        for (int i = 0; i < context.getMatrix().length; i++) {
            var ingredient = ingredients.size() > i ? ingredients.get(i).getItemPair() : new ItemPair(TypeId.from(Material.AIR), 0);
            var item = currentMatrix[i];
            if (item.getAmount() <= ingredient.amount() * timesCrafted) {
                items[i] = null;
            } else {
                var newItem = item.clone();
                newItem.setAmount(item.getAmount() - ingredient.amount() * timesCrafted);
                items[i] = newItem;
            }
        }

        return items;
    }

    private Map<String, List<Ingredient>> generateShiftedIngredients(Ingredient[] ingredients, int craftingSize) {
        Map<String, List<Ingredient>> variations = new HashMap<>();
        variations.put(BlueprintLookupGenerator.toShapedKey(Arrays.stream(ingredients).map(Ingredient::getItemPair).toArray(ItemPair[]::new)), Arrays.asList(ingredients));

        // Convert to 2D matrix representation
        Ingredient[][] matrix = new Ingredient[craftingSize][craftingSize];
        for (int i = 0; i < craftingSize * craftingSize; i++) {
            matrix[i / craftingSize][i % craftingSize] = ingredients[i];
        }

        // Find bounding box of the recipe
        int minX = craftingSize, maxX = 0, minY = craftingSize, maxY = 0;
        for (int r = 0; r < craftingSize; r++) {
            for (int c = 0; c < craftingSize; c++) {
                if (!matrix[r][c].getItemPair().id().equals(TypeId.from(Material.AIR))) {
                    minX = Math.min(minX, c);
                    maxX = Math.max(maxX, c);
                    minY = Math.min(minY, r);
                    maxY = Math.max(maxY, r);
                }
            }
        }

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        // Generate all possible shifted versions
        for (int dx = 0; dx <= craftingSize - width; dx++) {
            for (int dy = 0; dy <= craftingSize - height; dy++) {
                List<Ingredient> shifted = new ArrayList<>(Collections.nCopies(craftingSize * craftingSize, new Ingredient(new ItemPair(TypeId.from(Material.AIR), 0))));

                for (int r = minY; r <= maxY; r++) {
                    for (int c = minX; c <= maxX; c++) {
                        if (!matrix[r][c].getItemPair().id().equals(TypeId.from(Material.AIR))) {
                            int newRow = r - minY + dy;
                            int newCol = c - minX + dx;
                            shifted.set(newRow * craftingSize + newCol, matrix[r][c]);
                        }
                    }
                }

                variations.put(BlueprintLookupGenerator.toShapedKey(shifted.stream().map(Ingredient::getItemPair).toList()), shifted);
            }
        }

        return variations;
    }

    @Override
    protected List<Ingredient> getMatchedIngredientList(BlueprintContext context) {
        if (symmetrical) {
            return variations.get(context.getShapedLookupKey());
        }
        return ingredients;

    }

    @Override
    public Blueprint complete() {
        super.complete();
        if (symmetrical) {
            variations = generateShiftedIngredients(ingredients.toArray(new Ingredient[0]), workbench.getCraftingSize());
        }
        return this;
    }
}
