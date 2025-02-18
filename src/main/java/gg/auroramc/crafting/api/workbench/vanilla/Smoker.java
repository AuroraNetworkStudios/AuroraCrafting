package gg.auroramc.crafting.api.workbench.vanilla;

import gg.auroramc.crafting.api.blueprint.CookingBlueprint;

import java.util.List;

public class Smoker extends VanillaWorkbench<CookingBlueprint> {
    public Smoker() {
        super("vanilla-smoker", 0, List.of(1), VanillaType.SMOKER);
    }
}
