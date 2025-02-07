package gg.auroramc.crafting.api.workbench;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
public class WorkbenchRegistry {
    private boolean frozen = false;
    private final CraftingTable craftingTable = new CraftingTable();
    private final SmithingTable smithingTable = new SmithingTable();
    private final Furnace furnace = new Furnace();

    @Getter(AccessLevel.NONE)
    private final Map<String, Workbench> workbenches = new HashMap<>();

    public void registerWorkbench(Workbench workbench) {
        if (frozen) throw new IllegalStateException("Cannot register workbench after freezing");
        workbenches.put(workbench.getId(), workbench);
    }

    public Workbench getWorkbench(String id) {
        return workbenches.get(id);
    }

    public Collection<Workbench> getCustomWorkbenches() {
        return workbenches.values();
    }

    public void freeze() {
        frozen = true;

        craftingTable.freeze();
        smithingTable.freeze();
        furnace.freeze();

        for (var workbench : workbenches.values()) {
            workbench.freeze();
        }
    }
}
