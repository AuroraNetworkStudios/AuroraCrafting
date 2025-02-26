package gg.auroramc.crafting.api.workbench.vanilla;

import gg.auroramc.crafting.api.blueprint.StoneCutterBlueprint;

import java.util.List;

public class StoneCutter extends VanillaWorkbench<StoneCutterBlueprint> {
    public StoneCutter() {
        super("vanilla-stone-cutter", 1, List.of(0), VanillaType.STONE_CUTTER);
    }
}
