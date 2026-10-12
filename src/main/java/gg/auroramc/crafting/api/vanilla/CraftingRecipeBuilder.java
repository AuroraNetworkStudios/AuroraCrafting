package gg.auroramc.crafting.api.vanilla;


import lombok.Getter;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;

@Getter
@SuppressWarnings("unchecked")
public abstract class CraftingRecipeBuilder<T extends CraftingRecipe, R extends CraftingRecipeBuilder<T, R>> extends RecipeBuilder<CraftingRecipeBuilder<T, R>, T> {
    protected CraftingBookCategory category = CraftingBookCategory.MISC;
    protected String group = null;

    public CraftingRecipeBuilder(String id) {
        super(id);
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
