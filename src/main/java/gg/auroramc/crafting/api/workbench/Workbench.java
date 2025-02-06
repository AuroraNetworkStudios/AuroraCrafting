package gg.auroramc.crafting.api.workbench;

import gg.auroramc.crafting.api.blueprint.*;
import gg.auroramc.crafting.util.Square;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Workbench {
    @Getter
    private String id;

    private final Map<String, Blueprint> blueprints = new HashMap<>();
    private final Map<BlueprintType, Map<String, Blueprint>> matrixLookup = new HashMap<>();
    @Getter
    private final int resultSlot;
    @Getter
    private final List<Integer> matrixSlots;
    @Getter
    private final boolean square;

    public Workbench(String id, int resultSlot, List<Integer> matrixSlots) {
        this.id = id;
        this.resultSlot = resultSlot;
        this.matrixSlots = matrixSlots;
        this.square = Square.isSquareCraftingArea(matrixSlots);
    }

    public void addBlueprint(BlueprintType type, Blueprint blueprint) {
        blueprints.put(blueprint.getId(), blueprint);
        if (blueprint instanceof ShapedBlueprint shapedBlueprint) {
            for (var ingredientList : shapedBlueprint.getShapeIngredientVariants()) {
                var key = BlueprintLookupGenerator.toShapedKey(ingredientList);
                matrixLookup.computeIfAbsent(type, t -> new HashMap<>()).put(key, blueprint);
            }
        } else {
            matrixLookup.computeIfAbsent(type, t -> new HashMap<>()).put(BlueprintLookupGenerator.toKey(blueprint), blueprint);
        }
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
