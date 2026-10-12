package gg.auroramc.crafting.api.workbench.vanilla;

import gg.auroramc.crafting.api.blueprint.SmithingBlueprint;

import java.util.List;

public class SmithingTable extends VanillaWorkbench<SmithingBlueprint> {
    public SmithingTable() {
        super("vanilla-smithing-table", 3, List.of(0, 1, 2), VanillaType.SMITHING_TABLE);
    }
}
