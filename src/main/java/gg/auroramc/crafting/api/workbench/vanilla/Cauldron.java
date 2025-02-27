package gg.auroramc.crafting.api.workbench.vanilla;

import gg.auroramc.crafting.api.blueprint.CauldronBlueprint;

import java.util.List;

public class Cauldron extends VanillaWorkbench<CauldronBlueprint> {
    public Cauldron() {
        super("vanilla-cauldron", 0, List.of(1), VanillaType.CAULDRON);
    }

    @Override
    protected boolean shouldRegisterVanillaRecipes() {
        return false;
    }
}