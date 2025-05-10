package gg.auroramc.crafting.api.workbench.vanilla;

import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.crafting.api.ItemPair;
import gg.auroramc.crafting.api.blueprint.*;
import org.bukkit.Bukkit;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class CookingWorkbench extends VanillaWorkbench<CookingBlueprint> {
    private final List<CookingRecipe<?>> vanillaRecipes = new ArrayList<>();
    private final Map<String, Blueprint> vanillaLookup = new HashMap<>();

    public CookingWorkbench(String id, int resultSlot, List<Integer> matrixSlots, VanillaType type) {
        super(id, resultSlot, matrixSlots, type);

        for (@NotNull Iterator<Recipe> it = Bukkit.recipeIterator(); it.hasNext(); ) {
            var recipe = it.next();
            if (recipe instanceof CookingRecipe<?> cookingRecipe) {
                if (cookingRecipe.getKey().getNamespace().equals("minecraft")) {
                    vanillaRecipes.add(cookingRecipe);
                }
            }
        }
    }

    public @Nullable CookingBlueprint lookupBlueprint(BlueprintContext context) {
        return (CookingBlueprint) super.lookupBlueprint(context, type.getBlueprintType());
    }

    @Override
    public void addBlueprint(BlueprintType type, Blueprint blueprint) {
        super.addBlueprint(type, blueprint);
        if (blueprint instanceof CookingBlueprint cookingBlueprint && cookingBlueprint.getVanillaOptions().getChoiceType() == ChoiceType.ITEM_TYPE) {
            if (hasVanillaRecipe(cookingBlueprint.getInputItem())) {
                vanillaLookup.put(
                        BlueprintLookupGenerator.toShapedKey(blueprint.getIngredientItems().stream().map(i -> new ItemPair(TypeId.from(i.getType()), 1)).toArray(ItemPair[]::new)),
                        blueprint
                );
            }
        }
    }

    @Override
    protected boolean shouldRegisterVanillaRecipeFor(Blueprint blueprint) {
        if (blueprint instanceof CookingBlueprint cookingBlueprint) {
            if (cookingBlueprint.getVanillaOptions().getChoiceType() == ChoiceType.ITEM_TYPE) {
                return !hasVanillaRecipe(cookingBlueprint.getInputItem());
            }
            return true;
        }

        return false;
    }

    private boolean hasVanillaRecipe(ItemStack input) {
        for (var recipe : vanillaRecipes) {
            if (recipe.getInputChoice().test(input)) {
                return true;
            }
        }
        return false;
    }

    public boolean matchesRegisteredVanillaRecipe(BlueprintContext context) {
        return vanillaLookup.containsKey(context.getShapedLookupKey());
    }
}
