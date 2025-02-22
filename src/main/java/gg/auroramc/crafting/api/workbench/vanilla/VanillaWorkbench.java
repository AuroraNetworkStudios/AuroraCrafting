package gg.auroramc.crafting.api.workbench.vanilla;

import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.blueprint.*;
import gg.auroramc.crafting.api.workbench.Workbench;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VanillaWorkbench<T extends Blueprint> extends Workbench {
    @Getter
    protected final VanillaType type;
    private final Set<NamespacedKey> registeredRecipes = new HashSet<>();

    public VanillaWorkbench(String id, int resultSlot, List<Integer> matrixSlots, VanillaType type) {
        super(id, resultSlot, matrixSlots);
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    public @Nullable T getBlueprint(BlueprintContext context) {
        return (T) lookupBlueprint(context, type.getBlueprintType());
    }

    public void addBlueprint(T blueprint) {
        addBlueprint(type.getBlueprintType(), blueprint);
    }


    @Override
    public void freeze() {
        super.freeze();
        int count = 0;
        for (var blueprint : getBlueprints(type.getBlueprintTypes())) {
            var recipe = BlueprintAdapter.adapt(blueprint);
            Recipe vanillaVariant = null;

            if (blueprint instanceof CraftingBlueprint<?> craftingBlueprint) {
                if (craftingBlueprint.getVanillaOptions().getChoiceType() == ChoiceType.ITEM_TYPE) {
                    vanillaVariant = Bukkit.getCraftingRecipe(blueprint.getIngredientItems().toArray(new ItemStack[0]), Bukkit.getWorlds().getFirst());
                }
            }

            var success = Bukkit.addRecipe(recipe);

            if (success) {
                registeredRecipes.add(((Keyed) recipe).getKey());
                count++;
                // As dumb as it seems, we need to remove the vanilla recipe and re-add it to make sure it's the last one.
                // Otherwise, users with overlapping recipes couldn't craft vanilla recipes with vanilla ingredients.
                if (vanillaVariant instanceof Keyed keyed) {
                    Bukkit.removeRecipe(keyed.getKey());
                    Bukkit.addRecipe(vanillaVariant);
                }
            }
        }
        AuroraCrafting.logger().info("Registered " + count + " recipes for workbench: " + id);
    }

    public void discoverRecipesFor(Player player) {
        player.discoverRecipes(registeredRecipes);
    }

    public void dispose() {
        for (var key : registeredRecipes) {
            Bukkit.removeRecipe(key);
        }
    }
}
