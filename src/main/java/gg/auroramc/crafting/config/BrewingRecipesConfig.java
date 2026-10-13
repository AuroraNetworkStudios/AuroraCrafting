package gg.auroramc.crafting.config;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.decorators.IgnoreField;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
public class BrewingRecipesConfig extends AuroraConfig {
    @IgnoreField
    private String sourcePath;

    private List<RecipeConfig> recipes = new ArrayList<>();

    public BrewingRecipesConfig(File file) {
        super(file);
        var target = "blueprints" + File.separator;
        var absPath = file.getAbsolutePath();
        var index = absPath.indexOf(target);
        this.sourcePath = absPath.substring(index + target.length()).replace(".yml", "").replace(File.separator, "/");
    }

    @Getter
    public static final class RecipeConfig {
        private String id;
        private String ingredient;
        private String input;
        private String result;
        private SmithingRecipesConfig.DisplayOptions displayOptions = new SmithingRecipesConfig.DisplayOptions();
        private List<String> onCraft;
        private String group;

        @Setter
        @IgnoreField
        private String sourcePath;
    }

    @Override
    public void load() {
        super.load();
        recipes.forEach(recipe -> recipe.setSourcePath(sourcePath));
    }
}
