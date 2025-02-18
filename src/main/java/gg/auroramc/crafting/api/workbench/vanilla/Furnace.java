package gg.auroramc.crafting.api.workbench.vanilla;

import gg.auroramc.crafting.api.blueprint.CookingBlueprint;

import java.util.List;

public class Furnace extends VanillaWorkbench<CookingBlueprint> {
    public Furnace() {
        super("vanilla-furnace", 0, List.of(1), VanillaType.FURNACE);
    }
}
