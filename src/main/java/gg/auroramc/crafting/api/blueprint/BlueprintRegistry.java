package gg.auroramc.crafting.api.blueprint;

import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.crafting.api.workbench.Workbench;
import gg.auroramc.crafting.api.workbench.WorkbenchRegistry;

import java.util.*;

public class BlueprintRegistry {
    private final Map<String, Blueprint> blueprints;
    private final Map<TypeId, List<Blueprint>> resultLookup;

    private BlueprintRegistry(Map<String, Blueprint> blueprints, Map<TypeId, List<Blueprint>> resultLookup) {
        this.blueprints = new LinkedHashMap<>(blueprints);
        this.resultLookup = Map.copyOf(resultLookup);
    }

    public Blueprint getBlueprint(String id) {
        return blueprints.get(id);
    }

    public List<Blueprint> getBlueprintsFor(TypeId item) {
        return resultLookup.getOrDefault(item, Collections.emptyList());
    }

    public Blueprint getBlueprintFor(TypeId item) {
        var list = resultLookup.getOrDefault(item, Collections.emptyList());
        if (list.isEmpty()) {
            return null;
        }
        return list.getFirst();
    }

    public Collection<Blueprint> getBlueprints() {
        return blueprints.values();
    }

    public static BlueprintRegistry createFrom(WorkbenchRegistry workbenchRegistry) {
        Map<String, Blueprint> collectedBlueprints = new LinkedHashMap<>();
        Map<TypeId, List<Blueprint>> resultLookupBlueprints = new HashMap<>();

        for (Workbench workbench : workbenchRegistry.getVanillaWorkbenches()) {
            addWorkbenchBlueprints(collectedBlueprints, resultLookupBlueprints, workbench);
        }

        for (Workbench workbench : workbenchRegistry.getCustomWorkbenches()) {
            addWorkbenchBlueprints(collectedBlueprints, resultLookupBlueprints, workbench);
        }

        return new BlueprintRegistry(collectedBlueprints, resultLookupBlueprints);
    }

    private static void addWorkbenchBlueprints(Map<String, Blueprint> blueprints, Map<TypeId, List<Blueprint>> resultLookupBlueprints, Workbench workbench) {
        for (var blueprint : workbench.getBlueprints()) {
            blueprints.put(blueprint.getId(), blueprint);
            resultLookupBlueprints.computeIfAbsent(blueprint.getResult().id(), k -> new ArrayList<>()).add(blueprint);
        }
    }
}
