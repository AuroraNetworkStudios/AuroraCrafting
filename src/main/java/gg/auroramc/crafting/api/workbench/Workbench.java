package gg.auroramc.crafting.api.workbench;

import gg.auroramc.crafting.api.blueprint.Blueprint;
import gg.auroramc.crafting.api.blueprint.BlueprintContext;
import gg.auroramc.crafting.api.blueprint.BlueprintLookupGenerator;
import gg.auroramc.crafting.api.blueprint.BlueprintType;
import gg.auroramc.crafting.util.InventoryUtils;
import gg.auroramc.crafting.util.Square;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Workbench {
    @Getter
    protected String id;
    @Getter
    private boolean frozen = false;

    private final Map<String, Blueprint> blueprints = new HashMap<>();
    private final Map<BlueprintType, Map<String, Blueprint>> categorizedBlueprints = new HashMap<>();
    private final Map<BlueprintType, Map<String, Blueprint>> matrixLookup = new HashMap<>();
    @Getter
    protected final int resultSlot;
    @Getter
    protected final List<Integer> matrixSlots;
    @Getter
    protected boolean square;

    public Workbench(String id, int resultSlot, List<Integer> matrixSlots) {
        this.id = id;
        this.resultSlot = resultSlot;
        this.matrixSlots = matrixSlots;
        this.square = Square.isSquareCraftingArea(matrixSlots);
    }

    public void addBlueprint(BlueprintType type, Blueprint blueprint) {
        if (frozen) throw new IllegalStateException("Cannot register blueprint after freezing");
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

    public @NotNull List<Blueprint> getCraftableBlueprints(Player player, int maxCount, BlueprintType... types) {
        var craftableBlueprints = new ArrayList<Blueprint>();

        var itemCount = InventoryUtils.buildItemCounts(player);

        for (var type : types) {
            for (var blueprint : categorizedBlueprints.computeIfAbsent(type, (k) -> new HashMap<>()).values()) {
                if (blueprint.hasAccess(player) && blueprint.getQuickCraftTimes(itemCount) > 0) {
                    craftableBlueprints.add(blueprint);
                    if (craftableBlueprints.size() >= maxCount) break;
                }
            }
        }

        return craftableBlueprints;
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
