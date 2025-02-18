package gg.auroramc.crafting.api.workbench.vanilla;

import gg.auroramc.crafting.api.blueprint.BlueprintContext;
import gg.auroramc.crafting.api.blueprint.BlueprintType;
import gg.auroramc.crafting.api.blueprint.SmithingBlueprint;
import gg.auroramc.crafting.api.workbench.Workbench;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SmithingTable extends Workbench {
    public SmithingTable() {
        super("vanilla-smithing-table", 0, List.of(1, 2, 3));
    }

    public @Nullable SmithingBlueprint getBlueprint(BlueprintContext context) {
        return (SmithingBlueprint) this.getBlueprint(BlueprintType.SMITHING, context);
    }

    public void addBlueprint(SmithingBlueprint blueprint) {
        this.addBlueprint(BlueprintType.SMITHING, blueprint);
    }
}
