package gg.auroramc.crafting.api.workbench.vanilla;

import gg.auroramc.crafting.api.blueprint.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CraftingTable extends VanillaWorkbench<CraftingBlueprint<?>> {
    // The 2x2 player inventory grid mapped into the top left corner of the 3x3 matrix, -1 means no slot
    private static final List<Integer> INVENTORY_GRID_SLOTS = List.of(1, 2, -1, 3, 4, -1, -1, -1, -1);

    public CraftingTable() {
        super("vanilla-crafting-table", 0, List.of(1, 2, 3, 4, 5, 6, 7, 8, 9), VanillaType.CRAFTING_TABLE);
        this.square = true;
        this.craftingSize = 3;
    }

    @Override
    public void addBlueprint(CraftingBlueprint<?> blueprint) {
        if (blueprint instanceof ShapedBlueprint) {
            super.addBlueprint(BlueprintType.SHAPED, blueprint);
        } else if (blueprint instanceof ShapelessBlueprint) {
            super.addBlueprint(BlueprintType.SHAPELESS, blueprint);
        } else {
            throw new IllegalArgumentException("Invalid blueprint type");
        }
    }

    public @Nullable ShapedBlueprint getShapedBlueprint(BlueprintContext context) {
        return (ShapedBlueprint) this.lookupBlueprint(context, BlueprintType.SHAPED);
    }

    public @Nullable ShapelessBlueprint getShapelessBlueprint(BlueprintContext context) {
        return (ShapelessBlueprint) this.lookupBlueprint(context, BlueprintType.SHAPELESS);
    }

    /**
     * Gets the inventory slot for each position of the 3x3 matrix, -1 if the position has no slot.
     */
    public List<Integer> getMatrixSlots(Inventory inventory) {
        return inventory.getType() == InventoryType.CRAFTING ? INVENTORY_GRID_SLOTS : matrixSlots;
    }

    @Override
    public BlueprintContext createContext(Player player, Inventory inventory) {
        var slots = getMatrixSlots(inventory);
        var matrix = new ItemStack[slots.size()];
        for (int i = 0; i < slots.size(); i++) {
            var item = slots.get(i) == -1 ? null : inventory.getItem(slots.get(i));
            matrix[i] = item == null ? ItemStack.empty() : item;
        }
        return new BlueprintContext(player, matrix);
    }
}
