package gg.auroramc.crafting.config.menu.vanilla;

import lombok.Getter;

@Getter
public class SmithingRecipeViewConfig extends RecipeViewConfig {
    private Slots slots;

    @Getter
    public static final class Slots extends RecipeViewConfig.Slots {
        private Integer template;
        private Integer base;
        private Integer addition;
        private Integer result;
    }
}
