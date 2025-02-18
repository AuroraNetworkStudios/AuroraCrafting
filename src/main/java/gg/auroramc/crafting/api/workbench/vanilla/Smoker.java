package gg.auroramc.crafting.api.workbench.vanilla;

import gg.auroramc.crafting.api.blueprint.BlueprintContext;
import gg.auroramc.crafting.api.blueprint.BlueprintType;
import gg.auroramc.crafting.api.blueprint.CookingBlueprint;
import gg.auroramc.crafting.api.workbench.Workbench;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Smoker extends Workbench {
    public Smoker() {
        super("vanilla-smoker", 0, List.of(1));
    }

    public @Nullable CookingBlueprint getBlueprint(BlueprintContext context) {
        return (CookingBlueprint) this.getBlueprint(BlueprintType.SMOKER, context);
    }

    public void addBlueprint(CookingBlueprint blueprint) {
        this.addBlueprint(BlueprintType.SMOKER, blueprint);
    }
}
