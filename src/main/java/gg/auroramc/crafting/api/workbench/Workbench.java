package gg.auroramc.crafting.api.workbench;

import gg.auroramc.crafting.api.blueprint.Blueprint;
import gg.auroramc.crafting.api.blueprint.BlueprintContext;
import gg.auroramc.crafting.api.blueprint.BlueprintLookupGenerator;
import gg.auroramc.crafting.api.blueprint.BlueprintType;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class Workbench {
    @Getter
    protected String id;
    @Getter
    private boolean frozen = false;

    protected final Map<String, Blueprint> blueprints = new HashMap<>();
    protected final Map<BlueprintType, Map<String, Blueprint>> categorizedBlueprints = new HashMap<>();
    protected final Map<BlueprintType, Map<String, Blueprint>> matrixLookup = new HashMap<>();
    @Getter
    protected final int resultSlot;
    @Getter
    protected final List<Integer> matrixSlots;


    public Workbench(String id, int resultSlot, List<Integer> matrixSlots) {
        this.id = id;
        this.resultSlot = resultSlot;
        this.matrixSlots = matrixSlots;
    }

    public void addBlueprint(BlueprintType type, Blueprint blueprint) {
        if (frozen) throw new IllegalStateException("Cannot register blueprint after freezing");
        if (blueprints.containsKey(blueprint.getId())) {
            throw new IllegalArgumentException("Blueprint with ID: " + blueprint.getId() + " already exists");
        }
        blueprints.put(blueprint.getId(), blueprint);
        categorizedBlueprints.computeIfAbsent(type, t -> new HashMap<>()).put(blueprint.getId(), blueprint);
        matrixLookup.computeIfAbsent(type, t -> new HashMap<>()).put(BlueprintLookupGenerator.toKey(blueprint), blueprint);
    }

    public Collection<Blueprint> getBlueprints() {
        return blueprints.values();
    }

    public Blueprint getBlueprint(String id) {
        return blueprints.get(id);
    }

    public @Nullable Blueprint getBlueprint(BlueprintType type, BlueprintContext context) {
        var lookup = matrixLookup.get(type);
        if (lookup == null) return null;

        if (type == BlueprintType.SHAPELESS) {
            var shapelessKey = BlueprintLookupGenerator.toShapelessKey(context.getIdMatrix());
            return lookup.get(shapelessKey);
        } else {
            var shapedKey = BlueprintLookupGenerator.toShapedKey(context.getIdMatrix());
            return lookup.get(shapedKey);
        }
    }

    public void freeze() {
        frozen = true;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Workbench workbench = (Workbench) object;
        return Objects.equals(id, workbench.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
