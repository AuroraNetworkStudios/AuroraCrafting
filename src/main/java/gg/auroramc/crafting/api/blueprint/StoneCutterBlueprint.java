package gg.auroramc.crafting.api.blueprint;

import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.crafting.api.ItemPair;
import gg.auroramc.crafting.api.workbench.Workbench;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public class StoneCutterBlueprint extends Blueprint {

    public static StoneCutterBlueprint stoneCutterBlueprint(Workbench workbench, String id) {
        return new StoneCutterBlueprint(workbench, id);
    }

    public StoneCutterBlueprint(Workbench workbench, String id) {
        super(workbench, id);
    }

    public StoneCutterBlueprint input(ItemPair input) {
        if (!this.ingredients.isEmpty()) {
            throw new IllegalStateException("Input already set");
        }
        this.addIngredient(input);
        return this;
    }

    public ItemStack getInputItem() {
        return this.ingredientItems.getFirst();
    }

    public TypeId getInput() {
        return this.ingredients.getFirst().getItemPair().id();
    }

    @Override
    public int getTimesCraftable(BlueprintContext context) {
        // Not applicable, since stonecutter recipes handled purely by vanilla
        return 0;
    }

    @Override
    public ItemStack[] calcRemainingIngredientMatrix(BlueprintContext context, int timesCrafted) {
        // Not applicable, since stonecutter recipes handled purely by vanilla
        return new ItemStack[0];
    }
}
