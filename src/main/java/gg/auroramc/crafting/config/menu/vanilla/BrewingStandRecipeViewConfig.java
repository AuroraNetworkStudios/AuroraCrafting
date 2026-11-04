package gg.auroramc.crafting.config.menu.vanilla;

import gg.auroramc.aurora.api.config.premade.ItemConfig;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class BrewingStandRecipeViewConfig {
    private String title = "Brewing recipes";
    private Integer rows = 6;
    private Map<String, ItemConfig> customItems;

    private Items items;
    private Slots slots;

    @Getter
    public static final class Slots {
        private List<Integer> result = List.of(40);
        private List<Integer> input = List.of(22);
        private Integer ingredient = 13;
        private Integer prevRecipe = 52;
        private Integer nextRecipe = 53;
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
                    var oldInput = yaml.getInt("slots.input", 13);
                    var oldIngredient = yaml.getIntegerList("slots.ingredient");

                    yaml.set("slots.input", oldIngredient);
                    yaml.set("slots.ingredient", oldInput);

                    yaml.set("config-version", 1);
                }
        );
    }
}
