package gg.auroramc.crafting.config.menu;

import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.workbench.custom.MenuOptions;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

@Getter
public class WorkbenchDefaultConfig implements MenuOptions.DefaultSupplier {
    private ItemConfig filler;
    private String title = "Workbench";
    private Integer rows = 6;
    private ItemConfig invalidResultItem;
    private ItemConfig emptyQuickCraftItem;
    private ItemConfig noPermissionQuickCraftItem;
    private ItemConfig blueprintCompletedItem;
    private ItemConfig blueprintNotCompletedItem;
    private ItemConfig nextRecipeItem;
    private ItemConfig previousRecipeItem;
    private ItemConfig backItem;

    @Override
    public ItemConfig getEmptyQuickCraft() {
        return emptyQuickCraftItem;
    }

    @Override
    public ItemConfig getNoPermissionQuickCraft() {
        return noPermissionQuickCraftItem;
    }

    @Override
    public ItemConfig getBlueprintCompleted() {
        return blueprintCompletedItem;
    }

    @Override
    public ItemConfig getBlueprintNotCompleted() {
        return blueprintNotCompletedItem;
    }

    @Override
    public ItemConfig getInvalidResult() {
        return invalidResultItem;
    }

    @Override
    public ItemConfig getNextRecipe() {
        return nextRecipeItem;
    }

    @Override
    public ItemConfig getPreviousRecipe() {
        return previousRecipeItem;
    }

    @Override
    public ItemConfig getBack() {
        return backItem;
    }

    // Steps by config-version, only ever appended, each sets config-version itself
    public static List<Consumer<YamlConfiguration>> migrations() {
        return List.of(
                yaml -> {
                    var oldFile = new File(AuroraCrafting.getInstance().getDataFolder() + "/menus", "recipe_view.yml");
                    if (oldFile.exists()) {
                        var oldYaml = YamlConfiguration.loadConfiguration(oldFile);
                        yaml.set("back-item", oldYaml.get("items.back"));
                    } else {
                        yaml.set("back-item.material", "arrow");
                        yaml.set("back-item.name", "&fBack");
                        yaml.set("back-item.lore", List.of("&7Click to go back"));
                    }
                    yaml.set("config-version", 1);
                }
        );
    }
}
