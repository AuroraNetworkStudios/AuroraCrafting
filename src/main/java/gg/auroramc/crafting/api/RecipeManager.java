package gg.auroramc.crafting.api;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.crafting.AuroraCrafting;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RecipeManager {
    private final AuroraCrafting plugin;
    private final Map<String, AuroraRecipe> shapedRecipeLookup = new HashMap<>();
    private final Map<String, AuroraRecipe> shapelessRecipeLookup = new HashMap<>();
    private final Map<String, AuroraRecipe> recipeIdLookup = new HashMap<>();
    private final Map<String, List<AuroraRecipe>> recipeCategoryLookup = new HashMap<>();
    private final Map<TypeId, AuroraRecipe> recipeResultLookup = new HashMap<>();

    public RecipeManager(AuroraCrafting plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        shapedRecipeLookup.clear();
        shapelessRecipeLookup.clear();
        recipeIdLookup.clear();
        recipeCategoryLookup.clear();

        var recipes = new HashMap<String, List<AuroraRecipe>>();

        for (var recipeFile : plugin.getConfigManager().getCustomRecipes()) {
            for (var recipeConfig : recipeFile.getRecipes()) {
                var recipe = RecipeFactory.createRecipe(
                        recipeConfig.getId(),
                        getItemPair(recipeConfig.getResult(), recipeConfig.getId(), Material.AIR),
                        recipeConfig.getShapeless(),
                        recipeConfig.getWorkbench(),
                        recipeConfig.getPermission(),
                        recipeConfig.getLockedLore()
                );

                for (var ingredient : recipeConfig.getIngredients()) {
                    recipe.addIngredient(getItemPair(ingredient, recipeConfig.getId(), Material.BARRIER));
                }

                registerRecipe(recipe, recipeConfig.getSourcePath());
                recipes.computeIfAbsent(recipeConfig.getSourcePath(), k -> new ArrayList<>()).add(recipe);
            }
        }

        for (var category : plugin.getConfigManager().getRecipeBookConfig().getCategories()) {
            if (category.getId() == null || category.getId().isEmpty()) {
                AuroraCrafting.logger().severe("Category id can't be null or empty!");
                continue;
            }

            var list = recipeCategoryLookup.computeIfAbsent(category.getId(), k -> new ArrayList<>());

            for (var fileName : category.getFiles()) {
                for (var entry : recipes.entrySet()) {
                    if (entry.getKey().endsWith(fileName)) {
                        var recipeList = entry.getValue();
                        if (recipeList != null) {
                            for (var recipe : recipeList) {
                                recipe.setCategory(category);
                                list.add(recipe);
                            }
                        }
                    }

                }
            }

            for (var recipeId : category.getRecipes()) {
                var recipe = recipeIdLookup.get(recipeId);
                if (recipe != null) {
                    recipe.setCategory(category);
                    list.add(recipe);
                }
            }
        }
    }

    private ItemPair getItemPair(String item, String recipeId, Material invalidMaterial) {
        var split = item.split("/");
        if (split[0].isEmpty()) {
            return new ItemPair(TypeId.from(Material.AIR), 0);
        }
        var pair = new ItemPair(TypeId.fromDefault(split[0]), Integer.parseInt(split[1]));

        var itemStack = AuroraAPI.getItemManager().resolveItem(pair.id());
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            AuroraCrafting.logger().severe("Can't validate item id: " + pair.id() + " in recipe: " + recipeId);
            return new ItemPair(TypeId.from(invalidMaterial), 1);
        }

        return pair;
    }

    public @Nullable AuroraRecipe getRecipeByMatrix(List<ItemStack> matrix) {
        var recipe = shapedRecipeLookup.get(asShapedLookupKey(matrix));
        if (recipe == null) {
            recipe = shapelessRecipeLookup.get(asShapelessLookupKey(matrix));
        }
        return recipe;
    }

    public @NotNull List<AuroraRecipe> getRecipesByCategory(String category) {
        return recipeCategoryLookup.getOrDefault(category, new ArrayList<>());
    }

    public @Nullable AuroraRecipe getRecipeById(String id) {
        return recipeIdLookup.get(id);
    }


    private String asShapedLookupKey(List<ItemStack> matrix) {
        var key = new StringBuilder();
        for (var ingredient : matrix) {
            key.append(AuroraAPI.getItemManager().resolveId(ingredient));
            key.append(";");
        }
        return key.toString();
    }

    private String asShapelessLookupKey(List<ItemStack> matrix) {
        var idList = new ArrayList<TypeId>(matrix.size());

        for (var ingredient : matrix) {
            var id = AuroraAPI.getItemManager().resolveId(ingredient);
            if (id.id().equals("air")) continue;
            idList.add(id);
        }

        idList.sort(Comparator.comparing(TypeId::toString));

        var key = new StringBuilder();

        for (var id : idList) {
            key.append(id.toString());
            key.append(";");
        }

        return key.toString();
    }

    public @NotNull List<AuroraRecipe> getCraftableRecipes(Player player, int maxCount, String workbench) {
        var craftableRecipes = new ArrayList<AuroraRecipe>();

        var itemCount = AuroraRecipe.buildItemCounts(player);

        for (var recipe : shapedRecipeLookup.values()) {
            if (recipe.hasPermission(player, workbench) && recipe.getQuickCraftTimes(itemCount) > 0) {
                craftableRecipes.add(recipe);
                if (craftableRecipes.size() >= maxCount) break;
            }
        }

        for (var recipe : shapelessRecipeLookup.values()) {
            if (recipe.hasPermission(player, workbench) && recipe.getQuickCraftTimes(itemCount) > 0) {
                craftableRecipes.add(recipe);
                if (craftableRecipes.size() >= maxCount) break;
            }
        }

        return craftableRecipes;
    }

    public Collection<String> getRecipeIds() {
        return recipeIdLookup.keySet();
    }

    public @Nullable AuroraRecipe getRecipeByResult(TypeId result) {
        return recipeResultLookup.get(result);
    }

    public void registerRecipe(AuroraRecipe recipe, String sourceFile) {
        var key = recipe.asLookupKey();

        if (recipe instanceof ShapelessAuroraRecipe) {
            shapelessRecipeLookup.put(key, recipe);
            AuroraCrafting.logger().debug("Registered shapeless recipe: " + key + " with result: " + recipe.getResult().id() + " for workbench: " + recipe.getWorkbench());
        } else {
            shapedRecipeLookup.put(key, recipe);
            AuroraCrafting.logger().debug("Registered shaped recipe: " + key + " with result: " + recipe.getResult().id() + " for workbench: " + recipe.getWorkbench());
        }

        recipeResultLookup.put(recipe.getResult().id(), recipe);

        if (recipe.getId() != null && !recipe.getId().isEmpty()) {
            if (recipeIdLookup.put(recipe.getId(), recipe) != null) {
                AuroraCrafting.logger().severe("Duplicate recipe id found: " + recipe.getId());
            }
        }

        AuroraCrafting.logger().debug("Loaded recipe: " + recipe.getId());
    }

    public Collection<AuroraRecipe> getShapedRecipes() {
        return shapedRecipeLookup.values();
    }

    public Collection<AuroraRecipe> getShapelessRecipes() {
        return shapelessRecipeLookup.values();
    }
}
