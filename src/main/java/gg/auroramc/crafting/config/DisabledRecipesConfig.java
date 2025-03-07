package gg.auroramc.crafting.config;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.crafting.AuroraCrafting;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Getter
public class DisabledRecipesConfig extends AuroraConfig {
    private Set<String> recipes = new HashSet<>();
    private Set<String> smithingRecipes = new HashSet<>();

    public DisabledRecipesConfig(File file) {
        super(file);
    }

    public DisabledRecipesConfig(AuroraCrafting plugin) {
        super(getFile(plugin));
    }

    public static File getFile(AuroraCrafting plugin) {
        return new File(plugin.getDataFolder(), "disabled_recipes.yml");
    }

    public static void saveDefault(AuroraCrafting plugin) {
        if (!getFile(plugin).exists()) {
            plugin.saveResource("disabled_recipes.yml", false);
        }
    }

    @Override
    protected List<Consumer<YamlConfiguration>> getMigrationSteps() {
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
