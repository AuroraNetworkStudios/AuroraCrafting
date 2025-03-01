package gg.auroramc.crafting.api.workbench.custom;

import gg.auroramc.aurora.api.config.premade.ItemConfig;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
public class RecipeBookOptions {
    @Builder.Default
    private Integer prevRecipeSlot = null;
    @Builder.Default
    private Integer nextRecipeSlot = null;
    @Builder.Default
    private Integer resultSlot = null;
    @Builder.Default
    private Integer backSlot = null;
    @Builder.Default
    private String title = "Crafting recipe";
    @Builder.Default
    private Map<String, ItemConfig> customItems = new HashMap<>();
}
