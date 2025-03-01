package gg.auroramc.crafting.api.workbench.custom;

import gg.auroramc.aurora.api.config.premade.ItemConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public final class MenuOptions implements Cloneable {
    @Setter
    private static DefaultSupplier defaultSupplier;

    public interface DefaultSupplier {
        ItemConfig getFiller();

        ItemConfig getEmptyQuickCraft();

        ItemConfig getNoPermissionQuickCraft();

        ItemConfig getBlueprintCompleted();

        ItemConfig getBlueprintNotCompleted();

        ItemConfig getInvalidResult();

        ItemConfig getNextRecipe();

        ItemConfig getPreviousRecipe();

        ItemConfig getBack();

        Integer getRows();

        String getTitle();
    }

    private List<ItemConfig> customItems;
    private ItemConfig fillerItem;
    private ItemConfig backItem;
    private ItemConfig emptyQuickCraftItem;
    private ItemConfig noPermissionQuickCraftItem;
    private ItemConfig blueprintCompletedItem;
    private ItemConfig blueprintNotCompletedItem;
    private ItemConfig invalidResultItem;
    private ItemConfig nextRecipeItem;
    private ItemConfig previousRecipeItem;
    private Integer rows;
    private String title;

    public MenuOptions setDefaults() {
        if (this.fillerItem == null) {
            this.fillerItem = defaultSupplier.getFiller();
        }
        if (this.emptyQuickCraftItem == null) {
            this.emptyQuickCraftItem = defaultSupplier.getEmptyQuickCraft();
        }
        if (this.noPermissionQuickCraftItem == null) {
            this.noPermissionQuickCraftItem = defaultSupplier.getNoPermissionQuickCraft();
        }
        if (this.blueprintCompletedItem == null) {
            this.blueprintCompletedItem = defaultSupplier.getBlueprintCompleted();
        }
        if (this.blueprintNotCompletedItem == null) {
            this.blueprintNotCompletedItem = defaultSupplier.getBlueprintNotCompleted();
        }
        if (this.invalidResultItem == null) {
            this.invalidResultItem = defaultSupplier.getInvalidResult();
        }
        if (this.nextRecipeItem == null) {
            this.nextRecipeItem = defaultSupplier.getNextRecipe();
        }
        if (this.previousRecipeItem == null) {
            this.previousRecipeItem = defaultSupplier.getPreviousRecipe();
        }
        if (this.backItem == null) {
            this.backItem = defaultSupplier.getBack();
        }
        if (this.rows == null) {
            this.rows = defaultSupplier.getRows();
        }
        if (this.title == null) {
            this.title = defaultSupplier.getTitle();
        }
        if (customItems == null) {
            customItems = new ArrayList<>();
        }
        return this;
    }


    public MenuOptions validate() {
        if (fillerItem == null) {
            throw new IllegalArgumentException("Filler item cannot be null");
        }
        if (emptyQuickCraftItem == null) {
            throw new IllegalArgumentException("Empty quick craft item cannot be null");
        }
        if (noPermissionQuickCraftItem == null) {
            throw new IllegalArgumentException("No permission quick craft item cannot be null");
        }
        if (blueprintCompletedItem == null) {
            throw new IllegalArgumentException("Blueprint completed item cannot be null");
        }
        if (blueprintNotCompletedItem == null) {
            throw new IllegalArgumentException("Blueprint not completed item cannot be null");
        }
        if (invalidResultItem == null) {
            throw new IllegalArgumentException("Invalid result item cannot be null");
        }
        if (nextRecipeItem == null) {
            throw new IllegalArgumentException("Next recipe item cannot be null");
        }
        if (previousRecipeItem == null) {
            throw new IllegalArgumentException("Previous recipe item cannot be null");
        }
        if (rows == null || rows < 1 || rows > 6) {
            throw new IllegalArgumentException("Rows must be between 1 and 6");
        }
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        return this;
    }

    @SneakyThrows
    @Override
    public MenuOptions clone() {
        return (MenuOptions) super.clone();
    }
}
