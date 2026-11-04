package gg.auroramc.crafting.config;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Getter
public class DisabledRecipesConfig {
    private Set<String> recipes = new HashSet<>();
    private Set<String> smithingRecipes = new HashSet<>();

    // Steps by config-version, only ever appended, each sets config-version itself
    public static List<Consumer<YamlConfiguration>> migrations() {
        return List.of(
                (yaml) -> {
                    yaml.set("smithing-recipes", List.of());
                    yaml.setComments("smithing-recipes",
                            List.of(
                                    "You can also disable smithing recipes by the result.",
                                    "You need to write item IDs here for example: \"mythicmobs:enchanted_netherite_helmet\""
                            )
                    );
                    yaml.set("config-version", 1);
                }
        );
    }
}
