package gg.auroramc.crafting.config;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.crafting.AuroraCrafting;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
public class RecipeBookConfig extends AuroraConfig {
    private List<RecipeCategory> categories = new ArrayList<>();

    @Getter
    public static final class RecipeCategory {
        private String id;
        private List<String> files = new ArrayList<>();
        private List<String> recipes = new ArrayList<>();
        private List<RecipeCategory> categories = new ArrayList<>();
        private Menu menu;
    }

    @Getter
    public static final class Menu {
        private String title;
        private ItemConfig item;
    }

    public RecipeBookConfig(AuroraCrafting plugin) {
        super(getFile(plugin));
    }

    public static File getFile(AuroraCrafting plugin) {
        return new File(plugin.getDataFolder(), "recipe_book.yml");
    }

    public static void saveDefault(AuroraCrafting plugin) {
        if (!getFile(plugin).exists()) {
            plugin.saveResource("recipe_book.yml", false);
        }
    }
}
