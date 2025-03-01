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
public class BlastFurnaceRecipeViewConfig extends AuroraConfig {
    private String title = "Blast Furnace Recipes";
    private Integer rows = 4;
    private Map<String, ItemConfig> customItems;

    private Items items;
    private Slots slots;

    @Getter
    public static final class Slots {
        private Integer result = 15;
        private Integer fuel = 20;
        private Integer input = 11;
        private Integer prevRecipe = 34;
        private Integer nextRecipe = 35;
    }

    @Getter
    public static final class Items {
        private ItemConfig filler;
        private ItemConfig fuel;
        private ItemConfig back;
    }

    public BlastFurnaceRecipeViewConfig(AuroraCrafting plugin) {
        super(getFile(plugin));
    }

    public static File getFile(AuroraCrafting plugin) {
        return new File(plugin.getDataFolder() + "/" + VANILLA_RECIPE_VIEW_PATH, "blast_furnace.yml");
    }

    public static void saveDefault(AuroraCrafting plugin) {
        if (!getFile(plugin).exists()) {
            plugin.saveResource(VANILLA_RECIPE_VIEW_PATH + "/blast_furnace.yml", false);
        }
    }

    @Override
    protected List<Consumer<YamlConfiguration>> getMigrationSteps() {
        return List.of(
                (yaml) -> {
                    yaml.set("slots.prev-recipe", 34);
                    yaml.set("slots.next-recipe", 35);
                    yaml.set("config-version", 1);
                }
        );
    }
}