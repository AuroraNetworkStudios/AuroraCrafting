package gg.auroramc.crafting.config;

import gg.auroramc.aurora.api.config.ConfigLoader;
import gg.auroramc.aurora.api.config.decorators.IgnoreField;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class CraftingRecipesConfig {
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
        private Boolean quickCraft = true;
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
    }

    @Getter
    public static final class DisplayOptions {
        private List<String> lockedLore = new ArrayList<>();
    }

    /**
     * Reads a recipe file. These are read again on every reload, and a server's own ones have no bundled copy, so
     * nothing is used as defaults under them.
     */
    public static CraftingRecipesConfig load(File file, boolean vanilla) {
        var config = ConfigLoader.load(CraftingRecipesConfig.class, file.getPath(), YamlConfiguration.loadConfiguration(file));
        config.vanilla = vanilla;
        config.sourcePath = ConfigManager.sourcePath(file);
        config.recipes.forEach(recipe -> recipe.setSourcePath(config.sourcePath));
        return config;
    }
}
