package gg.auroramc.crafting.config;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.decorators.IgnoreField;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class WorkbenchConfig extends AuroraConfig {
    @IgnoreField
    private final String id;

    private ItemConfig filler;
    private Map<String, ItemConfig> customItems;
    private String title;
    private Integer rows;
    private Integer resultSlot = 25;
    private List<Integer> matrixSlots;
    private List<Integer> completionIndicatorSlots;
    private List<Integer> quickCraftingSlots;
    private ItemConfig invalidResultItem;
    private ItemConfig emptyQuickCraftItem;
    private ItemConfig noPermissionQuickCraftItem;
    private ItemConfig blueprintCompletedItem;
    private ItemConfig blueprintNotCompletedItem;
    private ItemConfig nextRecipeItem;
    private ItemConfig previousRecipeItem;
    private String commandCompletion;
    private Boolean includeVanillaRecipesInQuickCrafting = false;

    public WorkbenchConfig(File file, String id) {
        super(file);
        this.id = id;
    }

    @Override
    public void load() {
        super.load();
        if(commandCompletion == null) {
            commandCompletion = id;
        }
    }
}
