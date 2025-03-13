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
@Setter
public class GrindstoneRecipesConfig extends AuroraConfig {

    @IgnoreField
    private String sourcePath;
    private List<RecipeConfig> recipes = new ArrayList<>();

    /**
     * recipes:
     *   - id: grinded_diamond
     *     input: ["minecraft:diamond", "minecraft:emerald"]
     *     result: "minecraft:prismarine_shard"
     *     # Every field is optional below
     *     experience: 10
     */

    @Getter
    public static final class RecipeConfig {

        private List<String> input;
        private String result;
        private float experience = 0.0F;

        private String id;
        private String group;
        private String category;

        private DisplayOptions displayOptions = new DisplayOptions();

        @Setter
        @IgnoreField
        private String sourcepath;
    }

    public GrindstoneRecipesConfig(File file) {
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
        recipes.forEach(recipe -> recipe.setSourcepath(sourcePath));
    }

}