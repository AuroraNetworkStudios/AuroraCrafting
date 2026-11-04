package gg.auroramc.crafting.config.menu.vanilla;

import gg.auroramc.aurora.api.config.premade.ItemConfig;
import lombok.Getter;

import java.util.List;

@Getter
public class CauldronRecipeViewConfig extends RecipeViewConfig {
    private Slots slots;
    private Fluids fluidMaterials;

    @Getter
    public static final class Fluids {
        private ItemConfig water;
        private ItemConfig lava;
        private ItemConfig powderSnow;
    }

    @Getter
    public static final class Slots extends RecipeViewConfig.Slots {
        private Integer input;
        private Integer result;
        private FluidSlots fluidSlots;
    }

    // Where the fluid is shown, by the recipe's fluid level
    @Getter
    public static final class FluidSlots {
        private List<Integer> one;
        private List<Integer> two;
        private List<Integer> three;
    }
}
