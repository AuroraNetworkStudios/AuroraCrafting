package gg.auroramc.crafting.api.workbench.custom;

import gg.auroramc.crafting.api.blueprint.Blueprint;
import gg.auroramc.crafting.api.blueprint.BlueprintType;
import gg.auroramc.crafting.api.workbench.Workbench;
import gg.auroramc.crafting.util.InventoryUtils;
import gg.auroramc.crafting.util.Square;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class CustomWorkbench extends Workbench {
    private final boolean square;
    private final List<Integer> quickCraftSlots;
    private MenuOptions menuOptions;

    public CustomWorkbench(String id, int resultSlot, List<Integer> matrixSlots, List<Integer> quickCraftSlots) {
        this(id, resultSlot, matrixSlots, quickCraftSlots, MenuOptions.builder().build());
    }

    public CustomWorkbench(String id, int resultSlot, List<Integer> matrixSlots, List<Integer> quickCraftSlots, MenuOptions menuOptions) {
        super(id, resultSlot, matrixSlots);
        this.quickCraftSlots = quickCraftSlots == null ? List.of() : quickCraftSlots;
        this.square = Square.isSquareCraftingArea(matrixSlots);
        this.menuOptions = menuOptions.clone().setDefaults();
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

    public void validate() {
        if (matrixSlots.isEmpty()) {
            throw new IllegalStateException("Matrix slots cannot be empty");
        }
        if (matrixSlots.contains(resultSlot)) {
            throw new IllegalStateException("Matrix slots cannot contain the result slot");
        }
        if (quickCraftSlots.contains(resultSlot)) {
            throw new IllegalStateException("Quick craft slots cannot contain the result slot");
        }
        this.menuOptions.validate();
    }

    public void setMenuOptions(MenuOptions menuOptions) {
        this.menuOptions = menuOptions.clone().setDefaults().validate();
    }
}
