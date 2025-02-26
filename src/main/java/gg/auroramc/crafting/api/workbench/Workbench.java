package gg.auroramc.crafting.api.workbench;

import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.blueprint.*;
import gg.auroramc.crafting.util.Square;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class Workbench {
    @Getter
    protected String id;
    @Getter
    private boolean frozen = false;

    protected final Map<String, Blueprint> blueprints = new LinkedHashMap<>();
    protected final Map<BlueprintType, Map<String, Blueprint>> categorizedBlueprints = new HashMap<>();
    protected final Map<BlueprintType, Map<String, Blueprint>> matrixLookup = new HashMap<>();
    @Getter
    protected final int resultSlot;
    @Getter
    protected final List<Integer> matrixSlots;
    @Getter
    protected boolean square;
    @Getter
    protected int craftingSize;

    public Workbench(String id, int resultSlot, List<Integer> matrixSlots) {
        this.id = id;
        this.resultSlot = resultSlot;
        this.matrixSlots = matrixSlots;
        this.square = Square.isSquareCraftingArea(matrixSlots, 9);

        if (square) {
            this.craftingSize = (int) Math.sqrt(matrixSlots.size());
        } else {
            this.craftingSize = -1;
        }
    }

    public void addBlueprint(BlueprintType type, Blueprint blueprint) {
        if (frozen) throw new IllegalStateException("Cannot register blueprint after freezing");
        if (blueprints.containsKey(blueprint.getId())) {
            throw new IllegalArgumentException("Blueprint with ID: " + blueprint.getId() + " already exists");
        }
        blueprints.put(blueprint.getId(), blueprint);
        categorizedBlueprints.computeIfAbsent(type, t -> new HashMap<>()).put(blueprint.getId(), blueprint);
        matrixLookup.computeIfAbsent(type, t -> new HashMap<>()).put(BlueprintLookupGenerator.toKey(blueprint), blueprint);

        if (blueprint instanceof ShapedBlueprint shapedBlueprint && square) {
            AuroraCrafting.logger().debug("Generating shifted recipes for shaped blueprint: " + shapedBlueprint.getId());
            var keys = BlueprintLookupGenerator.toShapedVariations(shapedBlueprint);
            for (var key : keys) {
                AuroraCrafting.logger().debug(key);
                matrixLookup.computeIfAbsent(type, t -> new HashMap<>()).put(key, blueprint);
            }
        }
    }

    public Collection<Blueprint> getBlueprints() {
        return blueprints.values();
    }

    public Collection<Blueprint> getBlueprints(BlueprintType... type) {
        if (type.length == 0) return getBlueprints();
        var result = new ArrayList<Blueprint>();
        for (var t : type) {
            var lookup = categorizedBlueprints.get(t);
            if (lookup != null) result.addAll(lookup.values());
        }
        return result;
    }

    public Blueprint getBlueprint(String id) {
        return blueprints.get(id);
    }

    public @Nullable Blueprint lookupBlueprint(BlueprintContext context, BlueprintType... types) {
        for (var type : types) {
            var lookup = matrixLookup.get(type);
            if (lookup == null) continue;

            if (type == BlueprintType.SHAPELESS) {
                var res = lookup.get(context.getShapelessLookupKey());
                if (res != null) return res;
            } else {
                var res = lookup.get(context.getShapedLookupKey());
                if (res != null) return res;
            }
        }
        return null;
    }

    public BlueprintContext createContext(Player player, Inventory inventory) {
        var matrix = new ItemStack[matrixSlots.size()];
        for (int i = 0; i < matrixSlots.size(); i++) {
            var item = inventory.getItem(matrixSlots.get(i));
            matrix[i] = item == null ? ItemStack.empty() : item;
        }
        return new BlueprintContext(player, matrix);
    }

    public BlueprintContext createContext(Player player, ItemStack... items) {
        return new BlueprintContext(player, items);
    }

    public BlueprintContext createContext(Player player, ItemStack item) {
        var items = new ItemStack[1];
        items[0] = item;
        return new BlueprintContext(player, items);
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
