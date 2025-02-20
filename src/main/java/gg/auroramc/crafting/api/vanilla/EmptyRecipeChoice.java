package gg.auroramc.crafting.api.vanilla;

import gg.auroramc.aurora.api.util.Version;
import org.bukkit.Material;
import org.bukkit.inventory.RecipeChoice;

public class EmptyRecipeChoice {
    private static RecipeChoice empty = null;

    public static RecipeChoice get() {
        if (empty != null) {
            return empty;
        }

        if (Version.isAtLeastVersion(20, 6)) {
            empty = RecipeChoice.empty();
            return empty;
        } else {
            empty = new RecipeChoice.MaterialChoice(Material.AIR);
            return empty;
        }
    }
}
