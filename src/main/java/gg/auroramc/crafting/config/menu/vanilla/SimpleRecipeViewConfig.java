package gg.auroramc.crafting.config.menu.vanilla;

import lombok.Getter;

/**
 * Furnace, blast furnace, smoker, campfire and stonecutter: one input and one result, and fuel for the first three.
 */
@Getter
public class SimpleRecipeViewConfig extends RecipeViewConfig {
    private Slots slots;

    @Getter
    public static final class Slots extends RecipeViewConfig.Slots {
        private Integer input;
        private Integer result;
        private Integer fuel;
    }
}
