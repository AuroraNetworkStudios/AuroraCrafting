package gg.auroramc.crafting.api.vanilla;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;

public abstract class RecipeBuilder<T extends RecipeBuilder<T, R>, R extends Recipe> {
    protected final NamespacedKey key;
    protected ItemStack result;

    public RecipeBuilder(String id) {
        this.key = new NamespacedKey("aurora", id);
    }

    public RecipeBuilder<T, R> result(ItemStack result) {
        this.result = result;
        return this;
    }

    public abstract R build();

    protected RecipeChoice exactChoiceFor(ItemStack item) {
        return item == null || item.isEmpty() ? RecipeChoice.empty() : RecipeChoice.exactChoice(item);
    }

    protected RecipeChoice dynamicChoiceFor(ItemStack item) {
        return item == null || item.isEmpty() ? RecipeChoice.empty() : new RecipeChoice.MaterialChoice(item.getType());
    }
}
