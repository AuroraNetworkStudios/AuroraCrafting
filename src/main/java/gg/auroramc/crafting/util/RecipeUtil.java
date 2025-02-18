package gg.auroramc.crafting.util;

import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Recipe;

import java.util.*;

public class RecipeUtil {
    private static final Map<String, Recipe> removedRecipes = Maps.newHashMap();

    public static void removeVanillaRecipes(Set<String> recipes) {
        var removedEntries = new HashMap<>(removedRecipes);
        for (var recipe : removedEntries.entrySet()) {
            if (!recipes.contains(recipe.getKey())) {
                Bukkit.addRecipe(recipe.getValue());
                removedRecipes.remove(recipe.getKey());
            }
        }

        for (var recipe : recipes) {
            if (removedRecipes.containsKey(recipe)) {
                continue;
            }
            var recipeKey = NamespacedKey.fromString(recipe);
            var oldRecipe = Bukkit.getRecipe(recipeKey);

            var success = Bukkit.removeRecipe(recipeKey);
            if (success) {
                removedRecipes.put(recipe, oldRecipe);
            }
        }

        Bukkit.updateRecipes();
    }
}
