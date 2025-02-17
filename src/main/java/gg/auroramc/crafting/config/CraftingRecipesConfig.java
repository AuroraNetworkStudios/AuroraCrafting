package gg.auroramc.crafting.config;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.decorators.IgnoreField;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.crafting.AuroraCrafting;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class CraftingRecipesConfig extends AuroraConfig {
    @IgnoreField
    private String sourcePath;

    private List<RecipeConfig> recipes = new ArrayList<>();

    @Getter
    public static final class RecipeConfig {
        private String id;
        private String permission;
        private String workbench;
        private Boolean shapeless = false;
        private String result;
        private VanillaOptions vanillaOptions;
        private DisplayOptions displayOptions;
        private Map<Integer, MergeOptions> mergeOptions;
        private List<String> ingredients;
        private List<String> onCraft;
        // This is legacy for backwards compatibility
        private List<String> lockedLore = new ArrayList<>();

        @Setter
        @IgnoreField
        private String sourcePath;
    }

    @Getter
    public static final class VanillaOptions {
        private String category;
        private String group;
        private String choiceType;
    }

    @Getter
    public static final class MergeOptions {
        private Boolean enchants = false;
        private Boolean trim = false;
    }

    @Getter
    public static final class DisplayOptions {
        private Map<String, ItemConfig> items;
        private List<String> lockedLore = new ArrayList<>();
    }

    public CraftingRecipesConfig(File file) {
        super(file);
        var target = "blueprints" + File.separator;
        var absPath = file.getAbsolutePath();
        var index = absPath.indexOf(target);
        this.sourcePath = absPath.substring(index + target.length()).replace(".yml", "");
    }

    @Override
    public void load() {
        super.load();
        recipes.forEach(recipe -> {
            recipe.setSourcePath(sourcePath);
            var matrixSize = AuroraCrafting.getInstance().getConfigManager().getWorkbenchConfig().get(recipe.getWorkbench()).getMatrixSlots().size();
            if (!recipe.getShapeless() && recipe.getIngredients().size() < matrixSize) {
                for (int i = recipe.getIngredients().size(); i < matrixSize; i++) {
                    recipe.getIngredients().add("");
                }
            }
        });
    }
}
