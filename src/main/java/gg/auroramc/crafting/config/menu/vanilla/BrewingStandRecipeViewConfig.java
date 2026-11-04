package gg.auroramc.crafting.config.menu.vanilla;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.function.Consumer;

@Getter
public class BrewingStandRecipeViewConfig extends RecipeViewConfig {
    private Slots slots;

    @Getter
    public static final class Slots extends RecipeViewConfig.Slots {
        private List<Integer> result;
        private List<Integer> input;
        private Integer ingredient;
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
