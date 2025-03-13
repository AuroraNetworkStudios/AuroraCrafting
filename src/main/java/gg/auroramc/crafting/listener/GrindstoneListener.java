package gg.auroramc.crafting.listener;

import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.blueprint.Blueprint;
import gg.auroramc.crafting.api.blueprint.BlueprintContext;
import gg.auroramc.crafting.api.blueprint.BlueprintType;
import gg.auroramc.crafting.api.workbench.Workbench;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class GrindstoneListener implements Listener {

    private final AuroraCrafting plugin;

    public GrindstoneListener(AuroraCrafting plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void grind(PrepareGrindstoneEvent event) {
        if (!(event.getViewers().getFirst() instanceof Player player)) return;

        Workbench grindStone = plugin.getWorkbenchRegistry().getGrindstone();
        BlueprintContext context = grindStone.createContext(player, event.getInventory());
        Blueprint blueprint = grindStone.lookupBlueprint(context, BlueprintType.GRINDSTONE);

        // ? return
        if (blueprint == null) return;

        if (!blueprint.hasAccess(player)) {
            event.setResult(null);
            return;
        }

        if (blueprint.getTimesCraftable(context) <= 0) {
            event.setResult(null);
            return;
        }

        event.setResult(blueprint.getResultItem(context));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void click(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null || !event.getClickedInventory().getType().equals(InventoryType.GRINDSTONE))
            return;
        if (event.getAction().equals(InventoryAction.NOTHING)) return;
        if (event.getSlot() == 2) return;


        ItemStack cursor = event.getCursor();
        if (cursor.isEmpty()) return;

        event.setCancelled(true);
        event.getInventory().setItem(event.getSlot(), cursor);
        player.setItemOnCursor(null);
    }


    private static final Set<InventoryAction> ACCEPTED_CRAFT_ACTIONS = Set.of(InventoryAction.COLLECT_TO_CURSOR, InventoryAction.MOVE_TO_OTHER_INVENTORY);

    @EventHandler(priority = EventPriority.HIGHEST)
    private void craft(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null || !event.getClickedInventory().getType().equals(InventoryType.GRINDSTONE))
            return;
        if (event.getAction().equals(InventoryAction.NOTHING)) return;
        if (event.getSlot() != 2) return;
        if (!ACCEPTED_CRAFT_ACTIONS.contains(event.getAction()) && !event.getAction().name().startsWith("PICKUP_"))
            return;

        Workbench grindStone = plugin.getWorkbenchRegistry().getGrindstone();
        BlueprintContext context = grindStone.createContext(player, event.getInventory());
        Blueprint blueprint = grindStone.lookupBlueprint(context, BlueprintType.GRINDSTONE);

        if (blueprint == null) return;

        // we have to decrement the first two slots (0, 1) by the amount needed for the recipe since the vanilla behavior takes all.
        // we basically have to "re-set" the first two slots to the remaining items.

        // Get items from slots 0 and 1
        ItemStack slot0 = event.getInventory().getItem(0);
        ItemStack slot1 = event.getInventory().getItem(1);
        if (slot0 == null || slot0.isEmpty() || slot1 == null || slot1.isEmpty())
            return; // Ensure both slots have items

        // Deduct required amounts
        if (!blueprint.getIngredients().isEmpty()) {
            slot0.setAmount(slot0.getAmount() - blueprint.getIngredients().getFirst().getItemPair().amount());
        }

        if (blueprint.getIngredients().size() > 1) {
            slot1.setAmount(slot1.getAmount() - blueprint.getIngredients().get(1).getItemPair().amount());
        }

        // Schedule the inventory update to bypass vanilla behavior
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            event.getInventory().setItem(0, slot0);
            event.getInventory().setItem(1, slot1);
        }, 1L);
    }

}