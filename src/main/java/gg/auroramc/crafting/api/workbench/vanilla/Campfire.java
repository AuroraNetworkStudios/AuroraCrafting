package gg.auroramc.crafting.api.workbench.vanilla;

import gg.auroramc.crafting.api.blueprint.CookingBlueprint;

import java.util.List;

public class Campfire extends VanillaWorkbench<CookingBlueprint> {
    public Campfire() {
        super("vanilla-campfire", 0, List.of(1), VanillaType.CAMPFIRE);
    }
}
