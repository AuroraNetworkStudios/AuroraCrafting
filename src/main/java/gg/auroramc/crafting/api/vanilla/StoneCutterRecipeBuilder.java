package gg.auroramc.crafting.api.vanilla;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.StonecuttingRecipe;

public class StoneCutterRecipeBuilder extends RecipeBuilder<StoneCutterRecipeBuilder, StonecuttingRecipe> {
    private ItemStack input;
    private String group = null;

    public static StoneCutterRecipeBuilder stoneCutterRecipe(String id) {
        return new StoneCutterRecipeBuilder(id);
    }

    public StoneCutterRecipeBuilder(String id) {
        super(id);
    }

    public StoneCutterRecipeBuilder input(ItemStack input) {
        this.input = input;
        return this;
    }

    public StoneCutterRecipeBuilder group(String group) {
        this.group = group;
        return this;
    }

    @Override
    public StonecuttingRecipe build() {
        var recipe = new StonecuttingRecipe(key, result, exactChoiceFor(input));
        if (group != null) {
            recipe.setGroup(group);
        }
        return recipe;
    }
}
