package gg.auroramc.crafting.api.blueprint;

import gg.auroramc.crafting.api.workbench.Workbench;
import gg.auroramc.crafting.api.workbench.WorkbenchRegistry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BlueprintRegistry {
    private final Map<String, Blueprint> blueprints;

    private BlueprintRegistry(Map<String, Blueprint> blueprints) {
        this.blueprints = Map.copyOf(blueprints);
    }

    public Blueprint getBlueprint(String id) {
        return blueprints.get(id);
    }

    public Collection<Blueprint> getBlueprints() {
        return blueprints.values();
    }

    public static BlueprintRegistry createFrom(WorkbenchRegistry workbenchRegistry) {
        Map<String, Blueprint> collectedBlueprints = new HashMap<>();

        addWorkbenchBlueprints(collectedBlueprints, workbenchRegistry.getCraftingTable());
        addWorkbenchBlueprints(collectedBlueprints, workbenchRegistry.getSmithingTable());
        addWorkbenchBlueprints(collectedBlueprints, workbenchRegistry.getFurnace());

        for (Workbench workbench : workbenchRegistry.getCustomWorkbenches()) {
            addWorkbenchBlueprints(collectedBlueprints, workbench);
        }

        return new BlueprintRegistry(collectedBlueprints);
    }

    private static void addWorkbenchBlueprints(Map<String, Blueprint> blueprints, Workbench workbench) {
        for (Blueprint blueprint : workbench.getBlueprints()) {
            blueprints.put(blueprint.getId(), blueprint);
        }
    }
}
