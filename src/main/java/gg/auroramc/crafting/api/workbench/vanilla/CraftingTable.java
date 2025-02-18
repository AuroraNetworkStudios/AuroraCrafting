package gg.auroramc.crafting.api.workbench.vanilla;

import gg.auroramc.crafting.api.blueprint.BlueprintContext;
import gg.auroramc.crafting.api.blueprint.BlueprintType;
import gg.auroramc.crafting.api.blueprint.ShapedBlueprint;
import gg.auroramc.crafting.api.blueprint.ShapelessBlueprint;
import gg.auroramc.crafting.api.workbench.Workbench;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CraftingTable extends Workbench {
    public CraftingTable() {
        super("vanilla-crafting-table", 0, List.of(1, 2, 3, 4, 5, 6, 7, 8, 9));
    }

    public @Nullable ShapedBlueprint getShapedBlueprint(BlueprintContext context) {
        return (ShapedBlueprint) this.getBlueprint(BlueprintType.SHAPED, context);
    }

    public @Nullable ShapelessBlueprint getShapelessBlueprint(BlueprintContext context) {
        return (ShapelessBlueprint) this.getBlueprint(BlueprintType.SHAPELESS, context);
    }
}
