package gg.auroramc.crafting.api.vanilla;

import org.bukkit.inventory.FurnaceRecipe;

public class FurnaceRecipeBuilder extends CookingRecipeBuilder<FurnaceRecipe>{
    public FurnaceRecipeBuilder(String id) {
        super(id);
    }

    public static FurnaceRecipeBuilder furnaceRecipe(String id) {
        return new FurnaceRecipeBuilder(id);
    }

    @Override
    public FurnaceRecipe buildInternal() {
        return new FurnaceRecipe(key, result, predicateChoiceFor(input), experience, cookingTime);
    }
}
