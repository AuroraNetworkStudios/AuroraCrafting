package gg.auroramc.crafting.config;

import gg.auroramc.aurora.api.config.ConfigLoader;
import gg.auroramc.aurora.api.config.decorators.IgnoreField;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
public class StoneCutterRecipesConfig {
    @IgnoreField
    private String sourcePath;

    private List<RecipeConfig> recipes = new ArrayList<>();

    @Getter
    public static final class RecipeConfig {
        private String id;
        private String result;
        private String input;
        private DisplayOptions displayOptions = new DisplayOptions();

        @Setter
        @IgnoreField
        private String sourcePath;
    }

    @Getter
    public static final class DisplayOptions {
        private List<String> lockedLore = new ArrayList<>();
    }

    /**
     * Reads a recipe file. These are read again on every reload, and a server's own ones have no bundled copy, so
     * nothing is used as defaults under them.
     */
    public static StoneCutterRecipesConfig load(File file) {
        var config = ConfigLoader.load(StoneCutterRecipesConfig.class, file.getPath(), YamlConfiguration.loadConfiguration(file));
        config.sourcePath = ConfigManager.sourcePath(file);
        config.recipes.forEach(recipe -> recipe.setSourcePath(config.sourcePath));
        return config;
    }
}
