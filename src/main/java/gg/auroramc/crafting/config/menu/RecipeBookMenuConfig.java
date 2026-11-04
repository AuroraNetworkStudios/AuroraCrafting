package gg.auroramc.crafting.config.menu;

import gg.auroramc.aurora.api.config.premade.ItemConfig;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class RecipeBookMenuConfig {
    private String title = "Recipe Book";
    private Integer rows = 6;
    private List<String> appendLore = new ArrayList<>();
    private ItemConfig filler;
    private Map<String, ItemConfig> customItems;
}
