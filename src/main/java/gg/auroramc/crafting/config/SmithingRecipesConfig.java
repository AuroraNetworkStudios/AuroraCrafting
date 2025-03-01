package gg.auroramc.crafting.config;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.decorators.IgnoreField;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.blueprint.ChoiceType;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class SmithingRecipesConfig extends AuroraConfig {
    @IgnoreField
    private String sourcePath;

    private List<RecipeConfig> recipes = new ArrayList<>();

    @Getter
    public static final class RecipeConfig {
        private String id;
        private String result;
        private String template;
        private String base;
        private String addition;
        private String permission;
        private DisplayOptions displayOptions = new DisplayOptions();
        private VanillaOptions vanillaOptions = new VanillaOptions();
        private Map<Integer, MergeOptions> mergeOptions;
        private List<String> onCraft;

        @Setter
        @IgnoreField
        private String sourcePath;
    }

    @Getter
    public static final class DisplayOptions {
        private Map<String, ItemConfig> items = new HashMap<>();
        private List<String> lockedLore = new ArrayList<>();
    }

    @Getter
    public static final class VanillaOptions {
        private String choiceType = ChoiceType.ITEM_TYPE.name();
        private String group;
    }

    public SmithingRecipesConfig(File file) {
        super(file);
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
