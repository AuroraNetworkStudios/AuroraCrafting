package gg.auroramc.crafting.api.blueprint;

import gg.auroramc.crafting.api.ItemPair;
import gg.auroramc.crafting.api.workbench.Workbench;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.recipe.CookingBookCategory;

@Getter
public class CookingBlueprint extends Blueprint {
    private VanillaOptions vanillaOptions = new VanillaOptions(CookingBookCategory.MISC, null, 200, 0);
    private Type type = Type.FURNACE;

    public record VanillaOptions(CookingBookCategory category, String group, int cookingTime, float experience) {
    }

    public enum Type {
        FURNACE,
        BLAST_FURNACE,
        SMOKER,
        CAMPFIRE
    }

    public CookingBlueprint(Workbench workbench, String id) {
        super(workbench, id);
    }

    public static CookingBlueprint cookingBlueprint(Workbench workbench, String id) {
        return new CookingBlueprint(workbench, id);
    }

    public CookingBlueprint vanillaOptions(VanillaOptions vanillaOptions) {
        this.vanillaOptions = vanillaOptions;
        return this;
    }

    public CookingBlueprint type(Type type) {
        this.type = type;
        return this;
    }

    public CookingBlueprint input(ItemPair input) {
        if (!this.ingredients.isEmpty()) {
            throw new IllegalStateException("Input already set");
        }
        this.addIngredient(input);
        return this;
    }

    public ItemStack input() {
        return this.ingredientItems.getFirst();
    }

    @Override
    public int getTimesCraftable(BlueprintContext context) {
        return 0;
    }

    @Override
    public ItemStack[] calcRemainingIngredientMatrix(BlueprintContext context, int timesCrafted) {
        return new ItemStack[0];
    }
}
