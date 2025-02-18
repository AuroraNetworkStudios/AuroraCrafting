package gg.auroramc.crafting.api.workbench.vanilla;

import gg.auroramc.crafting.api.blueprint.CookingBlueprint;

import java.util.List;

public class BlastFurnace extends VanillaWorkbench<CookingBlueprint> {
    public BlastFurnace() {
        super("vanilla-blast-furnace", 0, List.of(1), VanillaType.BLAST_FURNACE);
    }
}
