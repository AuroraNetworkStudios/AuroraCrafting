package gg.auroramc.crafting.api.workbench;

import java.util.List;

public class SmithingTable extends Workbench {
    public SmithingTable() {
        super("vanilla-smithing-table", 0, List.of(1, 2, 3));
        square = false;
    }
}
