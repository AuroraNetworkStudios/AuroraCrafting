package gg.auroramc.crafting.api.workbench.custom;

import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.blueprint.Blueprint;
import gg.auroramc.crafting.api.blueprint.BlueprintGroup;
import gg.auroramc.crafting.api.blueprint.BlueprintType;
import gg.auroramc.crafting.api.blueprint.RecipeWrapperBlueprint;
import gg.auroramc.crafting.api.workbench.Workbench;
import gg.auroramc.crafting.util.InventoryUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public class CustomWorkbench extends Workbench {
    private final List<Integer> quickCraftSlots;
    private final List<Integer> completionIndicatorSlots;
    @Setter
    private RecipeBookOptions recipeBookOptions;
    private MenuOptions menuOptions;
    @Setter
    private boolean includeVanillaRecipesInQuickCrafting = false;
    private List<RecipeWrapperBlueprint> vanillaRecipes = new ArrayList<>();

    public CustomWorkbench(String id, int resultSlot, List<Integer> matrixSlots, List<Integer> quickCraftSlots, List<Integer> completionIndicatorSlots) {
        this(id, resultSlot, matrixSlots, quickCraftSlots, completionIndicatorSlots, MenuOptions.builder().build());
    }

    public CustomWorkbench(String id, int resultSlot, List<Integer> matrixSlots, List<Integer> quickCraftSlots, List<Integer> completionIndicatorSlots, MenuOptions menuOptions) {
        this(id, resultSlot, matrixSlots, quickCraftSlots, completionIndicatorSlots, menuOptions, RecipeBookOptions.builder().build());
    }

    public CustomWorkbench(String id, int resultSlot, List<Integer> matrixSlots, List<Integer> quickCraftSlots, List<Integer> completionIndicatorSlots, MenuOptions menuOptions, RecipeBookOptions recipeBookOptions) {
        super(id, resultSlot, matrixSlots);
        this.quickCraftSlots = quickCraftSlots == null ? List.of() : quickCraftSlots;
        this.completionIndicatorSlots = completionIndicatorSlots == null ? List.of() : completionIndicatorSlots;
        this.menuOptions = menuOptions.clone().setDefaults();
        this.recipeBookOptions = recipeBookOptions;
    }

    public @NotNull List<Blueprint> getCraftableBlueprints(Player player, int maxCount, BlueprintType... types) {
        var groups = new HashSet<BlueprintGroup>();
        var craftableBlueprints = new ArrayList<Blueprint>();

        var itemCount = InventoryUtils.buildItemCounts(player);

        for (var type : types) {
            for (var blueprint : categorizedBlueprints.computeIfAbsent(type, (k) -> new HashMap<>()).values()) {
                if (blueprint.hasAccess(player) && blueprint.getQuickCraftTimes(itemCount) > 0) {
                    if (blueprint.getGroup() != null) {
                        if (groups.add(blueprint.getGroup())) {
                            craftableBlueprints.add(blueprint);
                            if (craftableBlueprints.size() >= maxCount) break;
                        }
                    } else {
                        craftableBlueprints.add(blueprint);
                        if (craftableBlueprints.size() >= maxCount) break;
                    }
                }
            }
        }

        if (craftableBlueprints.size() >= maxCount) return craftableBlueprints;

        for (var vanillaRecipe : vanillaRecipes) {
            if (vanillaRecipe.getQuickCraftTimes(itemCount) > 0) {
                craftableBlueprints.add(vanillaRecipe);
                if (craftableBlueprints.size() >= maxCount) {
                    return craftableBlueprints;
                }
            }
        }

        return craftableBlueprints;
    }

    public void validate() {
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("Workbench id cannot be null or empty");
        }
        if (matrixSlots == null || matrixSlots.isEmpty()) {
            throw new IllegalStateException("Matrix slots cannot be empty");
        }
        if (matrixSlots.contains(resultSlot)) {
            throw new IllegalStateException("Matrix slots cannot contain the result slot");
        }
        if (quickCraftSlots.contains(resultSlot)) {
            throw new IllegalStateException("Quick craft slots cannot contain the result slot");
        }
        this.menuOptions.validate();
    }

    public void setMenuOptions(MenuOptions menuOptions) {
        this.menuOptions = menuOptions.clone().setDefaults().validate();
    }

    @Override
    public void freeze() {
        super.freeze();
        AuroraCrafting.logger().info("Registered " + blueprints.size() + " recipes for workbench: " + id);

        if (includeVanillaRecipesInQuickCrafting) {
            for (@NotNull Iterator<Recipe> it = Bukkit.recipeIterator(); it.hasNext(); ) {
                var recipe = it.next();
                if (recipe instanceof ShapedRecipe shapedRecipe) {
                    if (shapedRecipe.getKey().getNamespace().equals("minecraft") && !recipe.getResult().isEmpty()) {
                        vanillaRecipes.add(new RecipeWrapperBlueprint(this, shapedRecipe));
                    }
                } else if (recipe instanceof ShapelessRecipe shapelessRecipe && !recipe.getResult().isEmpty()) {
                    if (shapelessRecipe.getKey().getNamespace().equals("minecraft")) {
                        vanillaRecipes.add(new RecipeWrapperBlueprint(this, shapelessRecipe));
                    }
                }
            }
            AuroraCrafting.logger().info("Registered " + vanillaRecipes.size() + " vanilla recipes for quick crafting in workbench: " + id);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private int resultSlot;
        private List<Integer> matrixSlots;
        private List<Integer> completionIndicatorSlots;
        private List<Integer> quickCraftSlots;
        private MenuOptions menuOptions;
        private RecipeBookOptions recipeBookOptions;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder resultSlot(int resultSlot) {
            this.resultSlot = resultSlot;
            return this;
        }

        public Builder matrixSlots(List<Integer> matrixSlots) {
            this.matrixSlots = matrixSlots;
            return this;
        }

        public Builder quickCraftSlots(List<Integer> quickCraftSlots) {
            this.quickCraftSlots = quickCraftSlots;
            return this;
        }

        public Builder completionIndicatorSlots(List<Integer> completionIndicatorSlots) {
            this.completionIndicatorSlots = completionIndicatorSlots;
            return this;
        }

        public Builder menuOptions(MenuOptions menuOptions) {
            this.menuOptions = menuOptions;
            return this;
        }

        public CustomWorkbench build() {
            return new CustomWorkbench(id, resultSlot, matrixSlots, quickCraftSlots, completionIndicatorSlots, menuOptions);
        }
    }
}
