package gg.auroramc.crafting.api.workbench;

import java.util.List;

public class CraftingTable extends Workbench {
    public CraftingTable() {
        super("vanilla-crafting-table", 0, List.of(1, 2, 3, 4, 5, 6, 7, 8, 9));
        square = true;
    }
}
