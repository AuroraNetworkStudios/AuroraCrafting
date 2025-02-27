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
        for (var blueprint : getBlueprints(type.getBlueprintTypes())) {
            var recipe = BlueprintAdapter.adapt(blueprint);

            if (!shouldRegisterVanillaRecipeFor(blueprint)) {
                count++;
                continue;
            }

            var success = Bukkit.addRecipe(recipe);

            if (success) {
                registeredRecipes.add(((Keyed) recipe).getKey());
                count++;
            }
        }
        AuroraCrafting.logger().info("Registered " + count + " recipes for workbench: " + id);
    }

    public void discoverRecipesFor(Player player) {
        player.discoverRecipes(registeredRecipes);
    }

    public void dispose() {
        if (!shouldRegisterVanillaRecipes()) return;
        for (var key : registeredRecipes) {
            Bukkit.removeRecipe(key);
        }
    }

    protected boolean shouldRegisterVanillaRecipes() {
        return true;
    }

    protected boolean shouldRegisterVanillaRecipeFor(Blueprint blueprint) {
        return true;
    }
}
