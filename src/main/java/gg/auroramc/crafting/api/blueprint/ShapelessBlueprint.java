package gg.auroramc.crafting.api.blueprint;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.crafting.api.workbench.Workbench;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.recipe.CraftingBookCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

@Getter
public class ShapelessBlueprint extends CraftingBlueprint<ShapelessBlueprint> {

    public ShapelessBlueprint(Workbench workbench, String id) {
        super(workbench, id);
    }

    public static ShapelessBlueprint shapelessBlueprint(Workbench workbench, String id) {
        return new ShapelessBlueprint(workbench, id);
    }

    @Override
    public int getTimesCraftable(BlueprintContext context) {
        var items = Arrays.copyOf(context.getIdMatrix(), context.getIdMatrix().length);
        var ingredients = new ArrayList<>(this.ingredients);
        var currentMatrix = context.getMatrix();

        Arrays.sort(items, Comparator.comparing(a -> a.id().toString()));
        ingredients.sort(Comparator.comparing(a -> a.id().toString()));

        int maxCraftable = Integer.MAX_VALUE;
        var matches = true;

        for (int i = 0; i < currentMatrix.length; i++) {
            var ingredient = ingredients.size() > i ? ingredients.get(i) : BlueprintContext.AIR;
            var item = items.length > i && items[i] != null ? items[i] : BlueprintContext.AIR;
            var itemTypeId = item.id();
            if (!itemTypeId.equals(ingredient.id())) {
                matches = false;
                break;
            } else if (item.amount() < ingredient.amount()) {
                matches = false;
                break;
            } else if (!ingredient.id().id().equals("air")) {
                maxCraftable = Math.min(maxCraftable, Math.max(1, item.amount()) / Math.max(1, ingredient.amount()));
            }
        }

        if (!matches) return 0;

        return maxCraftable;
    }

    @Override
    public ItemStack[] calcRemainingIngredientMatrix(BlueprintContext context, int timesCrafted) {
        var currentMatrix = context.getMatrix();
        var remainingItems = new ItemStack[currentMatrix.length];
        var ingredientsCopy = new ArrayList<>(ingredients); // Create a copy of ingredients

        // Copy the matrix to maintain the order, then process each item to deduct ingredients
        for (int i = 0; i < currentMatrix.length; i++) {
            var item = currentMatrix[i];
            if (item == null || item.getAmount() == 0) {
                remainingItems[i] = null;
                continue;
            }

            var itemId = AuroraAPI.getItemManager().resolveId(item);

            // Find matching ingredient for the current item
            for (var ingredient : ingredientsCopy) {
                if (ingredient.id().equals(itemId)) {
                    int requiredAmount = ingredient.amount() * timesCrafted;
                    int currentAmount = item.getAmount();

                    if (currentAmount <= requiredAmount) {
                        remainingItems[i] = null; // All used up
                    } else {
                        var newItem = item.clone();
                        newItem.setAmount(currentAmount - requiredAmount);
                        remainingItems[i] = newItem;
                    }

                    // Remove the ingredient from the copy list after deducting
                    ingredientsCopy.remove(ingredient);
                    break;
                }
            }
        }

        return remainingItems;
    }
}
