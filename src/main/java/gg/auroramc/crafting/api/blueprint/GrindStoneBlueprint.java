package gg.auroramc.crafting.api.blueprint;

import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.crafting.api.ItemPair;
import gg.auroramc.crafting.api.workbench.Workbench;
import gg.auroramc.crafting.api.workbench.vanilla.Grindstone;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Getter
public class GrindStoneBlueprint extends Blueprint {
    public GrindStoneBlueprint(Workbench workbench, String id) {
        super(workbench, id);
    }

    @Override
    public GrindStoneBlueprint addIngredient(ItemPair itemPair) {
        if (this.ingredients.size() >= 2) {
            throw new IllegalArgumentException("Grindstone recipes can only have 2 ingredients");
        }
        super.addIngredient(itemPair);

        return this;
    }

    @Override
    public int getTimesCraftable(BlueprintContext context) {
        int maxCraftable = Integer.MAX_VALUE;
        ItemStack[] items = context.getMatrix();
        ItemPair[] idMatrix = context.getIdMatrix();

        for (int i = 0; i < items.length; i++) {
            ItemPair ingredient = ingredients.size() > i ? ingredients.get(i).getItemPair() : BlueprintContext.AIR;
            ItemStack item = items[i];
            TypeId itemTypeId = item.isEmpty() ? TypeId.from(Material.AIR) : idMatrix[i].id();

            if (!itemTypeId.equals(ingredient.id()) || item.getAmount() < ingredient.amount()) {
                return 0;
            }

            if (!ingredient.id().id().equals("air")) {
                int craftable = item.getAmount() / Math.max(1, ingredient.amount());
                maxCraftable = Math.min(maxCraftable, craftable);
            }
        }

        return maxCraftable;
    }

    @Override
    public ItemStack[] calcRemainingIngredientMatrix(BlueprintContext context, int timesCrafted) {
        ItemStack[] items = new ItemStack[context.getMatrix().length];
        ItemStack[] currentMatrix = context.getMatrix();

        for (int i = 0; i < currentMatrix.length; i++) {
            ItemPair ingredient = ingredients.size() > i ? ingredients.get(i).getItemPair() : BlueprintContext.AIR;
            ItemStack item = currentMatrix[i];
            int requiredAmount = ingredient.amount() * timesCrafted;

            if (item.getAmount() > requiredAmount) {
                ItemStack newItem = item.clone();
                newItem.setAmount(item.getAmount() - requiredAmount);
                items[i] = newItem;
            } else {
                items[i] = null;
            }
        }

        return items;
    }

    @Override
    public Blueprint complete() {
        super.complete();
        this.mergeOptionsEnabled = false;
        return this;
    }


    // REGION: builder

    public static GrindStoneBlueprint grindStoneBlueprint(Workbench workbench, String id) {
        return new GrindStoneBlueprint(workbench, id);
    }

    // ENDREGION: builder
}