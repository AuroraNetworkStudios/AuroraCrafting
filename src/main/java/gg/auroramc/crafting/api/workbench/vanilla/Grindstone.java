package gg.auroramc.crafting.api.workbench.vanilla;

import gg.auroramc.crafting.api.blueprint.CauldronBlueprint;

import java.util.List;

public class Grindstone extends VanillaWorkbench<CauldronBlueprint> {
    public Grindstone() {
        super("vanilla-grindstone", 2, List.of(0, 1), VanillaType.GRINDSTONE);
    }

    @Override
    protected boolean shouldRegisterVanillaRecipes() {
        return false;
    }
}