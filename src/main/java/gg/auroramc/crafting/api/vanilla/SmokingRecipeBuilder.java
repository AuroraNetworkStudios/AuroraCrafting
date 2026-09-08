package gg.auroramc.crafting.api.vanilla;

import org.bukkit.inventory.SmokingRecipe;
import org.bukkit.inventory.recipe.CookingBookCategory;

public class SmokingRecipeBuilder extends CookingRecipeBuilder<SmokingRecipe>{
    public SmokingRecipeBuilder(String id) {
        super(id);
        this.category = CookingBookCategory.FOOD;
    }

    public static SmokingRecipeBuilder smokingRecipe(String id) {
        return new SmokingRecipeBuilder(id);
    }

    @Override
    public SmokingRecipe buildInternal() {
        return new SmokingRecipe(key, result, predicateChoiceFor(input), experience, cookingTime);
    }
}
