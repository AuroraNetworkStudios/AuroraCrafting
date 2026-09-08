package gg.auroramc.crafting.api.vanilla;

import org.bukkit.inventory.CampfireRecipe;

public class CampfireRecipeBuilder extends CookingRecipeBuilder<CampfireRecipe> {
    public CampfireRecipeBuilder(String id) {
        super(id);
    }

    public static CampfireRecipeBuilder campfireRecipe(String id) {
        return new CampfireRecipeBuilder(id);
    }

    @Override
    public CampfireRecipe buildInternal() {
        return new CampfireRecipe(key, result, predicateChoiceFor(input), experience, cookingTime);
    }
}
