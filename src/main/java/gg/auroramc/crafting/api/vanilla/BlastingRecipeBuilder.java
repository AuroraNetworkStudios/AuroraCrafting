package gg.auroramc.crafting.api.vanilla;

import org.bukkit.inventory.BlastingRecipe;

public class BlastingRecipeBuilder extends CookingRecipeBuilder<BlastingRecipe> {
    public BlastingRecipeBuilder(String id) {
        super(id);
    }

    @Override
    public BlastingRecipe buildInternal() {
        return new BlastingRecipe(key, result, predicateChoiceFor(input), experience, cookingTime);
    }

    public static BlastingRecipeBuilder blastingRecipe(String id) {
        return new BlastingRecipeBuilder(id);
    }
}
