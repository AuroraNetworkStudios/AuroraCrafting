package gg.auroramc.crafting.api.workbench.vanilla;

import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.blueprint.Blueprint;
import gg.auroramc.crafting.api.blueprint.BlueprintAdapter;
import gg.auroramc.crafting.api.blueprint.BlueprintContext;
import gg.auroramc.crafting.api.workbench.Workbench;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class VanillaWorkbench<T extends Blueprint> extends Workbench {
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
        if (!shouldRegisterVanillaRecipes()) return;

        int count = 0;
        int overlapping = 0;
        for (var blueprint : getBlueprints(type.getBlueprintTypes())) {
            if (canBeUncraftable(blueprint) && overlapsVanillaRecipe(blueprint)) {
                overlapping++;
                continue;
            }

            var key = registerVanillaRecipe(blueprint);

            if (key != null) {
                registeredRecipes.add(key);
                count++;
            }
        }
        AuroraCrafting.logger().info("Registered " + count + " recipes for workbench: " + id
                + (overlapping > 0 ? " (" + overlapping + " more are crafted through the vanilla recipe with the same items)" : ""));
    }

    /**
     * Vanilla matches our recipes on item ids only, it doesn't know about ingredient amounts or permissions. So a
     * blueprint with either can be impossible to craft for a grid vanilla matched it for. If a vanilla recipe uses the
     * same items, vanilla could pick ours there and hide its own result (9 iron ingots next to a recipe needing 16
     * per slot), so such a blueprint isn't registered: vanilla keeps matching its own recipe, and the listener swaps
     * in our result when the blueprint can be crafted. Every other blueprint is registered.
     */
    private static boolean canBeUncraftable(Blueprint blueprint) {
        var permission = blueprint.getPermission();
        return blueprint.isStacked() || (permission != null && !permission.isBlank());
    }

    /**
     * Whether a vanilla recipe accepts the blueprint's items, one item per slot like vanilla sees them.
     */
    protected boolean overlapsVanillaRecipe(Blueprint blueprint) {
        return false;
    }

    public void discoverRecipesFor(Player player) {
        player.discoverRecipes(registeredRecipes);
    }

    public void dispose() {
        if (!shouldRegisterVanillaRecipes()) return;
        for (var key : registeredRecipes) {
            removeVanillaRecipe(key);
        }
    }

    protected boolean shouldRegisterVanillaRecipes() {
        return true;
    }

    protected @Nullable NamespacedKey registerVanillaRecipe(Blueprint blueprint) {
        var recipe = BlueprintAdapter.adapt(blueprint);
        var success = Bukkit.addRecipe(recipe);
        if (success) {
            return ((Keyed) recipe).getKey();
        } else {
            return null;
        }
    }

    protected void removeVanillaRecipe(NamespacedKey key) {
        Bukkit.removeRecipe(key);
    }
}
