package gg.auroramc.crafting.config;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.decorators.IgnoreField;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.crafting.api.blueprint.ChoiceType;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class CraftingRecipesConfig extends AuroraConfig {
    @IgnoreField
    private String sourcePath;

    @IgnoreField
    private boolean vanilla;

    private List<RecipeConfig> recipes = new ArrayList<>();

    @Getter
    public static final class RecipeConfig {
        private String id;
        private String permission;
        private String workbench = "default";
        private Boolean shapeless = false;
        private Boolean symmetry = false;
        private String result;
        private VanillaOptions vanillaOptions = new VanillaOptions();
        private DisplayOptions displayOptions = new DisplayOptions();
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
        private String choiceType = ChoiceType.ITEM_TYPE.name();
    }

    @Getter
    public static final class DisplayOptions {
        private Map<String, ItemConfig> items = new HashMap<>();
        private List<String> lockedLore = new ArrayList<>();
    }

    public CraftingRecipesConfig(File file, boolean vanilla) {
        super(file);
        this.vanilla = vanilla;
        var target = "blueprints" + File.separator;
        var absPath = file.getAbsolutePath();
        var index = absPath.indexOf(target);
        this.sourcePath = absPath.substring(index + target.length()).replace(".yml", "").replace(File.separator, "/");
    }

    @Override
    public void load() {
        super.load();
        recipes.forEach(recipe -> recipe.setSourcePath(sourcePath));
    }
}
