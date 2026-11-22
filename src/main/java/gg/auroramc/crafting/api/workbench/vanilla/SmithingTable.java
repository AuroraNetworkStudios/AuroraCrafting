package gg.auroramc.crafting.api.workbench.vanilla;

import gg.auroramc.crafting.api.blueprint.Blueprint;
import gg.auroramc.crafting.api.blueprint.SmithingBlueprint;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.bukkit.inventory.SmithingTrimRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SmithingTable extends VanillaWorkbench<SmithingBlueprint> {
    public SmithingTable() {
        super("vanilla-smithing-table", 3, List.of(0, 1, 2), VanillaType.SMITHING_TABLE);
    }

    @Override
    protected boolean overlapsVanillaRecipe(Blueprint blueprint) {
        if (!(blueprint instanceof SmithingBlueprint smithing)) return false;

        var template = single(smithing.getTemplateItem());
        var base = single(smithing.getBaseItem());
        var addition = single(smithing.getAdditionItem());

        for (var iterator = Bukkit.recipeIterator(); iterator.hasNext(); ) {
            var recipe = iterator.next();
            if (recipe instanceof SmithingTransformRecipe transform && transform.getKey().getNamespace().equals(NamespacedKey.MINECRAFT)) {
                if (matches(transform.getTemplate(), template) && matches(transform.getBase(), base) && matches(transform.getAddition(), addition)) {
                    return true;
                }
            } else if (recipe instanceof SmithingTrimRecipe trim && trim.getKey().getNamespace().equals(NamespacedKey.MINECRAFT)) {
                if (matches(trim.getTemplate(), template) && matches(trim.getBase(), base) && matches(trim.getAddition(), addition)) {
                    return true;
                }
            }
        }
        return false;
    }

    // One item, the way vanilla sees a slot (it doesn't know about amounts)
    private static ItemStack single(@Nullable ItemStack item) {
        return item == null || item.isEmpty() ? ItemStack.empty() : item.asOne();
    }

    private static boolean matches(@Nullable RecipeChoice choice, ItemStack item) {
        if (choice == null || choice.equals(RecipeChoice.empty())) return item.isEmpty();
        return !item.isEmpty() && choice.test(item);
    }
}
