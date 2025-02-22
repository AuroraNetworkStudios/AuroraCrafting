package gg.auroramc.crafting.config;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.decorators.IgnoreField;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.crafting.AuroraCrafting;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CauldronRecipesConfig extends AuroraConfig {

    @IgnoreField
    private String sourcePath;
    private List<RecipeConfig> recipes = new ArrayList<>();
    @Getter
    public static final class RecipeConfig {

        private String id;
        private String result;
        private String input;
        private float experience = 0.0F;
        private int fluidLevel = 0;
        private String fluid;
        private String category;
        
        private DisplayOptions displayOptions = new DisplayOptions();

        @Setter
        @IgnoreField
        private String sourcepath;
    }

    public CauldronRecipesConfig(File file) {
        super(file);
        var target = "blueprints" + File.separator;
        var absPath = file.getAbsolutePath();
        var index = absPath.indexOf(target);
        this.sourcePath = absPath.substring(index + target.length()).replace(".yml", "");
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

        Iterator<RecipeConfig> iterator = recipes.iterator();

        while (iterator.hasNext()) {
            RecipeConfig recipe = iterator.next();

            if (recipe.id == null) {
                iterator.remove();
                AuroraCrafting.logger().severe("Cauldron recipe in " + sourcePath + " has no id, removing...");
            } else if (recipe.result == null) {
                iterator.remove();
                AuroraCrafting.logger().severe("Cauldron recipe in " + sourcePath + " with id " + recipe.id + " has no result, removing...");
            } else if (recipe.input == null) {
                iterator.remove();
                AuroraCrafting.logger().severe("Cauldron recipe in " + sourcePath + " with id " + recipe.id + " has no input, removing...");
            }
        }
    }

}