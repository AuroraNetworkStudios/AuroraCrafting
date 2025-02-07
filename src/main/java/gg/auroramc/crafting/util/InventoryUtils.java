/**
 * THIS FILE IS COPIED FROM BETONQUESTS
 * We don't want to reinvent the wheel.
 */

package gg.auroramc.crafting.util;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;


/**
 * This is a utility class for working with inventories and crafting.
 */
public final class InventoryUtils {

    private InventoryUtils() {
    }

    /**
     * Check whether an item "is nothing". Sometimes they are {@link ItemStack}s with material {@link Material#AIR} but
     * they also might be null. This method provides an easy check.
     *
     * @param slotItem item to check
     * @return true if the slot is empty
     */
    public static boolean isEmptySlot(@Nullable final ItemStack slotItem) {
        return slotItem == null || slotItem.getType().equals(Material.AIR);
    }

    /**
     * Calculate the maximum amount an item can fit into an inventory. The amount of the item stack itself will be
     * ignored, to see how often the given stack fits use {@code calculateSpaceForItem(inventory, item) / item.getAmount()}.
     *
     * @param inventory the inventory to check
     * @param item      the item to fit.
     * @return the maximum amount the item fits into the inventory
     */
    public static int calculateSpaceForItem(final Inventory inventory, final ItemStack item) {
        int remainingSpace = 0;
        for (final ItemStack i : inventory.getStorageContents()) {
            if (isEmptySlot(i)) {
                remainingSpace += item.getMaxStackSize();
            } else if (i.isSimilar(item)) {
                remainingSpace += item.getMaxStackSize() - i.getAmount();
            }
        }
        return remainingSpace;
    }


    public static Map<TypeId, Integer> buildItemCounts(Player player) {
        Map<TypeId, Integer> itemCount = new HashMap<>(player.getInventory().getSize());

        for (var item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR || item.getAmount() == 0) continue;
            var id = AuroraAPI.getItemManager().resolveId(item);
            itemCount.merge(id, item.getAmount(), Integer::sum);
        }

        return itemCount;
    }
}
