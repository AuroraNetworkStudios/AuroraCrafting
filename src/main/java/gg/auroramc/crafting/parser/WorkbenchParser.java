package gg.auroramc.crafting.parser;

import gg.auroramc.crafting.api.workbench.custom.CustomWorkbench;
import gg.auroramc.crafting.api.workbench.custom.MenuOptions;
import gg.auroramc.crafting.api.workbench.custom.RecipeBookOptions;
import gg.auroramc.crafting.config.WorkbenchConfig;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WorkbenchParser {
    private final WorkbenchConfig config;

    public static WorkbenchParser from(WorkbenchConfig config) {
        return new WorkbenchParser(config);
    }

    public CustomWorkbench parse() {
        var workbench = new CustomWorkbench(config.getId(), config.getResultSlot(), config.getMatrixSlots(), config.getQuickCraftingSlots(), config.getCompletionIndicatorSlots());

        workbench.setMenuOptions(MenuOptions.builder()
                .rows(config.getRows())
                .title(config.getTitle())
                .fillerItem(config.getFiller())
                .emptyQuickCraftItem(config.getEmptyQuickCraftItem())
                .noPermissionQuickCraftItem(config.getNoPermissionQuickCraftItem())
                .invalidResultItem(config.getInvalidResultItem())
                .blueprintCompletedItem(config.getBlueprintCompletedItem())
                .blueprintNotCompletedItem(config.getBlueprintNotCompletedItem())
                .nextRecipeItem(config.getNextRecipeItem())
                .previousRecipeItem(config.getPreviousRecipeItem())
                .backItem(config.getBackItem())
                .build());

        workbench.setRecipeBookOptions(RecipeBookOptions.builder()
                .backSlot(config.getRecipeBook().getBackSlot())
                .customItems(config.getRecipeBook().getCustomItems())
                .nextRecipeSlot(config.getRecipeBook().getNextRecipeSlot())
                .prevRecipeSlot(config.getRecipeBook().getPrevRecipeSlot())
                .resultSlot(config.getRecipeBook().getResultSlot())
                .title(config.getRecipeBook().getTitle())
                .build());

        workbench.setIncludeVanillaRecipesInQuickCrafting(config.getIncludeVanillaRecipesInQuickCrafting());

        workbench.validate();

        return workbench;
    }
}
