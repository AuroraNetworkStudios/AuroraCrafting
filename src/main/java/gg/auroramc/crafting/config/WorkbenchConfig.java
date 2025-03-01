package gg.auroramc.crafting.config;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.decorators.IgnoreField;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.crafting.AuroraCrafting;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Getter
@Setter
public class WorkbenchConfig extends AuroraConfig {
    @IgnoreField
    private final String id;

    private ItemConfig filler;
    private Map<String, ItemConfig> customItems;
    private String title;
    private Integer rows;
    private Integer resultSlot = 25;
    private List<Integer> matrixSlots;
    private List<Integer> completionIndicatorSlots;
    private List<Integer> quickCraftingSlots;
    private ItemConfig invalidResultItem;
    private ItemConfig backItem;
    private ItemConfig emptyQuickCraftItem;
    private ItemConfig noPermissionQuickCraftItem;
    private ItemConfig blueprintCompletedItem;
    private ItemConfig blueprintNotCompletedItem;
    private ItemConfig nextRecipeItem;
    private ItemConfig previousRecipeItem;
    private String commandCompletion;
    private Boolean includeVanillaRecipesInQuickCrafting = false;
    private RecipeBookOptions recipeBook = new RecipeBookOptions();

    @Getter
    public static final class RecipeBookOptions {
        private String title = "Crafting recipe";
        private Integer prevRecipeSlot = 52;
        private Integer nextRecipeSlot = 53;
        private Integer resultSlot = 25;
        private Integer backSlot = 45;
        private Map<String, ItemConfig> customItems = new HashMap<>();
    }

    public WorkbenchConfig(File file, String id) {
        super(file);
        this.id = id;
    }

    @Override
    public void load() {
        super.load();
        if (commandCompletion == null) {
            commandCompletion = id;
        }
    }

    @Override
    protected List<Consumer<YamlConfiguration>> getMigrationSteps() {
        return List.of(
                yaml -> {
                    yaml.set("recipe-book.title", "Crafting recipe");
                    yaml.set("recipe-book.back-slot", 45);
                    yaml.set("recipe-book.result-slot", 25);
                    yaml.set("recipe-book.prev-recipe-slot", yaml.getInt("prev-recipe-slot", 52));
                    yaml.set("recipe-book.next-recipe-slot", yaml.getInt("next-recipe-slot", 53));
                    yaml.set("prev-recipe-slot", null);
                    yaml.set("next-recipe-slot", null);

                    var oldFile = new File(AuroraCrafting.getInstance().getDataFolder() + "/menus", "recipe_view.yml");
                    if (oldFile.exists()) {
                        var oldYaml = YamlConfiguration.loadConfiguration(oldFile);
                        yaml.set("recipe-book.custom-items", oldYaml.getConfigurationSection("custom-items"));
                    } else {
                        yaml.set("recipe-book.custom-items", new HashMap<>());
                    }

                    yaml.set("config-version", 1);
                }
        );
    }
}
