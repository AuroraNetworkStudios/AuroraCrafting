package gg.auroramc.crafting.config.menu;

import gg.auroramc.aurora.api.config.premade.ItemConfig;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class RecipeBookCategoryConfig {
    private CategoryIcon categoryIcon;
    private Integer rows = 6;
    private List<String> appendLore = new ArrayList<>();
    private List<Integer> displayArea = List.of(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    );
    private SecretDisplay secretRecipeDisplay;
    private ItemConfig filler;
    private Map<String, ItemConfig> items;
    private Map<String, ItemConfig> customItems;

    @Getter
    public static final class CategoryIcon {
        private Boolean enabled = true;
        private ItemConfig item;
    }

    @Getter
    public static final class SecretDisplay {
        private Boolean enabled = true;
        private ItemConfig item;
    }
}
