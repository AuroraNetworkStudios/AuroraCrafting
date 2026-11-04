package gg.auroramc.crafting.config.menu.vanilla;

import gg.auroramc.aurora.api.config.premade.ItemConfig;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class CampfireRecipeViewConfig {
    private String title = "Campfire Recipes";
    private Integer rows = 3;
    private Map<String, ItemConfig> customItems;

    private Items items;
    private Slots slots;

    @Getter
    public static final class Slots {
        private Integer result = 15;
        private Integer input = 11;
        private Integer prevRecipe = 25;
        private Integer nextRecipe = 26;
    }

    @Getter
    public static final class Items {
        private ItemConfig filler;
        private ItemConfig back;
    }

    // Steps by config-version, only ever appended, each sets config-version itself
    public static List<Consumer<YamlConfiguration>> migrations() {
        return List.of(
                (yaml) -> {
                    yaml.set("slots.prev-recipe", 25);
                    yaml.set("slots.next-recipe", 26);
                    yaml.set("config-version", 1);
                }
        );
    }
}