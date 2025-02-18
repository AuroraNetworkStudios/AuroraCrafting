package gg.auroramc.crafting.api.vanilla;


import gg.auroramc.crafting.api.blueprint.ChoiceType;
import lombok.Getter;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.recipe.CraftingBookCategory;

import java.util.function.Function;

@Getter
@SuppressWarnings("unchecked")
public abstract class CraftingRecipeBuilder<T extends CraftingRecipe, R extends CraftingRecipeBuilder<T, R>> extends RecipeBuilder<CraftingRecipeBuilder<T, R>, T> {
    protected CraftingBookCategory category = CraftingBookCategory.MISC;
    protected String group = null;
    protected final Function<ItemStack, RecipeChoice> choiceSelector;

    public CraftingRecipeBuilder(String id, ChoiceType choiceType) {
        super(id);

        switch (choiceType) {
            case ITEM_TYPE -> choiceSelector = this::dynamicChoiceFor;
            case EXACT -> choiceSelector = this::exactChoiceFor;
            default -> throw new IllegalArgumentException("Invalid choice type: " + choiceType);
        }
    }

    public R category(CraftingBookCategory category) {
        this.category = category;
        return (R) this;
    }

    public R group(String group) {
        this.group = group;
        return (R) this;
    }
}
