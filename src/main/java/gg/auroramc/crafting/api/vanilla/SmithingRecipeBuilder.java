package gg.auroramc.crafting.api.vanilla;

import gg.auroramc.aurora.api.item.TypeId;
import org.bukkit.inventory.SmithingTransformRecipe;

public class SmithingRecipeBuilder extends RecipeBuilder<SmithingRecipeBuilder, SmithingTransformRecipe> {
    private TypeId template = null;
    private TypeId base = null;
    private TypeId addition = null;

    public SmithingRecipeBuilder(String id) {
        super(id);
    }

    public static SmithingRecipeBuilder smithingRecipe(String id) {
        return new SmithingRecipeBuilder(id);
    }

    public SmithingRecipeBuilder template(TypeId template) {
        this.template = template;
        return this;
    }

    public SmithingRecipeBuilder base(TypeId base) {
        this.base = base;
        return this;
    }

    public SmithingRecipeBuilder addition(TypeId addition) {
        this.addition = addition;
        return this;
    }

    @Override
    public SmithingTransformRecipe build() {
        return new SmithingTransformRecipe(
                key,
                result,
                predicateChoiceFor(template),
                predicateChoiceFor(base),
                predicateChoiceFor(addition),
                true
        );
    }
}
