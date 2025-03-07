package gg.auroramc.crafting.listener;

import gg.auroramc.aurora.api.util.ItemUtils;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.blueprint.BlueprintType;
import gg.auroramc.crafting.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CraftingListener implements Listener {
    private final AuroraCrafting plugin;

    public CraftingListener(AuroraCrafting plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareCrafting(PrepareItemCraftEvent event) {
        if (!(event.getViewers().getFirst() instanceof Player player)) return;

        var workbench = plugin.getWorkbenchRegistry().getCraftingTable();
        var context = workbench.createContext(player, event.getInventory());
        var blueprint = workbench.lookupBlueprint(context, BlueprintType.SHAPED, BlueprintType.SHAPELESS);

        boolean isAuroraRecipe = false;

        if (event.getRecipe() instanceof CraftingRecipe recipe) {
            if (recipe.getKey().getNamespace().equals("aurora")) {
                isAuroraRecipe = true;
            }
        }

        if (blueprint == null) {
            if (isAuroraRecipe) {
                event.getInventory().setResult(null);
            }
            return;
        }

        if (!blueprint.hasAccess(player)) {
            event.getInventory().setResult(null);
            return;
        }

        if (blueprint.getTimesCraftable(context) <= 0) {
            if (isAuroraRecipe) {
                event.getInventory().setResult(null);
            }
            return;
        }

        event.getInventory().setResult(blueprint.getResultItem(context));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraftItem(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getClickedInventory() instanceof CraftingInventory craftingInventory)) return;

        var workbench = plugin.getWorkbenchRegistry().getCraftingTable();
        if (event.getSlot() != workbench.getResultSlot()) return;

        var context = workbench.createContext(player, craftingInventory);
        var blueprint = workbench.lookupBlueprint(context, BlueprintType.SHAPED, BlueprintType.SHAPELESS);

        if (blueprint == null) {
            return;
        }

        if (!blueprint.isStacked()) return;

        var timesCraftable = blueprint.getTimesCraftable(context);
        if (timesCraftable == 0) {
            event.setCancelled(true);
            return;
        }

        // Don't fuck with the event if it isn't a shift click
        if (!event.isShiftClick()) {
            var result = craftingInventory.getResult();
            var newMatrix = blueprint.calcRemainingIngredientMatrix(context, 1);

            if (event.getClick() == ClickType.LEFT || event.getClick() == ClickType.RIGHT) {
                if (event.getCursor().isEmpty() || (event.getCursor().isSimilar(result) && event.getCursor().getMaxStackSize() >= event.getCursor().getAmount() + result.getAmount())) {
                    updateMatrix(player, craftingInventory, newMatrix);
                }
            } else if (event.getClick() == ClickType.SWAP_OFFHAND) {
                if (player.getInventory().getItemInOffHand().isEmpty()) {
                    updateMatrix(player, craftingInventory, newMatrix);
                }
            } else if (event.getClick() == ClickType.DROP || event.getClick() == ClickType.CONTROL_DROP) {
                updateMatrix(player, craftingInventory, newMatrix);
            } else {
                event.setCancelled(true);
                return;
            }

            craftingInventory.setResult(result);
        } else {
            // Let's see what can we do to not cancel the event
            if (Math.floorDiv(blueprint.getResultItem().getMaxStackSize(), blueprint.getResult().amount()) >= timesCraftable) {
                var currentResult = craftingInventory.getResult();

                updateMatrix(player, craftingInventory, blueprint.calcRemainingIngredientMatrix(context, timesCraftable));

                if (currentResult != null) {
                    currentResult.setAmount(currentResult.getAmount() * timesCraftable);
                }

                craftingInventory.setResult(currentResult);
            } else {
                // Well, there is no more workarounds at this point. Just cancel and handle shift click crafting manually
                event.setCancelled(true);
                final var currentItem = event.getCurrentItem() != null ? event.getCurrentItem().clone() : ItemStack.empty();

                int currentSpace = InventoryUtils.calculateSpaceForItem(player.getInventory(), currentItem);
                if (currentSpace < blueprint.getResult().amount()) {
                    return;
                }
                final int timesCrafted = Math.min((currentSpace / blueprint.getResult().amount()) + 1, timesCraftable);

                if (timesCrafted == 1) {
                    updateMatrix(player, event.getInventory(), blueprint.calcRemainingIngredientMatrix(context, 1));
                    player.getInventory().addItem(currentItem);
                } else {
                    var amount = timesCrafted * blueprint.getResult().amount();
                    var stacks = ItemUtils.createStacksFromAmount(currentItem, amount);
                    player.getInventory().addItem(stacks);
                    updateMatrix(player, event.getInventory(), blueprint.calcRemainingIngredientMatrix(context, timesCrafted));
                }
            }
        }

    }

    private void updateMatrix(Player player, Inventory inventory, ItemStack[] resultingMatrix) {
        var workbench = plugin.getWorkbenchRegistry().getCraftingTable();
        run(player, () -> {
            for (var i = 0; i < workbench.getMatrixSlots().size(); i++) {
                inventory.setItem(workbench.getMatrixSlots().get(i), resultingMatrix[i]);
            }
        });
    }

    private void run(Player player, Runnable runnable) {
        runnable.run();
        Bukkit.getRegionScheduler().run(plugin, player.getLocation(), (t) -> runnable.run());
    }
}
