package gg.auroramc.crafting.config;

import gg.auroramc.aurora.api.config.premade.ItemConfig;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class RecipeBookConfig {
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
}
