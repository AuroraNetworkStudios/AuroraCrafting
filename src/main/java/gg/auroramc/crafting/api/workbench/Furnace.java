package gg.auroramc.crafting.api.workbench;

import java.util.List;

public class Furnace extends Workbench {
    public Furnace() {
        super("vanilla-furnace", 0, List.of(1));
        square = false;
    }
}
