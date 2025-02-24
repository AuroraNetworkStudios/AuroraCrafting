package gg.auroramc.crafting.api.workbench.vanilla;

import gg.auroramc.crafting.api.blueprint.*;
import org.bukkit.Bukkit;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CraftingTable extends VanillaWorkbench<CraftingBlueprint<?>> {
    public CraftingTable() {
        super("vanilla-crafting-table", 0, List.of(1, 2, 3, 4, 5, 6, 7, 8, 9), VanillaType.CRAFTING_TABLE);
        this.square = true;
        this.craftingSize = 3;
    }

    @Override
    public void addBlueprint(CraftingBlueprint<?> blueprint) {
        if (blueprint instanceof ShapedBlueprint) {
            super.addBlueprint(BlueprintType.SHAPED, blueprint);
        } else if (blueprint instanceof ShapelessBlueprint) {
            super.addBlueprint(BlueprintType.SHAPELESS, blueprint);
        } else {
            throw new IllegalArgumentException("Invalid blueprint type");
        }
    }

    public @Nullable ShapedBlueprint getShapedBlueprint(BlueprintContext context) {
        return (ShapedBlueprint) this.lookupBlueprint(context, BlueprintType.SHAPED);
    }

    public @Nullable ShapelessBlueprint getShapelessBlueprint(BlueprintContext context) {
        return (ShapelessBlueprint) this.lookupBlueprint(context, BlueprintType.SHAPELESS);
    }

    @Override
    protected boolean shouldRegisterVanillaRecipeFor(Blueprint blueprint) {
        if (blueprint instanceof CraftingBlueprint<?> craftingBlueprint) {
            if (craftingBlueprint.getVanillaOptions().getChoiceType() == ChoiceType.ITEM_TYPE) {
                var vanillaVariant = Bukkit.getCraftingRecipe(blueprint.getIngredientItems().toArray(new ItemStack[0]), Bukkit.getWorlds().getFirst());
                if (vanillaVariant instanceof CraftingRecipe craftingRecipe) {
                    return !craftingRecipe.getKey().getNamespace().equals("minecraft");
                }
            }
            return true;
        }

        return false;
    }
}
