package gg.auroramc.crafting.api.vanilla;

import gg.auroramc.aurora.api.item.TypeId;
import org.bukkit.inventory.StonecuttingRecipe;

public class StoneCutterRecipeBuilder extends RecipeBuilder<StoneCutterRecipeBuilder, StonecuttingRecipe> {
    private TypeId input;

    public static StoneCutterRecipeBuilder stoneCutterRecipe(String id) {
        return new StoneCutterRecipeBuilder(id);
    }

    public StoneCutterRecipeBuilder(String id) {
        super(id);
    }

    public StoneCutterRecipeBuilder input(TypeId input) {
        this.input = input;
        return this;
    }

    @Override
    public StonecuttingRecipe build() {
        return new StonecuttingRecipe(key, result, predicateChoiceFor(input));
    }
}
