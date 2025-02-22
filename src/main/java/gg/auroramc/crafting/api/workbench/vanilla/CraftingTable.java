package gg.auroramc.crafting.api.workbench.vanilla;

import gg.auroramc.crafting.api.blueprint.*;
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
}
