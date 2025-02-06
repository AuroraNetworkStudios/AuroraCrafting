package gg.auroramc.crafting.api.blueprint;

import gg.auroramc.crafting.api.workbench.Workbench;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.recipe.CookingBookCategory;

public class CookingBlueprint extends Blueprint {
    private CookingBookCategory vanillaCategory = CookingBookCategory.MISC;
    private String vanillaGroup;

    public CookingBlueprint(Workbench workbench, String id) {
        super(workbench, id);
    }

    public static CookingBlueprint cookingBlueprint(Workbench workbench, String id) {
        return new CookingBlueprint(workbench, id);
    }

    public CookingBlueprint vanillaCategory(CookingBookCategory category) {
        this.vanillaCategory = category;
        return this;
    }

    public CookingBlueprint vanillaGroup(String group) {
        this.vanillaGroup = group;
        return this;
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
