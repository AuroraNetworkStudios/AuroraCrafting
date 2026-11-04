package gg.auroramc.crafting.config.menu.vanilla;

import lombok.Getter;

import java.util.List;

@Getter
public class CraftingTableRecipeViewConfig extends RecipeViewConfig {
    private Slots slots;

    @Getter
    public static final class Slots extends RecipeViewConfig.Slots {
        private Integer result;
        private List<Integer> matrix;
    }
}
