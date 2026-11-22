package gg.auroramc.crafting.listener;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.aurora.api.util.ItemUtils;
import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.api.blueprint.BlueprintType;
import gg.auroramc.crafting.api.event.RegistryLoadEvent;
import gg.auroramc.crafting.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.SmithingInventory;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Collectors;

public class SmithingListener implements Listener {
    private final AuroraCrafting plugin;
    private final NamespacedKey smithingSoundKey = NamespacedKey.minecraft("block.smithing_table.use");
    // Replaced at once on reload, read from every player's own thread (Folia)
    private volatile Set<TypeId> disabledResults = Set.of();

    public SmithingListener(AuroraCrafting plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRegistryLoad(RegistryLoadEvent event) {
        disabledResults = plugin.getConfigManager().getDisabledRecipesConfig().getSmithingRecipes()
                .stream().map(TypeId::fromString).collect(Collectors.toUnmodifiableSet());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        if (!(event.getViewers().getFirst() instanceof Player player)) return;

        var workbench = plugin.getWorkbenchRegistry().getSmithingTable();
        var context = workbench.createContext(player, event.getInventory());
        var blueprint = workbench.lookupBlueprint(context, BlueprintType.SMITHING);

        // Our vanilla recipes use predicate choices on the resolved item id, so if vanilla matched
        // one of them we matched it too. No blueprint means this is a plain vanilla (or other plugin) recipe.
        if (blueprint == null) {
            if (event.getInventory().getInputTemplate() != null && event.getInventory().getInputTemplate().getType().name().endsWith("TRIM_SMITHING_TEMPLATE")) {
                return;
            }
            if (event.getResult() != null && !event.getResult().isEmpty()) {
                if (disabledResults.contains(AuroraAPI.getItemManager().resolveId(event.getResult()))) {
                    event.setResult(null);
                }
            }
            return;
        }

        if (!blueprint.hasAccess(player) || blueprint.getTimesCraftable(context) <= 0) {
            // We can't craft this blueprint here (no permission, or not enough items for its amounts). Such a blueprint
            // isn't registered if a vanilla recipe uses the same items (see VanillaWorkbench), so if vanilla matched a
            // recipe of its own here, that result stays (the netherite upgrade next to a recipe needing 4 ingots).
            if (isAuroraRecipe(event.getInventory().getRecipe())) {
                event.setResult(null);
            }
            return;
        }

        event.setResult(blueprint.getResultItem(context));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSmithing(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getClickedInventory() instanceof SmithingInventory)) return;

        var workbench = plugin.getWorkbenchRegistry().getSmithingTable();
        if (event.getSlot() != workbench.getResultSlot()) return;

        var context = workbench.createContext(player, event.getInventory());
        var blueprint = workbench.lookupBlueprint(context, BlueprintType.SMITHING);

        if (blueprint == null) {
            return;
        }

        if (!blueprint.isStacked()) return;

        // Can't craft this blueprint (no permission, or not enough items for its amounts): a result here comes from
        // another recipe with the same items (see onPrepareSmithing), so let vanilla handle the click
        var timesCraftable = blueprint.getTimesCraftable(context);
        if (timesCraftable == 0 || !blueprint.hasAccess(player)) return;

        event.setCancelled(true);

        // Ignore dumb ways of crafting
        if (event.getClick() != ClickType.SHIFT_LEFT && event.getClick() != ClickType.LEFT) {
            return;
        }

        final var currentItem = event.getCurrentItem() != null ? event.getCurrentItem().clone() : ItemStack.empty();
        final var sound = Registry.SOUNDS.get(smithingSoundKey);

        if (event.isShiftClick()) {
            int currentSpace = InventoryUtils.calculateSpaceForItem(player.getInventory(), currentItem);
            if (currentSpace < blueprint.getResult().amount()) {
                event.setCancelled(true);
                return;
            }
            final int availableSpace = currentSpace - blueprint.getResult().amount();
            final int timesCrafted = Math.min((availableSpace / blueprint.getResult().amount()) + 1, timesCraftable);

            if (timesCrafted == 1) {
                updateMatrix(player, event.getInventory(), blueprint.calcRemainingIngredientMatrix(context, 1));
                player.getInventory().addItem(currentItem);
                player.playSound(player, sound, 1f, 1f);
                blueprint.executeCraftActions(player, currentItem, blueprint.getResult().amount());
            } else {
                var amount = timesCrafted * blueprint.getResult().amount();
                var stacks = ItemUtils.createStacksFromAmount(currentItem, amount);
                player.getInventory().addItem(stacks);
                updateMatrix(player, event.getInventory(), blueprint.calcRemainingIngredientMatrix(context, timesCrafted));
                player.playSound(player, sound, 1f, 1f);
                blueprint.executeCraftActions(player, currentItem, amount);
            }
        } else {
            if (event.getCursor().isEmpty()) {
                updateMatrix(player, event.getInventory(), blueprint.calcRemainingIngredientMatrix(context, 1));
                player.getScheduler().run(plugin, (t) -> player.setItemOnCursor(currentItem), null);
                player.playSound(player, sound, 1f, 1f);
                blueprint.executeCraftActions(player, currentItem, blueprint.getResult().amount());
            } else {
                if (event.getCursor().isSimilar(currentItem)) {
                    var maxAmount = event.getCursor().getMaxStackSize() - event.getCursor().getAmount();
                    if (blueprint.getResult().amount() <= maxAmount) {
                        updateMatrix(player, event.getInventory(), blueprint.calcRemainingIngredientMatrix(context, 1));
                        player.playSound(player, sound, 1f, 1f);
                        player.getScheduler().run(plugin, (t) -> {
                            player.getItemOnCursor().setAmount(event.getCursor().getAmount() + blueprint.getResult().amount());
                        }, null);
                        blueprint.executeCraftActions(player, currentItem, blueprint.getResult().amount());
                    }
                }
            }
        }

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVanillaSmithingTake(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getClickedInventory() instanceof SmithingInventory inventory)) return;

