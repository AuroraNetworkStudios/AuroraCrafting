package gg.auroramc.crafting.api.vanilla;

import gg.auroramc.crafting.api.blueprint.ChoiceType;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.recipe.CookingBookCategory;

import java.util.function.Function;

public abstract class CookingRecipeBuilder<T extends CookingRecipe<T>> extends RecipeBuilder<CookingRecipeBuilder<T>, CookingRecipe<T>> {
    protected ItemStack input = null;
    protected float experience = 0;
    protected int cookingTime = 200;
    protected CookingBookCategory category = CookingBookCategory.MISC;
    private String group;
    protected final Function<ItemStack, RecipeChoice> choiceSelector;
    private final ChoiceType choiceType;

    public CookingRecipeBuilder(String id, ChoiceType choiceType) {
        super(id);
        this.choiceType = choiceType;

        switch (choiceType) {
            case ITEM_TYPE -> choiceSelector = this::dynamicChoiceFor;
            case EXACT -> choiceSelector = this::exactChoiceFor;
            default -> throw new IllegalArgumentException("Invalid choice type: " + choiceType);
        }
    }

    public CookingRecipeBuilder(String id) {
        this(id, ChoiceType.EXACT);
    }

    public CookingRecipeBuilder<T> category(CookingBookCategory category) {
        this.category = category;
        return this;
    }

    public CookingRecipeBuilder<T> group(String group) {
        this.group = group;
        return this;
    }

    public CookingRecipeBuilder<T> input(ItemStack input) {
        this.input = input;
        return this;
    }

    public CookingRecipeBuilder<T> experience(float experience) {
        this.experience = experience;
        return this;
    }

    public CookingRecipeBuilder<T> cookingTime(int cookingTime) {
        this.cookingTime = cookingTime;
        return this;
    }

    protected abstract CookingRecipe<T> buildInternal();

    public CookingRecipe<T> build() {
        var recipe = buildInternal();
        recipe.setCategory(category);
        if (choiceType == ChoiceType.ITEM_TYPE) {
            recipe.setExperience(0);
        }
        if (group != null) {
            recipe.setGroup(group);
        }
        return recipe;
    }
}
