package gg.auroramc.crafting.api.vanilla;

import gg.auroramc.crafting.api.blueprint.ChoiceType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingTransformRecipe;

import java.util.function.Function;

public class SmithingRecipeBuilder extends RecipeBuilder<SmithingRecipeBuilder, SmithingTransformRecipe> {
    private ItemStack template = null;
    private ItemStack base = null;
    private ItemStack addition = null;
    protected final Function<ItemStack, RecipeChoice> choiceSelector;

    public SmithingRecipeBuilder(String id, ChoiceType choiceType) {
        super(id);

        switch (choiceType) {
            case ITEM_TYPE -> choiceSelector = this::dynamicChoiceFor;
            case EXACT -> choiceSelector = this::exactChoiceFor;
            default -> throw new IllegalArgumentException("Invalid choice type: " + choiceType);
        }
    }

    public static SmithingRecipeBuilder smithingRecipe(String id, ChoiceType choiceType) {
        return new SmithingRecipeBuilder(id, choiceType);
    }

    public SmithingRecipeBuilder template(ItemStack template) {
        this.template = template;
        return this;
    }

    public SmithingRecipeBuilder base(ItemStack base) {
        this.base = base;
        return this;
    }

    public SmithingRecipeBuilder addition(ItemStack addition) {
        this.addition = addition;
        return this;
    }

    @Override
    public SmithingTransformRecipe build() {
        return new SmithingTransformRecipe(
                key,
                result,
                choiceSelector.apply(template),
                choiceSelector.apply(base),
                choiceSelector.apply(addition),
                true
        );
    }
}