        var workbench = plugin.getWorkbenchRegistry().getSmithingTable();
        if (event.getSlot() != workbench.getResultSlot()) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().isEmpty()) return;

        var context = workbench.createContext(player, inventory);
        var blueprint = workbench.lookupBlueprint(context, BlueprintType.SMITHING);

        // Stacked blueprints are crafted by onSmithing (which cancels the event), and runs the actions itself
        // Without access the result came from another recipe (see onPrepareSmithing), not this blueprint
        if (blueprint == null || blueprint.isStacked() || blueprint.getCraftActions().isEmpty() || !blueprint.hasAccess(player)) return;

        // Vanilla takes the result after this event, possibly multiple times (shift click),
        // so we compare the consumed inputs on the next tick to see how many times it was crafted
        final var result = event.getCurrentItem().clone();
        final var matrixSlots = workbench.getMatrixSlots();
        final var amountsBefore = new int[matrixSlots.size()];
        for (var i = 0; i < matrixSlots.size(); i++) {
            var item = inventory.getItem(matrixSlots.get(i));
            amountsBefore[i] = item == null ? 0 : item.getAmount();
        }

        player.getScheduler().run(plugin, (t) -> {
            // If the menu got closed in the meantime, the inputs were given back, so we can't tell what was crafted
            if (!inventory.equals(player.getOpenInventory().getTopInventory())) return;

            var timesCrafted = 0;
            for (var i = 0; i < matrixSlots.size(); i++) {
                var item = inventory.getItem(matrixSlots.get(i));
                var amountAfter = item == null ? 0 : item.getAmount();
                timesCrafted = Math.max(timesCrafted, amountsBefore[i] - amountAfter);
            }

            if (timesCrafted > 0) {
                blueprint.executeCraftActions(player, result, timesCrafted * blueprint.getResult().amount());
            }
        }, null);
    }

    private static boolean isAuroraRecipe(@Nullable Recipe recipe) {
        return recipe instanceof Keyed keyed && keyed.getKey().getNamespace().equals("aurora");
    }

    private void updateMatrix(Player player, Inventory inventory, ItemStack[] resultingMatrix) {
        var workbench = plugin.getWorkbenchRegistry().getSmithingTable();
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
