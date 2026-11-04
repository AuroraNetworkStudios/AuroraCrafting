package gg.auroramc.crafting.config.menu.vanilla;

import gg.auroramc.aurora.api.config.premade.ItemConfig;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A recipe view of a vanilla workbench (menus/vanilla_recipe_view/*.yml). Every view has these; each kind adds the
 * slots of the recipe's own items. Missing values come from the bundled copy of the file.
 */
@Getter
public abstract class RecipeViewConfig {
    private String title;
    private Integer rows;
    private Map<String, ItemConfig> customItems;
    private Items items;

    public abstract Slots getSlots();

    @Getter
    public static final class Items {
        private ItemConfig filler;
        private ItemConfig back;
        // Furnace, blast furnace and smoker only
        private ItemConfig fuel;
    }

    @Getter
    public static class Slots {
        private Integer prevRecipe;
        private Integer nextRecipe;
    }

    /**
     * The first migration of most views: the slots of the previous/next recipe buttons.
     */
    public static List<Consumer<YamlConfiguration>> pagingMigration(int prevRecipe, int nextRecipe) {
        return List.of(
                (yaml) -> {
                    yaml.set("slots.prev-recipe", prevRecipe);
                    yaml.set("slots.next-recipe", nextRecipe);
                    yaml.set("config-version", 1);
                }
        );
    }
}
