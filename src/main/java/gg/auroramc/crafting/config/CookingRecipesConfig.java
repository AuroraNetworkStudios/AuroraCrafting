package gg.auroramc.crafting.config;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.decorators.IgnoreField;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class CookingRecipesConfig extends AuroraConfig {
    @IgnoreField
    private String sourcePath;

    private List<RecipeConfig> recipes = new ArrayList<>();

    @Getter
    public static final class RecipeConfig {
        private String id;
        private String result;
        private String input;
        private Float experience = 0.0F;
        private Integer cookingTime = 200;
        private String category = "MISC";
        private String group;
        private DisplayOptions displayOptions = new DisplayOptions();

        @Setter
        @IgnoreField
        private String sourcePath;
    }

    public CookingRecipesConfig(File file) {
        super(file);
        var target = "blueprints" + File.separator;
        var absPath = file.getAbsolutePath();
        var index = absPath.indexOf(target);
        this.sourcePath = absPath.substring(index + target.length()).replace(".yml", "").replace(File.separator, "/");
    }

    @Getter
    public static final class DisplayOptions {
        private Map<String, ItemConfig> items = new HashMap<>();
        private List<String> lockedLore = new ArrayList<>();
    }

    @Override
    public void load() {
        super.load();
        recipes.forEach(recipe -> recipe.setSourcePath(sourcePath));
    }
}
