package gg.auroramc.crafting.listener;

import gg.auroramc.aurora.api.util.ItemUtils;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.blueprint.Blueprint;
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
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CraftingListener implements Listener {
    private final AuroraCrafting plugin;
    // Crafts that vanilla will complete after onCraftItem, their actions run in onCraftItemMonitor if the event wasn't cancelled
    private final Map<InventoryClickEvent, PendingCraft> pendingCrafts = new ConcurrentHashMap<>();

    public CraftingListener(AuroraCrafting plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareCrafting(PrepareItemCraftEvent event) {
        if (event.getViewers().isEmpty()) return;
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

        if (!blueprint.isStacked()) {
            // Vanilla handles the whole craft (possibly multiple times on shift click), so we
            // snapshot the matrix and check how much of it was used up on the next tick
            if (!blueprint.getCraftActions().isEmpty() && event.getCurrentItem() != null && !event.getCurrentItem().isEmpty()) {
                var slots = workbench.getMatrixSlots(craftingInventory);
                var matrixBefore = new ItemStack[slots.size()];
                for (var i = 0; i < slots.size(); i++) {
                    var item = slots.get(i) == -1 ? null : craftingInventory.getItem(slots.get(i));
                    matrixBefore[i] = item == null ? null : item.clone();
                }
                pendingCrafts.put(event, new PendingCraft(blueprint, event.getCurrentItem().clone(), 0, matrixBefore));
            }
            return;
        }

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
                    addPendingCraft(event, blueprint, result, 1);
                }
            } else if (event.getClick() == ClickType.SWAP_OFFHAND) {
                if (player.getInventory().getItemInOffHand().isEmpty()) {
                    updateMatrix(player, craftingInventory, newMatrix);
                    addPendingCraft(event, blueprint, result, 1);
                }
            } else if (event.getClick() == ClickType.DROP || event.getClick() == ClickType.CONTROL_DROP) {
                updateMatrix(player, craftingInventory, newMatrix);
                addPendingCraft(event, blueprint, result, 1);
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
                addPendingCraft(event, blueprint, currentResult, timesCraftable);

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
                // We cancelled the event and crafted it ourselves, so the actions run here
                blueprint.executeCraftActions(player, currentItem, timesCrafted * blueprint.getResult().amount());
            }
        }

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCraftItemMonitor(InventoryClickEvent event) {
        var pending = pendingCrafts.remove(event);
        if (pending == null || event.isCancelled()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        var blueprint = pending.blueprint();
        var resultAmount = blueprint.getResult().amount();

        if (pending.timesCrafted() > 0) {
            blueprint.executeCraftActions(player, pending.result(), pending.timesCrafted() * resultAmount);
            return;
        }

        // Not stacked, vanilla crafts it. Every craft uses up 1 item from each ingredient slot
        var inventory = event.getClickedInventory();
        if (inventory == null) return;
        var slots = plugin.getWorkbenchRegistry().getCraftingTable().getMatrixSlots(inventory);

        player.getScheduler().run(plugin, (t) -> {
            // If the menu got closed in the meantime, the matrix was given back, so we can't tell what was crafted
            if (!inventory.equals(player.getOpenInventory().getTopInventory())) return;

            var timesCrafted = 0;
            for (var i = 0; i < slots.size(); i++) {
                var before = pending.matrixBefore()[i];
                if (slots.get(i) == -1 || before == null || before.isEmpty()) continue;
                var after = inventory.getItem(slots.get(i));
                // Items with a crafting remainder (e.g. buckets) change type instead of shrinking
                var amountAfter = after != null && after.isSimilar(before) ? after.getAmount() : 0;
                timesCrafted = Math.max(timesCrafted, before.getAmount() - amountAfter);
            }

            if (timesCrafted > 0) {
                blueprint.executeCraftActions(player, pending.result(), timesCrafted * resultAmount);
            }
        }, null);
    }

    private void addPendingCraft(InventoryClickEvent event, Blueprint blueprint, @Nullable ItemStack result, int timesCrafted) {
        if (blueprint.getCraftActions().isEmpty()) return;
        var resultItem = result == null || result.isEmpty() ? blueprint.getResultItem().clone() : result.clone();
        pendingCrafts.put(event, new PendingCraft(blueprint, resultItem, timesCrafted, null));
    }

    private record PendingCraft(Blueprint blueprint, ItemStack result, int timesCrafted, ItemStack[] matrixBefore) {
    }

    private void updateMatrix(Player player, Inventory inventory, ItemStack[] resultingMatrix) {
        var slots = plugin.getWorkbenchRegistry().getCraftingTable().getMatrixSlots(inventory);
        run(player, () -> {
            for (var i = 0; i < slots.size(); i++) {
                if (slots.get(i) == -1) continue;
                inventory.setItem(slots.get(i), resultingMatrix[i]);
            }
        });
    }

    private void run(Player player, Runnable runnable) {
        runnable.run();
        Bukkit.getRegionScheduler().run(plugin, player.getLocation(), (t) -> runnable.run());
    }
}
