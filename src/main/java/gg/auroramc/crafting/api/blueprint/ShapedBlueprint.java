package gg.auroramc.crafting.api.blueprint;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.crafting.api.ItemPair;
import gg.auroramc.crafting.api.workbench.Workbench;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.recipe.CraftingBookCategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class ShapedBlueprint extends Blueprint {
    private CraftingBookCategory vanillaCategory = CraftingBookCategory.MISC;
    private String vanillaGroup;

    public ShapedBlueprint(Workbench workbench, String id) {
        super(workbench, id);
    }

    public static ShapedBlueprint shapedBlueprint(Workbench workbench, String id) {
        return new ShapedBlueprint(workbench, id);
    }

    public ShapedBlueprint vanillaCategory(CraftingBookCategory category) {
        this.vanillaCategory = category;
        return this;
    }

    public ShapedBlueprint vanillaGroup(String group) {
        this.vanillaGroup = group;
        return this;
    }

    public List<List<ItemPair>> getShapeIngredientVariants() {
        if(!workbench.isSquare()) {
            return List.of(ingredients);
        }
        int gridSize = (int) Math.sqrt(workbench.getMatrixSlots().size());

        int[] bounds = getRecipeBounds(ingredients, gridSize);
        List<ItemPair> trimmed = extractTrimmedRecipe(ingredients, bounds, gridSize);
        int recipeWidth = bounds[2] - bounds[0] + 1;
        int recipeHeight = bounds[3] - bounds[1] + 1;

        var placements = generateAllPlacements(trimmed, gridSize, recipeWidth, recipeHeight);
        placements.addAll(generateMirroredVariants(placements, gridSize, true));
        placements.add(ingredients);
        return placements;
    }

    private int[] getRecipeBounds(List<ItemPair> matrix, int gridSize) {
        int minX = gridSize, minY = gridSize, maxX = 0, maxY = 0;

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (!matrix.get(row * gridSize + col).id().equals(TypeId.from(Material.AIR))) {
                    minX = Math.min(minX, col);
                    maxX = Math.max(maxX, col);
                    minY = Math.min(minY, row);
                    maxY = Math.max(maxY, row);
                }
            }
        }
        return new int[]{minX, minY, maxX, maxY}; // Bounding box
    }

    private List<ItemPair> extractTrimmedRecipe(List<ItemPair> matrix, int[] bounds, int gridSize) {
        int width = bounds[2] - bounds[0] + 1;
        int height = bounds[3] - bounds[1] + 1;

        List<ItemPair> trimmed = new ArrayList<>();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                trimmed.add(matrix.get((bounds[1] + row) * gridSize + (bounds[0] + col)));
            }
        }
        return trimmed;
    }

    private List<List<ItemPair>> generateAllPlacements(List<ItemPair> recipe, int gridSize, int recipeWidth, int recipeHeight) {
        List<List<ItemPair>> placements = new ArrayList<>();

        for (int startRow = 0; startRow <= gridSize - recipeHeight; startRow++) {
            for (int startCol = 0; startCol <= gridSize - recipeWidth; startCol++) {
                placements.add(placeInGrid(recipe, gridSize, recipeWidth, recipeHeight, startRow, startCol));
            }
        }
        return placements;
    }

    private List<ItemPair> placeInGrid(List<ItemPair> recipe, int gridSize, int recipeWidth, int recipeHeight, int startRow, int startCol) {
        List<ItemPair> placed = new ArrayList<>(Collections.nCopies(gridSize * gridSize, new ItemPair(TypeId.from(Material.AIR), 0)));

        for (int row = 0; row < recipeHeight; row++) {
            for (int col = 0; col < recipeWidth; col++) {
                placed.set((startRow + row) * gridSize + (startCol + col), recipe.get(row * recipeWidth + col));
            }
        }
        return placed;
    }

    private List<List<ItemPair>> generateMirroredVariants(List<List<ItemPair>> placements, int gridSize, boolean allowFullMirroring) {
        List<List<ItemPair>> mirrored = new ArrayList<>();

        for (List<ItemPair> placement : placements) {
            mirrored.add(flipHorizontally(placement, gridSize));

            if (allowFullMirroring) {
                mirrored.add(flipVertically(placement, gridSize));
                mirrored.add(flipHorizontally(flipVertically(placement, gridSize), gridSize));
            }
        }
        return mirrored;
    }

    private List<ItemPair> flipHorizontally(List<ItemPair> matrix, int gridSize) {
        List<ItemPair> flipped = new ArrayList<>(matrix);

        for (int row = 0; row < gridSize; row++) {
            int start = row * gridSize;
            int end = start + gridSize - 1;
            while (start < end) {
                Collections.swap(flipped, start, end);
                start++;
                end--;
            }
        }
        return flipped;
    }

    private List<ItemPair> flipVertically(List<ItemPair> matrix, int gridSize) {
        List<ItemPair> flipped = new ArrayList<>(matrix);

        for (int row = 0; row < gridSize / 2; row++) {
            int startRow = row * gridSize;
            int endRow = (gridSize - row - 1) * gridSize;

            for (int col = 0; col < gridSize; col++) {
                Collections.swap(flipped, startRow + col, endRow + col);
            }
        }
        return flipped;
    }



    @Override
    public int getTimesCraftable(BlueprintContext context) {
        int maxCraftable = Integer.MAX_VALUE;

        var matches = true;
        var items = context.getMatrix();

        for (int i = 0; i < items.length; i++) {
            var ingredient = ingredients.size() > i ? ingredients.get(i) : new ItemPair(TypeId.from(Material.AIR), 0);
            var item = items[i];
            var itemTypeId = item.isEmpty() ? TypeId.from(Material.AIR) : AuroraAPI.getItemManager().resolveId(item);
            if (!itemTypeId.equals(ingredient.id())) {
                matches = false;
                break;
            } else if (item.getAmount() < ingredient.amount()) {
                matches = false;
                break;
            } else if (!ingredient.id().id().equals("air")) {
                maxCraftable = Math.min(maxCraftable, Math.max(1, item.getAmount()) / Math.max(1, ingredient.amount()));
            }
        }

        if (!matches) return 0;

        return maxCraftable;
    }

    @Override
    public ItemStack[] calcRemainingIngredientMatrix(BlueprintContext context, int timesCrafted) {
        var items = new ItemStack[context.getMatrix().length];
        var currentMatrix = context.getMatrix();

        for (int i = 0; i < context.getMatrix().length; i++) {
            var ingredient = ingredients.size() > i ? ingredients.get(i) : new ItemPair(TypeId.from(Material.AIR), 0);
            var item = currentMatrix[i];
            if (item.getAmount() <= ingredient.amount() * timesCrafted) {
                items[i] = null;
            } else {
                var newItem = item.clone();
                newItem.setAmount(item.getAmount() - ingredient.amount() * timesCrafted);
                items[i] = newItem;
            }
        }

        return items;
    }
}
