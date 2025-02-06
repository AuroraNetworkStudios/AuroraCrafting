package gg.auroramc.crafting.api.blueprint;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.crafting.api.ItemPair;
import gg.auroramc.crafting.api.workbench.Workbench;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.recipe.CraftingBookCategory;

@Getter
public class ShapedBlueprint extends CraftingBlueprint<ShapedBlueprint> {

    public ShapedBlueprint(Workbench workbench, String id) {
        super(workbench, id);
    }

    public static ShapedBlueprint shapedBlueprint(Workbench workbench, String id) {
        return new ShapedBlueprint(workbench, id);
    }

    @Override
    public int getTimesCraftable(BlueprintContext context) {
        int maxCraftable = Integer.MAX_VALUE;

        var matches = true;
        var items = context.getMatrix();

        for (int i = 0; i < items.length; i++) {
            var ingredient = ingredients.size() > i ? ingredients.get(i) : new ItemPair(TypeId.from(Material.AIR), 0);
            var item = items[i];
            var itemTypeId = item.isEmpty() ? TypeId.from(Material.AIR) : AuroraAPI.getItemManager().resolveId(item);
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
        var items = new ItemStack[context.getMatrix().length];
        var currentMatrix = context.getMatrix();

        for (int i = 0; i < context.getMatrix().length; i++) {
            var ingredient = ingredients.size() > i ? ingredients.get(i) : new ItemPair(TypeId.from(Material.AIR), 0);
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
}
