package gg.auroramc.crafting.config.menu.vanilla;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.crafting.AuroraCrafting;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static gg.auroramc.crafting.config.ConfigManager.VANILLA_RECIPE_VIEW_PATH;

@Getter
public class SmithingRecipeViewConfig extends AuroraConfig {
    private String title = "Smithing Table Recipes";
    private Integer rows = 4;
    private Map<String, ItemConfig> customItems;

    private Items items;
    private Slots slots;

    @Getter
    public static final class Slots {
        private Integer template = 10;
        private Integer base = 11;
        private Integer addition = 12;
        private Integer result = 16;
        private Integer prevRecipe = 25;
        private Integer nextRecipe = 26;

    }

    @Getter
    public static final class Items {
        private ItemConfig filler;
        private ItemConfig back;
    }

    public SmithingRecipeViewConfig(AuroraCrafting plugin) {
        super(getFile(plugin));
    }

    public static File getFile(AuroraCrafting plugin) {
        return new File(plugin.getDataFolder() + "/" + VANILLA_RECIPE_VIEW_PATH, "smithing_table.yml");
    }

    public static void saveDefault(AuroraCrafting plugin) {
        if (!getFile(plugin).exists()) {
            plugin.saveResource(VANILLA_RECIPE_VIEW_PATH + "/smithing_table.yml", false);
        }
    }

    @Override
    protected List<Consumer<YamlConfiguration>> getMigrationSteps() {
        return List.of(
                (yaml) -> {
                    yaml.set("slots.prev-recipe", 25);
                    yaml.set("slots.next-recipe", 26);
                    yaml.set("config-version", 1);
                }
        );
    }
}